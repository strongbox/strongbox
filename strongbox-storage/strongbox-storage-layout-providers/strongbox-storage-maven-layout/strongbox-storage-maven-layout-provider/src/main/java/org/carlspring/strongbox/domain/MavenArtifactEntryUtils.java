package org.carlspring.strongbox.domain;

import org.apache.maven.index.artifact.Gav;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;

/**
 * @author Przemyslaw Fusik
 */
public class MavenArtifactEntryUtils
{

    public static Gav toGav(Artifact artifactEntry)
    {
        return MavenArtifactUtils.convertPathToGav(artifactEntry.getArtifactPath());
    }

}
