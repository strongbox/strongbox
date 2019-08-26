package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.client.CloseableRestResponse;
import org.carlspring.strongbox.client.MutableRemoteRepositoryRetryArtifactDownloadConfiguration;
import org.carlspring.strongbox.client.RemoteRepositoryRetryArtifactDownloadConfiguration;
import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.config.hazelcast.HazelcastConfiguration;
import org.carlspring.strongbox.config.hazelcast.HazelcastInstanceId;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.repository.proxied.RestArtifactResolverFactory;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.testing.artifact.ArtifactResolutionServiceHelper;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Path;
import java.util.Objects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 */
@ActiveProfiles({"MockedRestArtifactResolverTestConfig", "test"})
@SpringBootTest
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public abstract class MockedRestArtifactResolverTestBase
{

    static final Resource jarArtifact = new ClassPathResource("artifacts/properties-injector-1.7.jar");

    protected static final String STORAGE0 = "storage0";

    protected static int BUF_SIZE = 8192;

    private static ThreadLocal<ArtifactResolverContext> contextHolder = new ThreadLocal<>();

    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    @Inject
    protected ArtifactResolutionServiceHelper artifactResolutionServiceHelper;

    @BeforeEach
    public void setup()
            throws IOException
    {
        initContext(lookupArtifactResolverContext());
    }

    @AfterEach
    public void cleanup()
    {
        cleanContext();
    }

    protected static ArtifactResolverContext getContext()
    {
        ArtifactResolverContext result = contextHolder.get();
        Objects.requireNonNull(result);
        
        return result;
    }
    
    protected static void initContext(ArtifactResolverContext context)
    {
        contextHolder.set(context);
    }

    
    protected static void cleanContext()
    {
        contextHolder.remove();
    }
    
    private static RemoteRepositoryRetryArtifactDownloadConfiguration createRemoteRepositoryConfiguration()
    {
        MutableRemoteRepositoryRetryArtifactDownloadConfiguration radc = new MutableRemoteRepositoryRetryArtifactDownloadConfiguration();
        radc.setMaxNumberOfAttempts(5);
        radc.setTimeoutSeconds(30);
        radc.setMinAttemptsIntervalSeconds(1);
        
        return new RemoteRepositoryRetryArtifactDownloadConfiguration(radc);
    }

    protected abstract ArtifactResolverContext lookupArtifactResolverContext();
    
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
            RestArtifactResolverFactory artifactResolverFactory = Mockito.mock(RestArtifactResolverFactory.class);

            Response response = Mockito.mock(Response.class);
            Mockito.when(response.getEntity()).then((i) -> getContext().getInputStream());
            Mockito.when(response.readEntity(InputStream.class)).then((i) -> getContext().getInputStream());
            Mockito.when(response.getStatus()).thenReturn(200);
            Mockito.when(response.getHeaderString("Accept-Ranges"))
                   .then((invocation) -> getContext().isByteRangeRequestSupported() ? "bytes" : "none");

            CloseableRestResponse restResponse = Mockito.mock(CloseableRestResponse.class);
            Mockito.when(restResponse.getResponse()).thenReturn(response);

            RestArtifactResolver artifactResolver = Mockito.mock(RestArtifactResolver.class);
            Mockito.when(artifactResolver.get(ArgumentMatchers.any(String.class))).thenReturn(restResponse);
            Mockito.when(artifactResolver.get(ArgumentMatchers.any(String.class), ArgumentMatchers.any(Long.class)))
                   .thenReturn(restResponse);
            Mockito.when(artifactResolver.head(ArgumentMatchers.any(String.class))).thenReturn(restResponse);
            Mockito.when(artifactResolver.getConfiguration())
                   .then((a) -> createRemoteRepositoryConfiguration());
            Mockito.when(artifactResolver.isAlive()).thenReturn(true);

            Mockito.when(artifactResolverFactory.newInstance(ArgumentMatchers.any(RemoteRepository.class)))
                   .thenReturn(artifactResolver);
            
            return artifactResolverFactory;
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

interface ArtifactResolverContext
{

    InputStream getInputStream();
    
    default boolean isByteRangeRequestSupported()
    {
        return true;
    }
    
}
