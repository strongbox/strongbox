package org.carlspring.strongbox.providers.repository;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.carlspring.strongbox.client.CloseableRestResponse;
import org.carlspring.strongbox.client.MutableRemoteRepositoryRetryArtifactDownloadConfiguration;
import org.carlspring.strongbox.client.RemoteRepositoryRetryArtifactDownloadConfiguration;
import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.config.HazelcastConfiguration;
import org.carlspring.strongbox.config.HazelcastInstanceId;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.providers.repository.proxied.RestArtifactResolverFactory;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.common.collect.ImmutableMap;

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

    @BeforeEach
    public void timeoutRetryFeatureRatherQuicklyForTestPurposes()
    {
        final MutableRemoteRepositoryRetryArtifactDownloadConfiguration remoteRepositoryRetryArtifactDownloadConfiguration =
                new MutableRemoteRepositoryRetryArtifactDownloadConfiguration();
        remoteRepositoryRetryArtifactDownloadConfiguration.setMaxNumberOfAttempts(5);
        remoteRepositoryRetryArtifactDownloadConfiguration.setTimeoutSeconds(30);
        remoteRepositoryRetryArtifactDownloadConfiguration.setMinAttemptsIntervalSeconds(1);
        configurationManagementService.set(remoteRepositoryRetryArtifactDownloadConfiguration);
    }

    @BeforeEach
    @AfterEach
    public void cleanup()
            throws Exception
    {
        deleteDirectoryRelativeToVaultDirectory(getVaultDirectoryVersionPath());

        artifactEntryService.delete(
                artifactEntryService.findArtifactList("storage-common-proxies",
                                                      "maven-central",
                                                      ImmutableMap.of("groupId", getGroupId(),
                                                                      "artifactId", getArtifactId(),
                                                                      "version", getArtifactVersion()),
                                                      true));
    }

    protected String getVaultDirectoryVersionPath()
    {
        return "storages/storage-common-proxies/maven-central/" + getGroupId().replaceAll(".", "/") + "/" +
               getArtifactId() + "/" + getArtifactVersion();
    }

    protected String getJarPath()
    {
        return getGroupId().replaceAll("\\.", "/") + "/" + getArtifactId() + "/" + getArtifactVersion() + "/" +
               getJarArtifact();
    }

    protected String getGroupId()
    {
        return "org.apache.commons";
    }

    protected String getArtifactId()
    {
        return "commons-lang3";
    }

    protected String getJarArtifact()
    {
        return getArtifactId() + "-" + getArtifactVersion() + ".jar";
    }

    protected abstract String getArtifactVersion();

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
        Mockito.when(artifactResolver.get(ArgumentMatchers.any(String.class))).thenReturn(restResponse);
        Mockito.when(artifactResolver.get(ArgumentMatchers.any(String.class), ArgumentMatchers.any(Long.class))).thenReturn(
                restResponse);
        Mockito.when(artifactResolver.head(ArgumentMatchers.any(String.class))).thenReturn(restResponse);
        Mockito.when(artifactResolver.getConfiguration()).thenReturn(configuration);
        Mockito.when(artifactResolver.isAlive()).thenReturn(true);

        Mockito.when(artifactResolverFactory.newInstance(ArgumentMatchers.any(RemoteRepository.class)))
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
                throw new UndeclaredThrowableException(e);
            }
        }

        @Override
        public abstract int read()
                throws IOException;

    }

    @Profile("MockedRestArtifactResolverTestConfig")
    @Import(HazelcastConfiguration.class)
    @Configuration
    public static class MockedRestArtifactResolverTestConfig
    {
        
        @Primary
        @Bean
        public HazelcastInstanceId hazelcastInstanceIdRdatb() 
        {
            return new HazelcastInstanceId("mocked-hazelcast-instance");
        }
        
        @Bean
        @Primary
        RestArtifactResolverFactory mockedArtifactResolverFactory()
        {
            return Mockito.mock(RestArtifactResolverFactory.class);
        }

        @Bean
        @Primary
        ArtifactEventListenerRegistry testArtifactEventListenerRegistry()
        {
            return new TestArtifactEventListenerRegistry();
        }

        public static class TestArtifactEventListenerRegistry extends ArtifactEventListenerRegistry
        {

            @Override
            public void dispatchArtifactFetchedFromRemoteEvent(Path path)
            {
                // this event cause java.io.EOFException within `MavenArtifactFetchedFromRemoteEventListener`
            }

        }

    }

}
