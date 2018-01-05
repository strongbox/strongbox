package org.carlspring.strongbox.providers.io;

import java.nio.file.Path;

import org.carlspring.strongbox.domain.ArtifactEntry;

/**
 * @author sbespalov
 *
 */
public class RootRepositoryPath extends RepositoryPath
{

    public RootRepositoryPath(Path target,
                              RepositoryFileSystem fileSystem)
    {
        super(target, fileSystem);
    }

    public RepositoryPath resolve(ArtifactEntry artifactEntry)
    {
        this.artifactEntry = artifactEntry;
        return super.resolve(artifactEntry.getArtifactPath());
    }

}
