package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.artifact.MavenArtifactUtils;

import org.apache.maven.index.artifact.Gav;

/**
 * @author Przemyslaw Fusik
 */
public class MavenArtifactEntryUtils
{

    public static Gav toGav(ArtifactEntry artifactEntry)
    {
        return MavenArtifactUtils.convertPathToGav(artifactEntry.getArtifactPath());
    }

}
