package org.carlspring.strongbox.providers.layout;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Nonnull;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.domain.ArtifactGroup;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repository.RepositoryManagementStrategy;

/**
 * @author carlspring
 */
public interface LayoutProvider<T extends ArtifactCoordinates>
{
    RepositoryManagementStrategy getRepositoryManagementStrategy();

    @Nonnull
    Set<String> listArchiveFilenames(RepositoryPath repositoryPath);

    Set<String> getDefaultArtifactCoordinateValidators();

    String getAlias();

    @Nonnull
    Set<ArtifactGroup> getArtifactGroups(RepositoryPath path)
            throws IOException;

}
