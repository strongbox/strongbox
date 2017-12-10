package org.carlspring.strongbox.providers.repository;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.client.CloseableRestResponse;
import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.client.RestArtifactResolverFactory;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.carlspring.strongbox.services.ArtifactByteStreamsCopyStrategy.BUF_SIZE;
import static org.junit.Assert.*;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RetryDownloadArtifactTest.MockedRestArtifactResolverTestConfig.class)
public class RetryDownloadArtifactTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final Resource jarArtifact = new ClassPathResource("artifacts/properties-injector-1.7.jar");

    private static final Resource metadataArtifact = new ClassPathResource("artifacts/maven-metadata.xml");

    private static final OneTimeBrokenArtifactInputStream brokenInputStream = new OneTimeBrokenArtifactInputStream(jarArtifact);

    @Inject
    private ArtifactResolutionService artifactResolutionService;

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Before
    @After
    public void cleanup()
            throws Exception
    {
        deleteDirectoryRelativeToVaultDirectory(
                "storages/storage-common-proxies/maven-central/org/carlspring/properties-injector");
        artifactEntryService.deleteAll();
    }

    @Test
    public void whenDownloadingArtifactDatabaseShouldBeAffectedByArtifactEntry()
            throws Exception
    {

        String storageId = "storage-common-proxies";
        String repositoryId = "maven-central";
        String path = "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar";

        Path destinationPath = getVaultDirectoryPath().resolve("storages").resolve(storageId).resolve(
                repositoryId).resolve(path);

        // given
        assertFalse(Files.exists(destinationPath));
        assertFalse(brokenInputStream.exceptionAlreadyThrown);

        // when
        assertStreamNotNull(storageId, repositoryId, path);

        // then
        assertTrue(Files.exists(destinationPath));
        assertTrue(brokenInputStream.exceptionAlreadyThrown);
    }

    private void assertStreamNotNull(String storageId,
                                     String repositoryId,
                                     String path)
            throws Exception
    {
        try (final InputStream is = artifactResolutionService.getInputStream(storageId, repositoryId, path))
        {
            assertNotNull("Failed to resolve " + path + "!", is);

            if (ArtifactUtils.isMetadata(path))
            {
                System.out.println(ByteStreams.toByteArray(is));
            }
        }
    }

    static class OneTimeBrokenArtifactInputStream
            extends InputStream
    {

        private int currentReadSize;

        private InputStream artifactInputStream;

        private boolean exceptionAlreadyThrown;

        public OneTimeBrokenArtifactInputStream(final Resource jarArtifact)
        {
            try
            {
                this.artifactInputStream = jarArtifact.getInputStream();
            }
            catch (IOException e)
            {
                throw Throwables.propagate(e);
            }
        }

        @Override
        public int read()
                throws IOException
        {

            if (currentReadSize == BUF_SIZE && !exceptionAlreadyThrown)
            {
                exceptionAlreadyThrown = true;
                throw new IOException("Connection lost.");
            }

            currentReadSize++;
            return artifactInputStream.read();
        }

    }

    @Configuration
    @Import({ Maven2LayoutProviderTestConfig.class })
    static class MockedRestArtifactResolverTestConfig
    {

        @Bean
        @Primary
        RestArtifactResolverFactory artifactResolverFactory()
                throws Exception
        {

            final Response response = Mockito.mock(Response.class);
            Mockito.when(response.getEntity()).thenReturn(brokenInputStream);
            Mockito.when(response.readEntity(InputStream.class)).thenReturn(brokenInputStream);
            Mockito.when(response.getStatus()).thenReturn(200);
            Mockito.when(response.getHeaderString("Accept-Ranges")).thenReturn("bytes");

            final CloseableRestResponse restResponse = Mockito.mock(CloseableRestResponse.class);
            Mockito.when(restResponse.getResponse()).thenReturn(response);

            final RestArtifactResolver artifactResolver = Mockito.mock(RestArtifactResolver.class);
            Mockito.when(artifactResolver.get(Matchers.any(String.class))).thenReturn(restResponse);
            Mockito.when(artifactResolver.get(Matchers.any(String.class), Matchers.any(Long.class))).thenReturn(
                    restResponse);
            Mockito.when(artifactResolver.head(Matchers.any(String.class))).thenReturn(restResponse);

            final RestArtifactResolverFactory artifactResolverFactory = Mockito.mock(RestArtifactResolverFactory.class);
            Mockito.when(artifactResolverFactory.newInstance(Matchers.any(String.class), Matchers.any(String.class),
                                                             Matchers.any(String.class))).thenReturn(artifactResolver);

            return artifactResolverFactory;
        }

    }
}
