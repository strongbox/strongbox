package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.client.RestArtifactResolverFactory;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.repository.proxied.CloseableProxyRepositoryResponse;
import org.carlspring.strongbox.services.ArtifactByteStreams;
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
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ProxyRepositoryArtifactByteStreams
        implements ArtifactByteStreams
{

    /**
     * TODO move to xml
     */
    public static final int MAX_NB_OF_ATTEMPTS = 5;
    /**
     * TODO move to xml
     */
    public static final int TIMEOUT_IN_SECONDS = 60;
    /**
     * TODO move to xml
     */
    public static final int MIN_DELAY_BETWEEN_ATTEMPTS_IN_SECONDS = 5;

    private static final Logger logger = LoggerFactory.getLogger(ProxyRepositoryArtifactByteStreams.class);

    @Inject
    private RemoteRepositoryAlivenessCacheManager remoteRepositoryAlivenessCacheManager;

    @Inject
    private RestArtifactResolverFactory artifactResolverFactory;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    private ArtifactByteStreams simpleArtifactByteStreams = SimpleArtifactByteStreams.INSTANCE;

    @Override
    public long copy(final InputStream inputStream,
                     final OutputStream outputStream,
                     final RepositoryPath artifactPath)
            throws IOException
    {
        StopWatch stopWatch = new StopWatch();
        int attempts = 1;

        try
        {
            stopWatch.start();
            return tryFull(inputStream, outputStream, artifactPath);
        }
        catch (IOException ex)
        {
            MutableObject<Boolean> rangeRequestSupported = new MutableObject();
            retry(artifactPath, stopWatch, attempts, rangeRequestSupported, ex);
        }
    }

    private void retry(final RepositoryPath artifactPath,
                       final StopWatch stopWatch,
                       int attempts,
                       final MutableObject<Boolean> rangeRequestSupported,
                       final IOException ex)
            throws IOException
    {
        finishUnsuccessfullyIfNumberOfAttemptsExceedTheLimit(attempts, ex);
        tryToSleepRequestedAmountOfTimeBetweenAttempts(ex);
        finishUnsuccessfullyIfTimeoutOccurred(stopWatch, ex);

        attempts++;
        if (checkRemoteRepositoryHeartbeat(artifactPath))
        {
            if (rangeRequestSupported.getValue() == null)
            {
                rangeRequestSupported.setValue(BooleanUtils.isTrue(isRangeRequestSupported(artifactPath)));
            }
            if (BooleanUtils.isNotTrue(rangeRequestSupported.getValue()))
            {
                throw //TODO;
            }
        }


    }

    private boolean isRangeRequestSupported(final RepositoryPath artifactPath)
            throws IOException
    {
        RemoteRepository remoteRepository = artifactPath.getFileSystem().getRepository().getRemoteRepository();
        try (final RestArtifactResolver client = artifactResolverFactory.newInstance(remoteRepository.getUrl(),
                                                                                     HttpAuthenticationFeature.basic(
                                                                                             remoteRepository.getUsername(),
                                                                                             remoteRepository.getPassword())))
        {
            final ArtifactCoordinates c = RepositoryFiles.readCoordinates(artifactPath);
            final URI resource = c.toResource();

            try (final CloseableProxyRepositoryResponse closeableProxyRepositoryResponse = new CloseableProxyRepositoryResponse(client.head(
                    resource.toString())))
            {
                final Response response = closeableProxyRepositoryResponse.getResponse();

                if (response.getStatus() != 200 || response.getEntity() == null)
                {
                    return false;
                }

                final String acceptRangesHeader = response.getHeaderString("Accept-Ranges");
                return (StringUtils.isNotBlank(acceptRangesHeader) && !"none".equals(acceptRangesHeader));
            }
            catch (ArtifactTransportException e)
            {
                throw new IOException(e);
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
            Thread.sleep(MIN_DELAY_BETWEEN_ATTEMPTS_IN_SECONDS * 100);
        }
        catch (InterruptedException e)
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
        if (attempts > MAX_NB_OF_ATTEMPTS)
        {
            throw ex;
        }
    }

    private void finishUnsuccessfullyIfTimeoutOccurred(final StopWatch stopWatch,
                                                       final IOException ex)
            throws IOException
    {
        if (stopWatch.getTime() > TIMEOUT_IN_SECONDS * 1000l)
        {
            throw ex;
        }
    }

    private long tryFull(final InputStream inputStream,
                         final OutputStream outputStream,
                         final RepositoryPath repositoryDestinationPath)
            throws IOException
    {
        return simpleArtifactByteStreams.copy(inputStream, outputStream, repositoryDestinationPath);
    }
}
