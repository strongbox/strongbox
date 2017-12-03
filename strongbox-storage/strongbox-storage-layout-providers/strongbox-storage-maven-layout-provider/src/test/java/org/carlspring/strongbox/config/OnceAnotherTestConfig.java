package org.carlspring.strongbox.config;

import org.carlspring.strongbox.client.ArtifactResolver;
import org.carlspring.strongbox.client.ArtifactResolverFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import static org.carlspring.strongbox.services.ArtifactManagementService.ARTIFACT_READ_BUFFER_SIZE;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Import({ Maven2LayoutProviderTestConfig.class })
public class OnceAnotherTestConfig
{

    @Bean
    @Primary
    ArtifactResolverFactory artifactResolverFactory()
            throws Exception
    {
        final InputStream inputStream = new BrokenInputStream();

        final Response response = Mockito.mock(Response.class);
        Mockito.when(response.getEntity()).thenReturn(inputStream);
        Mockito.when(response.readEntity(InputStream.class)).thenReturn(inputStream);
        Mockito.when(response.getStatus()).thenReturn(200);

        final ArtifactResolver artifactResolver = Mockito.mock(ArtifactResolver.class);
        Mockito.when(artifactResolver.getResourceWithResponse(Matchers.any(String.class))).thenReturn(response);

        final ArtifactResolverFactory artifactResolverFactory = Mockito.mock(ArtifactResolverFactory.class);

        Mockito.when(artifactResolverFactory.newInstance(Matchers.any(Client.class))).thenReturn(artifactResolver);

        return artifactResolverFactory;
    }

    public static class BrokenInputStream
            extends InputStream
    {

        private int currentReadSize;

        @Override
        public int read()
                throws IOException
        {
            if (currentReadSize > ARTIFACT_READ_BUFFER_SIZE)
            {
                throw new IOException("Connection lost.");
            }
            return currentReadSize++;
        }
    }

}
