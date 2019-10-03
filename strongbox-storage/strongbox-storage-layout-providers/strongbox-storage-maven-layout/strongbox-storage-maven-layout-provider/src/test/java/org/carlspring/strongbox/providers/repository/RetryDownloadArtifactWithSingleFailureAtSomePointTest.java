package org.carlspring.strongbox.providers.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.maven.artifact.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.Resource;

/**
 * @author Przemyslaw Fusik
 */
public class RetryDownloadArtifactWithSingleFailureAtSomePointTest
        extends MockedRestArtifactResolverTestBase
        implements ArtifactResolverContext
{

    private static final String REPOSITORY = "rdawsfaspt-repository";

    private static final String PROXY_REPOSITORY_URL = "https://repo.maven.apache.org/maven2/";
    
    private OneTimeBrokenArtifactInputStream brokenArtifactInputStream;

    private boolean exceptionAlreadyThrown;

    @Override
    public InputStream getInputStream()
    {
        return brokenArtifactInputStream;
    }

    @Override
    protected ArtifactResolverContext lookupArtifactResolverContext()
    {
        return this;
    }

    @Override
    @BeforeEach
    public void setup()
            throws IOException
    {
        super.setup();

        brokenArtifactInputStream = new OneTimeBrokenArtifactInputStream(jarArtifact);
    }

    @Test
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    public void resurrectedInputStreamShouldBeSuccessfullyHandledByRetryFeature(@MavenRepository(repositoryId = REPOSITORY)
                                                                                @Remote(url = PROXY_REPOSITORY_URL)
                                                                                Repository proxyRepository)
            throws Exception
    {
        final String storageId = proxyRepository.getStorage().getId();
        final String repositoryId = proxyRepository.getId();

        Artifact artifact = MavenArtifactTestUtils.getArtifactFromGAVTC("org.apache.commons:commons-lang3:3.2");
        String path = MavenArtifactUtils.convertArtifactToPath(artifact);
        RepositoryPath artifactPath = repositoryPathResolver.resolve(proxyRepository,
                                                                     path);
        
        // given
        assertThat(Files.exists(artifactPath)).isFalse();
        assertThat(exceptionAlreadyThrown).isFalse();

        // when
        artifactResolutionServiceHelper.assertStreamNotNull(storageId,
                                                            repositoryId,
                                                            path);

        // then
        assertThat(Files.exists(artifactPath)).isTrue();
        assertThat(Files.size(artifactPath)).isEqualTo(Files.size(jarArtifact.getFile().toPath()));
        assertThat(exceptionAlreadyThrown).isTrue();
    }

    private class OneTimeBrokenArtifactInputStream
            extends MockedRestArtifactResolverTestBase.BrokenArtifactInputStream
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
