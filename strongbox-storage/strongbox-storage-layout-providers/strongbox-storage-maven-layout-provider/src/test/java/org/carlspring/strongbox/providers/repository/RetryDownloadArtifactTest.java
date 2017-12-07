package org.carlspring.strongbox.providers.repository;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.MockedRestArtifactResolverTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import com.google.common.io.ByteStreams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertNotNull;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MockedRestArtifactResolverTestConfig.class)
public class RetryDownloadArtifactTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

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
        String path = "org/carlspring/properties-injector/1.6/properties-injector-1.6.jar";

        assertStreamNotNull(storageId, repositoryId, path);
    }

    private void assertStreamNotNull(String storageId,
                                     String repositoryId,
                                     String path)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
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


}
