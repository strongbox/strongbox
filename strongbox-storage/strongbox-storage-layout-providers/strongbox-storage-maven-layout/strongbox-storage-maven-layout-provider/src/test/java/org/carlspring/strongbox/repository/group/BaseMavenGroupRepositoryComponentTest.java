package org.carlspring.strongbox.repository.group;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
public abstract class BaseMavenGroupRepositoryComponentTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    protected void copyArtifactMetadata(String sourceRepositoryId,
                                        String destinationRepositoryId,
                                        String path)
            throws IOException
    {
        final Storage storage = getConfiguration().getStorage(STORAGE0);

        Repository repository = storage.getRepository(sourceRepositoryId);
        final Path sourcePath = repositoryPathResolver.resolve(repository, path);

        repository = storage.getRepository(destinationRepositoryId);
        final Path destinationPath = repositoryPathResolver.resolve(repository, path);
        FileUtils.copyFile(sourcePath.toFile(), destinationPath.toFile());
    }

}
