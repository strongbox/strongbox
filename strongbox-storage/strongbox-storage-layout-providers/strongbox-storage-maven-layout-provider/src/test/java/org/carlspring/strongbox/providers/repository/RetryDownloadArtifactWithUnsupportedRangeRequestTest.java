package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.config.MockedRestArtifactResolverTestConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Przemyslaw Fusik
 */
@ActiveProfiles("MockedRestArtifactResolverTestConfig")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MockedRestArtifactResolverTestConfig.class)
public class RetryDownloadArtifactWithUnsupportedRangeRequestTest
        extends RetryDownloadArtifactTestBase
{
    private boolean exceptionAlreadyThrown;
    
    private OneTimeBrokenArtifactInputStream brokenArtifactInputStream;

    @BeforeEach
    public void setup()
            throws Exception
    {
        brokenArtifactInputStream = new OneTimeBrokenArtifactInputStream(jarArtifact);
        prepareArtifactResolverContext(brokenArtifactInputStream, false);
    }

    @Test
    public void unsupportedRangeProxyRepositoryRequestShouldSkipRetryFeature()
            throws Exception
    {
        final String storageId = "storage-common-proxies";
        final String repositoryId = "maven-central";
        final String path = "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar";
        final Path destinationPath = getVaultDirectoryPath().resolve("storages").resolve(storageId).resolve(
                repositoryId).resolve(path);

        // given
        assertFalse(Files.exists(destinationPath));
        assertFalse(exceptionAlreadyThrown);

        IOException exception = assertThrows(IOException.class, () -> {
            // when
            assertStreamNotNull(storageId, repositoryId, path);
        });

        //then
        assertThat(exception.getMessage(), containsString("does not support range requests."));

    }

    private class OneTimeBrokenArtifactInputStream
            extends RetryDownloadArtifactTestBase.BrokenArtifactInputStream
    {

        private int currentReadSize;

        public OneTimeBrokenArtifactInputStream(final Resource jarArtifact)
        {
            super(jarArtifact);
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
}
