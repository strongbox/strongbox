package org.carlspring.strongbox.providers.io;

import java.nio.file.Path;

import org.carlspring.strongbox.domain.Artifact;

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

    public RepositoryPath resolve(Artifact artifactEntry)
    {
        RepositoryPath result = super.resolve(artifactEntry.getArtifactPath());
        result.artifact = artifactEntry;
        return result;
    }

}
