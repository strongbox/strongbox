package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.config.MockedRestArtifactResolverTestConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.carlspring.strongbox.services.ArtifactByteStreamsCopyStrategy.BUF_SIZE;
import static org.junit.Assert.*;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MockedRestArtifactResolverTestConfig.class)
public class RetryDownloadArtifactWithSingleFailureAtSomePointTest
        extends RetryDownloadArtifactTestBase
{

    private OneTimeBrokenArtifactInputStream brokenArtifactInputStream;

    @Before
    public void timeoutRetryFeatureRatherQuicklyForTestPurposes()
            throws Exception
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
        final String path = "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar";
        final Path destinationPath = getVaultDirectoryPath().resolve("storages").resolve(storageId).resolve(
                repositoryId).resolve(path);

        // given
        assertFalse(Files.exists(destinationPath));
        assertFalse(brokenArtifactInputStream.exceptionAlreadyThrown);

        // when
        assertStreamNotNull(storageId, repositoryId, path);

        // then
        assertTrue(Files.exists(destinationPath));
        assertThat(Files.size(destinationPath), CoreMatchers.equalTo(Files.size(jarArtifact.getFile().toPath())));
        assertTrue(brokenArtifactInputStream.exceptionAlreadyThrown);

    }

    static class OneTimeBrokenArtifactInputStream
            extends RetryDownloadArtifactTestBase.BrokenArtifactInputStream
    {

        private int currentReadSize;

        private boolean exceptionAlreadyThrown;

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
