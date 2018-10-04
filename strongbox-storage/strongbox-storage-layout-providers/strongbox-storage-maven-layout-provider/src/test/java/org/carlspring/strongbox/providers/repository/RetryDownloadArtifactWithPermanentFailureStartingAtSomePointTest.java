package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.config.MockedRestArtifactResolverTestConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertFalse;

/**
 * @author Przemyslaw Fusik
 */
@ActiveProfiles({"MockedRestArtifactResolverTestConfig","test"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MockedRestArtifactResolverTestConfig.class)
public class RetryDownloadArtifactWithPermanentFailureStartingAtSomePointTest
        extends RetryDownloadArtifactTestBase
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private PermanentBrokenArtifactInputStream brokenArtifactInputStream;

    @Before
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

        //then
        thrown.expect(IOException.class);
        thrown.expectMessage(containsString("Connection lost."));

        // when
        assertStreamNotNull(storageId, repositoryId, path);
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

