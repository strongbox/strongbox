package org.carlspring.strongbox.config;

import static org.carlspring.strongbox.config.HazelcastConfiguration.newDefaultMapConfig;

import java.nio.file.Path;

import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.providers.repository.proxied.RestArtifactResolverFactory;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.hazelcast.config.Config;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Import({ Maven2LayoutProviderTestConfig.class })
public class MockedRestArtifactResolverTestConfig
{
    @Primary
    @Bean
    public Config hazelcastConfig()
    {
        final Config config = new Config().setInstanceName("mocked-hazelcast-instance")
                                          .addMapConfig(newDefaultMapConfig(CacheName.Repository.REMOTE_REPOSITORY_ALIVENESS))
                                          .addMapConfig(newDefaultMapConfig(CacheName.Artifact.TAGS));
        
        config.getGroupConfig().setName("strongbox").setPassword("password");
        return config;
    }

    @Bean
    @Primary
    RestArtifactResolverFactory artifactResolverFactory()
    {
        return Mockito.mock(RestArtifactResolverFactory.class);
    }

    @Bean
    @Primary
    ArtifactEventListenerRegistry artifactEventListenerRegistry()
    {
        return new TestArtifactEventListenerRegistry();
    }    
    
    public static class TestArtifactEventListenerRegistry extends ArtifactEventListenerRegistry {

        @Override
        public void dispatchArtifactFetchedFromRemoteEvent(Path path)
        {
            // this event cause java.io.EOFException within `MavenArtifactFetchedFromRemoteEventListener`
        }
        
    }

}
