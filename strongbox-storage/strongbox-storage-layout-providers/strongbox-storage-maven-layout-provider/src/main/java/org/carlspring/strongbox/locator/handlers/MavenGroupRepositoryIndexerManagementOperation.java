package org.carlspring.strongbox.locator.handlers;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactIndexesService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Przemyslaw Fusik
 */
public class MavenGroupRepositoryIndexerManagementOperation
        extends MavenIndexerManagementOperation
{

    private final RepositoryPath groupRepositoryPath;

    public MavenGroupRepositoryIndexerManagementOperation(final ArtifactIndexesService artifactIndexesService,
                                                          final RepositoryPath groupRepositoryPath)
    {
        super(artifactIndexesService);
        this.groupRepositoryPath = groupRepositoryPath;
    }

    @Override
    protected RepositoryPath getArtifactPathToIndex(final RepositoryPath filePath)
    {
        final RepositoryPath relativeRegularFilePath = filePath.relativize();
        final RepositoryPath groupRepositoryRegularFilePath = groupRepositoryPath.resolve(relativeRegularFilePath);
        return groupRepositoryRegularFilePath;
    }
}
