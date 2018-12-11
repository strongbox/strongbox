package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 */
@ActiveProfiles({"MockedRestArtifactResolverTestConfig", "test"})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class RetryDownloadArtifactWithSingleFailureAtSomePointTest
        extends RetryDownloadArtifactTestBase
{

    private OneTimeBrokenArtifactInputStream brokenArtifactInputStream;

    private boolean exceptionAlreadyThrown;


    @BeforeEach
    public void setup()
    {
        brokenArtifactInputStream = new OneTimeBrokenArtifactInputStream(jarArtifact);
        prepareArtifactResolverContext(brokenArtifactInputStream, true);
    }

    @Test
    public void resurrectedInputStreamShouldBeSuccessfullyHandledByRetryFeature()
            throws Exception
    {
        final String storageId = "storage-common-proxies";
        final String repositoryId = "maven-central";
        final String path = getJarPath();
        final Path destinationPath = getVaultDirectoryPath().resolve("storages")
                                                            .resolve(storageId)
                                                            .resolve(repositoryId)
                                                            .resolve(path);

        // given
        assertFalse(Files.exists(destinationPath));
        assertFalse(exceptionAlreadyThrown);

        // when
        assertStreamNotNull(storageId, repositoryId, path);

        // then
        assertTrue(Files.exists(destinationPath));
        assertThat(Files.size(destinationPath), CoreMatchers.equalTo(Files.size(jarArtifact.getFile().toPath())));
        assertTrue(exceptionAlreadyThrown);
    }

    @Override
    protected String getArtifactVersion()
    {
        return "3.2";
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
