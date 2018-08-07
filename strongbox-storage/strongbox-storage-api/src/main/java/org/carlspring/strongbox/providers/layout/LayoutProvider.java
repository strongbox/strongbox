package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repository.RepositoryManagementStrategy;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Set;

/**
 * @author carlspring
 */
public interface LayoutProvider<T extends ArtifactCoordinates>
{

    //TODO: probably we should move this method into `RepositoryProvider` as "contains" logic depends on Repository type
    @Deprecated
    boolean containsPath(RepositoryPath repositoryPath)
            throws IOException;


    //TODO: we should perform delete logic in RepositoryPath and RepositoryFileSystemProvider
    @Deprecated
    void deleteMetadata(RepositoryPath repositoryPath)
            throws IOException;

    RepositoryManagementStrategy getRepositoryManagementStrategy();

    @Nonnull
    Set<String> listArchiveFilenames(RepositoryPath repositoryPath);

    Set<String> getDefaultArtifactCoordinateValidators();

}
