package org.carlspring.strongbox.services.impl;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.carlspring.strongbox.client.CloseableRestResponse;
import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.client.RestArtifactResolverFactory;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.configuration.RemoteRepositoryRetryArtifactDownloadConfiguration;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ArtifactByteStreamsCopyStrategy;
import org.carlspring.strongbox.services.support.ArtifactByteStreamsCopyException;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ProxyRepositoryArtifactByteStreamsCopy
        implements ArtifactByteStreamsCopyStrategy
{

    private static final Logger logger = LoggerFactory.getLogger(ProxyRepositoryArtifactByteStreamsCopy.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private RemoteRepositoryAlivenessCacheManager remoteRepositoryAlivenessCacheManager;

    @Inject
    private RestArtifactResolverFactory artifactResolverFactory;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    private ArtifactByteStreamsCopyStrategy simpleArtifactByteStreams = SimpleArtifactByteStreamsCopy.INSTANCE;
    
    private ThreadLocal<ArtifactCopyContext> artifactCopyContext = new ThreadLocal<>();;
    
    
    @Override
    public long copy(final InputStream from,
                     final OutputStream to,
                     final RepositoryPath artifactPath)
            throws IOException
    {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        long result;
        try(ArtifactCopyContext context = new ArtifactCopyContext())
        {
            context.setAttempts(1);
            context.setCurrentOffset(0);
            context.setStopWatch(stopWatch);
            artifactCopyContext.set(context);

            copyWithOffset(from, to, artifactPath);
            
            result = artifactCopyContext.get().getCurrentOffset();
        }
        
        return result;
    }

    private void copyWithOffset(final InputStream from,
                                final OutputStream to,
                                final RepositoryPath artifactPath)
            throws IOException
    {
        ArtifactCopyContext ctx = artifactCopyContext.get();
        
        try
        {
            long offset = simpleArtifactByteStreams.copy(from, to, artifactPath);
            ctx.setCurrentOffset(ctx.getCurrentOffset() + offset);
        }
        catch (ArtifactByteStreamsCopyException ex)
        {
            ctx.setCurrentOffset(ctx.getCurrentOffset() + ex.getOffset());
            retryCopyIfPossible(to, artifactPath, ex);
        }
    }

    private void retryCopyIfPossible(final OutputStream to,
                                     final RepositoryPath artifactPath,
                                     final IOException lastException)
            throws IOException
    {
        ArtifactCopyContext ctx = artifactCopyContext.get();
        ctx.setAttempts(ctx.getAttempts() + 1);
        
        logger.debug("Retrying remote stream copying ... Attempt number = [{}], Current Offset = [{}] Duration Time = [{}]",
                     ctx.getAttempts(), ctx.getCurrentOffset(),
                     ctx.getStopWatch());
        
        finishUnsuccessfullyIfNumberOfAttemptsExceedTheLimit(lastException);
        tryToSleepRequestedAmountOfTimeBetweenAttempts(lastException);
        finishUnsuccessfullyIfTimeoutOccurred(lastException);
        
        if (!checkRemoteRepositoryHeartbeat(artifactPath))
        {
            retryCopyIfPossible(to, artifactPath, lastException);
        }
        
        ctx.setRangeRequestSupported(Optional.ofNullable(ctx.getRangeRequestSupported())
                                             .orElse(BooleanUtils.isTrue(isRangeRequestSupported(artifactPath))));
        
        if (BooleanUtils.isNotTrue(ctx.getRangeRequestSupported()))
        {
            throw new IOException(
                    String.format("IOException occurred and repository of path [%s] does not support range requests.",
                                  artifactPath),
                    lastException);
        }

        performRangeRequest(to, artifactPath);
    }

    private void performRangeRequest(final OutputStream to,
                                     final RepositoryPath artifactPath)
            throws IOException
    {
        ArtifactCopyContext ctx = artifactCopyContext.get();
        
        RemoteRepository remoteRepository = artifactPath.getFileSystem().getRepository().getRemoteRepository();
        RestArtifactResolver client = ctx.getClient(remoteRepository);
        String resourcePath = getRestClientResourcePath(artifactPath);
        
        ctx.closeConnection();
        CloseableRestResponse closeableRestResponse = client.get(resourcePath, ctx.getCurrentOffset());
        ctx.setConnection(closeableRestResponse);
        
        Response response = closeableRestResponse.getResponse();
        if (response.getStatus() != 200 || response.getEntity() == null)
        {
            throw new IOException(String.format("Unreadable response from %s. Response status is %s",
                                                remoteRepository.getUrl(), response.getStatus()));
        }
        InputStream is = response.readEntity(InputStream.class);
        if (is == null)
        {
            throw new IOException(String.format("Unexpected null as InputStream from response from %s.",
                                                remoteRepository.getUrl()));
        }

        copyWithOffset(is, to, artifactPath);
    }

    private boolean isRangeRequestSupported(final RepositoryPath artifactPath)
            throws IOException
    {
        ArtifactCopyContext ctx = artifactCopyContext.get();
        
        RemoteRepository remoteRepository = artifactPath.getFileSystem().getRepository().getRemoteRepository();
        RestArtifactResolver client = ctx.getClient(remoteRepository);
        
        final String resourcePath = getRestClientResourcePath(artifactPath);
        try (final CloseableRestResponse closeableRestResponse = client.head(resourcePath))
        {
            final Response response = closeableRestResponse.getResponse();

            if (response.getStatus() != 200 || response.getEntity() == null)
            {
                return false;
            }

            final String acceptRangesHeader = response.getHeaderString("Accept-Ranges");
            return StringUtils.isNotBlank(acceptRangesHeader) && !"none".equals(acceptRangesHeader);
        }
    }    
    
    private boolean checkRemoteRepositoryHeartbeat(final RepositoryPath artifactPath)
    {
        final RemoteRepository remoteRepository = artifactPath.getFileSystem().getRepository().getRemoteRepository();
        return remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository);
    }

    private void tryToSleepRequestedAmountOfTimeBetweenAttempts(final IOException ex)
            throws IOException
    {
        try
        {
            //TODO: we should use Object.wait() instead
            Thread.sleep(getSleepMillisTimeBeforeNextAttempt());
        }
        catch (final InterruptedException e)
        {
            Thread.currentThread().interrupt();
            logger.error(e.getMessage(), e);
            throw ex;
        }
    }

    private void finishUnsuccessfullyIfNumberOfAttemptsExceedTheLimit(final IOException ex)
            throws IOException
    {
        if (artifactCopyContext.get().getAttempts() > getMaxAllowedNumberOfRetryAttempts())
        {
            throw ex;
        }
    }

    private void finishUnsuccessfullyIfTimeoutOccurred(final IOException ex)
            throws IOException
    {
        if (artifactCopyContext.get().getStopWatch().getTime() > getRetryTimeoutMillis())
        {
            throw ex;
        }
    }

    private String getRestClientResourcePath(final RepositoryPath artifactPath)
            throws IOException
    {
        final RepositoryPath finalArtifactPath = resolveToFinalArtifactPath(artifactPath);
        final Repository repository = finalArtifactPath.getFileSystem().getRepository();
        final LayoutProvider<?> layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final URI resource = layoutProvider.resolveResource(repository, finalArtifactPath.toString());
        return resource.toString();
    }

    private RepositoryPath resolveToFinalArtifactPath(final RepositoryPath artifactPath)
    {
        RepositoryPath finalArtifactPath = artifactPath;
        if (artifactPath.startsWith(artifactPath.getFileSystem().getTempPath().toString()))
        {
            finalArtifactPath = artifactPath.getFileSystem().getTempPath().relativize(artifactPath);
        }
        return finalArtifactPath;
    }

    private RestArtifactResolver getRestArtifactResolver(final RemoteRepository remoteRepository)
    {
        return artifactResolverFactory.newInstance(remoteRepository.getUrl(), remoteRepository.getUsername(),
                                                   remoteRepository.getPassword());
    }

    private long getRetryTimeoutMillis()
    {
        return getRetryConfiguration().getTimeoutSeconds() * 1000L;
    }

    private long getSleepMillisTimeBeforeNextAttempt()
    {
        return getRetryConfiguration().getMinAttemptsIntervalSeconds() * 1000L;
    }

    private int getMaxAllowedNumberOfRetryAttempts()
    {
        return getRetryConfiguration().getMaxNumberOfAttempts();
    }

    private RemoteRepositoryRetryArtifactDownloadConfiguration getRetryConfiguration()
    {
        return configurationManager.getConfiguration()
                                   .getRemoteRepositoriesConfiguration()
                                   .getRemoteRepositoryRetryArtifactDownloadConfiguration();
    }

    private class ArtifactCopyContext implements Closeable
    {
        
        private StopWatch stopWatch;
        private int attempts;
        private long currentOffset;
        private Boolean rangeRequestSupported;
        private RestArtifactResolver client;
        private Closeable connection;

        public StopWatch getStopWatch()
        {
            return stopWatch;
        }

        public void setStopWatch(StopWatch stopWatch)
        {
            this.stopWatch = stopWatch;
        }

        public int getAttempts()
        {
            return attempts;
        }

        public void setAttempts(int attempts)
        {
            this.attempts = attempts;
        }

        public long getCurrentOffset()
        {
            return currentOffset;
        }

        public void setCurrentOffset(long currentOffset)
        {
            this.currentOffset = currentOffset;
        }

        public Boolean getRangeRequestSupported()
        {
            return rangeRequestSupported;
        }

        public void setRangeRequestSupported(Boolean rangeRequestSupported)
        {
            this.rangeRequestSupported = rangeRequestSupported;
        }

        public RestArtifactResolver getClient(RemoteRepository repository)
        {
            return client = Optional.ofNullable(client).orElse(getRestArtifactResolver(repository));
        }

        public void setConnection(Closeable connection)
        {
            this.connection = connection;
        }

        public void closeConnection()
        {
            Optional.ofNullable(connection).ifPresent(c -> IOUtils.closeQuietly(c));
            connection = null;
        }
        
        @Override
        public void close()
            throws IOException
        {
            try
            {
                Optional.ofNullable(client).ifPresent(c -> IOUtils.closeQuietly(c));
                client = null;
            }
            finally
            {
                artifactCopyContext.remove();
            }
        }
        
    }

}
