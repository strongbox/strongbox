package org.carlspring.strongbox.providers.repository;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Przemyslaw Fusik
 */
@ActiveProfiles({"MockedRestArtifactResolverTestConfig","test"})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class RetryDownloadArtifactWithPermanentFailureStartingAtSomePointTest
        extends RetryDownloadArtifactTestBase
{

    private PermanentBrokenArtifactInputStream brokenArtifactInputStream;

    @BeforeEach
    public void setup()
            throws Exception
    {
        brokenArtifactInputStream = new PermanentBrokenArtifactInputStream(jarArtifact);
        prepareArtifactResolverContext(brokenArtifactInputStream, true);
    }

    @Test
    public void whenProxyRepositoryInputStreamFailsCompletelyArtifactDownloadShouldFail()
            throws Exception
    {
        final String storageId = "storage-common-proxies";
        final String repositoryId = "maven-central";
        final String path = "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar";
        final Path destinationPath = getVaultDirectoryPath().resolve("storages").resolve(storageId).resolve(
                repositoryId).resolve(path);

        // given
        assertFalse(Files.exists(destinationPath));


        IOException exception = assertThrows(IOException.class, () -> {
            // when
            assertStreamNotNull(storageId, repositoryId, path);
        });

        //then
        assertEquals("Connection lost.", exception.getMessage());
    }

    static class PermanentBrokenArtifactInputStream
            extends RetryDownloadArtifactTestBase.BrokenArtifactInputStream
    {

        private int currentReadSize;

        public PermanentBrokenArtifactInputStream(final Resource jarArtifact)
        {
            super(jarArtifact);
        }

        @Override
        public int read()
                throws IOException
        {

            if (currentReadSize >= BUF_SIZE)
            {
                throw new IOException("Connection lost.");
            }

            currentReadSize++;
            return artifactInputStream.read();
        }

    }
}

