package org.carlspring.strongbox.config;

import org.carlspring.strongbox.client.RestArtifactResolverFactory;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Import({ Maven2LayoutProviderTestConfig.class })
public class MockedRestArtifactResolverTestConfig
{

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
        final ArtifactEventListenerRegistry artifactEventListenerRegistry = Mockito.mock(
                ArtifactEventListenerRegistry.class);
        Mockito.doNothing().when(artifactEventListenerRegistry).dispatchArtifactFetchedFromRemoteEvent(
                Matchers.any(String.class),
                Matchers.any(String.class),
                Matchers.any(String.class));
        return artifactEventListenerRegistry;
    }

    @Bean
    @Primary
    String ehCacheCacheManagerId()
    {
        return "mockedRestArtifactResolverTestConfigCacheManager";
    }

}
