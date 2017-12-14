package org.carlspring.strongbox.services.impl;

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

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.time.StopWatch;
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

    @Override
    public long copy(final InputStream from,
                     final OutputStream to,
                     final RepositoryPath artifactPath)
            throws IOException
    {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        return copyWithOffset(from, to, artifactPath, stopWatch, 1, 0L, new MutableObject());
    }

    private long copyWithOffset(final InputStream from,
                                final OutputStream to,
                                final RepositoryPath artifactPath,
                                final StopWatch stopWatch,
                                final int attempts,
                                final long currentOffset,
                                final MutableObject<Boolean> rangeRequestSupported)
            throws IOException
    {
        try
        {
            return currentOffset + copySimple(from, to, artifactPath);
        }
        catch (final ArtifactByteStreamsCopyException ex)
        {
            return retryCopyIfPossible(to,
                                       artifactPath,
                                       stopWatch,
                                       attempts,
                                       currentOffset + ex.getOffset(),
                                       rangeRequestSupported,
                                       ex);
        }
    }

    private long copySimple(final InputStream from,
                            final OutputStream to,
                            final RepositoryPath repositoryDestinationPath)
            throws IOException
    {
        return simpleArtifactByteStreams.copy(from, to, repositoryDestinationPath);
    }

    private long retryCopyIfPossible(final OutputStream to,
                                     final RepositoryPath artifactPath,
                                     final StopWatch stopWatch,
                                     final int attempts,
                                     final long currentOffset,
                                     final MutableObject<Boolean> rangeRequestSupported,
                                     final IOException lastException)
            throws IOException
    {
        logger.debug(
                "Retrying remote stream copying ... Attempt number = [{}], Current Offset = [{}] Duration Time = [{}]",
                attempts, currentOffset, stopWatch);
        finishUnsuccessfullyIfNumberOfAttemptsExceedTheLimit(attempts, lastException);
        tryToSleepRequestedAmountOfTimeBetweenAttempts(lastException);
        finishUnsuccessfullyIfTimeoutOccurred(stopWatch, lastException);

        if (checkRemoteRepositoryHeartbeat(artifactPath))
        {
            if (rangeRequestSupported.getValue() == null)
            {
                rangeRequestSupported.setValue(BooleanUtils.isTrue(isRangeRequestSupported(artifactPath)));
            }
            if (BooleanUtils.isNotTrue(rangeRequestSupported.getValue()))
            {
                throw new IOException("IOException occurred and repository of path " + artifactPath +
                                      " does not support range requests.", lastException);
            }

            return performRangeRequest(to,
                                       artifactPath,
                                       stopWatch,
                                       attempts + 1,
                                       currentOffset,
                                       rangeRequestSupported);
        }

        return retryCopyIfPossible(to,
                                   artifactPath,
                                   stopWatch,
                                   attempts + 1,
                                   currentOffset,
                                   rangeRequestSupported,
                                   lastException);
    }

    private boolean isRangeRequestSupported(final RepositoryPath artifactPath)
            throws IOException
    {
        final RemoteRepository remoteRepository = artifactPath.getFileSystem().getRepository().getRemoteRepository();
        //TODO: we should cache this if once determined for concrete artifact
        try (final RestArtifactResolver client = getRestArtifactResolver(remoteRepository))
        {
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
    }

    private long performRangeRequest(final OutputStream to,
                                     final RepositoryPath artifactPath,
                                     final StopWatch stopWatch,
                                     final int attempts,
                                     final long currentOffset,
                                     final MutableObject<Boolean> rangeRequestSupported)
            throws IOException
    {
        final RemoteRepository remoteRepository = artifactPath.getFileSystem().getRepository().getRemoteRepository();
        try (final RestArtifactResolver client = getRestArtifactResolver(remoteRepository))
        {
            final String resourcePath = getRestClientResourcePath(artifactPath);
            try (final CloseableRestResponse closeableRestResponse = client.get(resourcePath, currentOffset))
            {
                final Response response = closeableRestResponse.getResponse();

                if (response.getStatus() != 200 || response.getEntity() == null)
                {
                    throw new IOException(String.format("Unreadable response from %s. Response status is %s",
                                                        remoteRepository.getUrl(), response.getStatus()));
                }
                final InputStream is = response.readEntity(InputStream.class);
                if (is == null)
                {
                    throw new IOException(String.format("Unexpected null as InputStream from response from %s.",
                                                        remoteRepository.getUrl()));
                }

                return copyWithOffset(is, to, artifactPath, stopWatch, attempts, currentOffset, rangeRequestSupported);
            }
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


    private void finishUnsuccessfullyIfNumberOfAttemptsExceedTheLimit(final int attempts,
                                                                      final IOException ex)
            throws IOException
    {
        if (attempts > getMaxAllowedNumberOfRetryAttempts())
        {
            throw ex;
        }
    }


    private void finishUnsuccessfullyIfTimeoutOccurred(final StopWatch stopWatch,
                                                       final IOException ex)
            throws IOException
    {
        if (stopWatch.getTime() > getRetryTimeoutMillis())
        {
            throw ex;
        }
    }


    private String getRestClientResourcePath(final RepositoryPath artifactPath)
            throws IOException
    {
        final RepositoryPath finalArtifactPath = resolveToFinalArtifactPath(artifactPath);
        final Repository repository = finalArtifactPath.getFileSystem().getRepository();
        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
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


}
