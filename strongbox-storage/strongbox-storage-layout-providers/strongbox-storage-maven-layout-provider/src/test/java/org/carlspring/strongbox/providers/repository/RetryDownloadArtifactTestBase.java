package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.client.CloseableRestResponse;
import org.carlspring.strongbox.client.RemoteRepositoryRetryArtifactDownloadConfiguration;
import org.carlspring.strongbox.client.MutableRemoteRepositoryRetryArtifactDownloadConfiguration;
import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.providers.repository.proxied.RestArtifactResolverFactory;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Throwables;
import org.junit.After;
import org.junit.Before;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author Przemyslaw Fusik
 */
public abstract class RetryDownloadArtifactTestBase
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    static final Resource jarArtifact = new ClassPathResource("artifacts/properties-injector-1.7.jar");

    protected static int BUF_SIZE = 8192;
    
    @Inject
    RestArtifactResolverFactory artifactResolverFactory;

    @Inject
    ArtifactEntryService artifactEntryService;

    @Before
    public void timeoutRetryFeatureRatherQuicklyForTestPurposes()
            throws Exception
    {
        final MutableRemoteRepositoryRetryArtifactDownloadConfiguration remoteRepositoryRetryArtifactDownloadConfiguration =
                new MutableRemoteRepositoryRetryArtifactDownloadConfiguration();
        remoteRepositoryRetryArtifactDownloadConfiguration.setMaxNumberOfAttempts(5);
        remoteRepositoryRetryArtifactDownloadConfiguration.setTimeoutSeconds(30);
        remoteRepositoryRetryArtifactDownloadConfiguration.setMinAttemptsIntervalSeconds(1);
        configurationManagementService.set(remoteRepositoryRetryArtifactDownloadConfiguration);
    }

    @Before
    @After
    public void cleanup()
            throws Exception
    {
        deleteDirectoryRelativeToVaultDirectory(
                "storages/storage-common-proxies/maven-central/org/carlspring/properties-injector");
        artifactEntryService.deleteAll();
    }

    void prepareArtifactResolverContext(final InputStream artifactInputStream,
                                        final boolean rangeRquestSupported)
    {
        
        RemoteRepositoryRetryArtifactDownloadConfiguration configuration = configurationManager.getConfiguration()
                                                                                               .getRemoteRepositoriesConfiguration()
                                                                                               .getRemoteRepositoryRetryArtifactDownloadConfiguration();

        final Response response = Mockito.mock(Response.class);
        Mockito.when(response.getEntity()).thenReturn(artifactInputStream);
        Mockito.when(response.readEntity(InputStream.class)).thenReturn(artifactInputStream);
        Mockito.when(response.getStatus()).thenReturn(200);
        Mockito.when(response.getHeaderString("Accept-Ranges")).thenReturn(rangeRquestSupported ? "bytes" : "none");

        final CloseableRestResponse restResponse = Mockito.mock(CloseableRestResponse.class);
        Mockito.when(restResponse.getResponse()).thenReturn(response);

        final RestArtifactResolver artifactResolver = Mockito.mock(RestArtifactResolver.class);
        Mockito.when(artifactResolver.get(Matchers.any(String.class))).thenReturn(restResponse);
        Mockito.when(artifactResolver.get(Matchers.any(String.class), Matchers.any(Long.class))).thenReturn(
                restResponse);
        Mockito.when(artifactResolver.head(Matchers.any(String.class))).thenReturn(restResponse);
        Mockito.when(artifactResolver.getConfiguration()).thenReturn(configuration);
        Mockito.when(artifactResolver.isAlive()).thenReturn(true);

        Mockito.when(artifactResolverFactory.newInstance(Matchers.any(RemoteRepository.class)))
               .thenReturn(artifactResolver);
    }

    abstract static class BrokenArtifactInputStream
            extends InputStream
    {

        InputStream artifactInputStream;

        BrokenArtifactInputStream(final Resource jarArtifact)
        {
            try
            {
                this.artifactInputStream = jarArtifact.getInputStream();
            }
            catch (final IOException e)
            {
                throw Throwables.propagate(e);
            }
        }

        @Override
        public abstract int read()
                throws IOException;

    }
}
