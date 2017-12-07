package org.carlspring.strongbox.config;

import org.carlspring.strongbox.client.CloseableRestResponse;
import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.client.RestArtifactResolverFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import static org.carlspring.strongbox.services.ArtifactByteStreamsCopyStrategy.BUF_SIZE;

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
            throws Exception
    {
        final InputStream brokenInputStream = new AnotherChanceInputStream(2, 3);

        final Response response = Mockito.mock(Response.class);
        Mockito.when(response.getEntity()).thenReturn(brokenInputStream);
        Mockito.when(response.readEntity(InputStream.class)).thenReturn(brokenInputStream);
        Mockito.when(response.getStatus()).thenReturn(200);
        Mockito.when(response.getHeaderString("Accept-Ranges")).thenReturn("bytes");

        final CloseableRestResponse restResponse = Mockito.mock(CloseableRestResponse.class);
        Mockito.when(restResponse.getResponse()).thenReturn(response);

        final RestArtifactResolver artifactResolver = Mockito.mock(RestArtifactResolver.class);
        Mockito.when(artifactResolver.get(Matchers.any(String.class))).thenReturn(restResponse);
        Mockito.when(artifactResolver.head(Matchers.any(String.class))).thenReturn(restResponse);

        final RestArtifactResolverFactory artifactResolverFactory = Mockito.mock(RestArtifactResolverFactory.class);
        Mockito.when(artifactResolverFactory.newInstance(Matchers.any(String.class), Matchers.any(String.class),
                                                         Matchers.any(String.class))).thenReturn(artifactResolver);

        return artifactResolverFactory;
    }

    public static class AnotherChanceInputStream
            extends InputStream
    {

        private final int positiveAttempt;

        private final int maxAttempts;

        private int attempt = 1;

        private int currentReadSize;

        public AnotherChanceInputStream(final int positiveAttempt,
                                        final int maxAttempts)
        {
            this.positiveAttempt = positiveAttempt;
            this.maxAttempts = maxAttempts;
        }


        @Override
        public int read()
                throws IOException
        {

            if (currentReadSize > BUF_SIZE)
            {
                currentReadSize = 0;
                if (attempt == maxAttempts)
                {
                    return -1;
                }
                if (attempt++ != positiveAttempt)
                {
                    throw new IOException("Connection lost.");
                }

            }
            return currentReadSize++;
        }
    }

}
