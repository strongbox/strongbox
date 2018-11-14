package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.domain.ArtifactEntry;

import java.nio.file.Path;

/**
 * @author sbespalov
 *
 */
public class RootRepositoryPath extends RepositoryPath
{

    public RootRepositoryPath(Path target,
                              LayoutFileSystem fileSystem)
    {
        super(target, fileSystem);
    }

    public RepositoryPath resolve(ArtifactEntry artifactEntry)
    {
        RepositoryPath result = super.resolve(artifactEntry.getArtifactPath());
        result.artifactEntry = artifactEntry;
        return result;
    }

}
