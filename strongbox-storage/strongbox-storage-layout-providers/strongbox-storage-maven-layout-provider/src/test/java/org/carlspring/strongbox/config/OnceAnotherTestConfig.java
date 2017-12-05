package org.carlspring.strongbox.config;

import org.carlspring.strongbox.client.CloseableRestResponse;
import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.client.RestArtifactResolverFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

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
    RestArtifactResolverFactory artifactResolverFactory()
            throws Exception
    {
        final InputStream inputStream = new BrokenInputStream();

        final CloseableRestResponse restResponse = Mockito.mock(CloseableRestResponse.class);

        final Response response = Mockito.mock(Response.class);
        Mockito.when(response.getEntity()).thenReturn(inputStream);
        Mockito.when(response.readEntity(InputStream.class)).thenReturn(inputStream);
        Mockito.when(response.getStatus()).thenReturn(200);

        final RestArtifactResolver artifactResolver = Mockito.mock(RestArtifactResolver.class);


        /*
        TODO
        Mockito.when(artifactResolver.get(Matchers.any(String.class))).thenReturn(response);
        */

        final RestArtifactResolverFactory artifactResolverFactory = Mockito.mock(RestArtifactResolverFactory.class);

        /*
        TODO
        Mockito.when(artifactResolverFactory.newInstance(Matchers.any(Client.class))).thenReturn(artifactResolver);
        */

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
