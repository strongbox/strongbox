package org.carlspring.strongbox.artifact;

import org.carlspring.maven.commons.util.ArtifactUtils;

import org.apache.maven.artifact.Artifact;

/**
 * @author Przemyslaw Fusik
 */
public class MavenArtifactUtils
{

    public static String convertArtifactToPath(MavenArtifact artifact)
    {
        return ArtifactUtils.convertArtifactToPath(artifact);
    }

    public static MavenArtifact getArtifactFromGAVTC(String gavtc)
    {
        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC(gavtc);
        return new MavenDetachedArtifact(artifact);
    }

    public static String getArtifactFileName(MavenArtifact artifact)
    {
        return ArtifactUtils.getArtifactFileName(artifact);
    }

    public static MavenArtifact convertPathToArtifact(String path)
    {
        Artifact artifact = ArtifactUtils.convertPathToArtifact(path);
        return new MavenDetachedArtifact(artifact);
    }

    public static String getSnapshotBaseVersion(String version)
    {
        return getSnapshotBaseVersion(version, true);
    }

    public static String getSnapshotBaseVersion(String version,
                                                boolean appendSnapshotSuffix)
    {
        return ArtifactUtils.getSnapshotBaseVersion(version, appendSnapshotSuffix);
    }

    public static String getArtifactLevelMetadataPath(Artifact artifact)
    {
        return ArtifactUtils.getArtifactLevelMetadataPath(artifact);
    }

    public static MavenArtifact getPOMArtifactFromGAV(String gav)
    {
        Artifact artifact = ArtifactUtils.getPOMArtifactFromGAV(gav);
        return new MavenDetachedArtifact(artifact);
    }

    public static MavenArtifact getPOMArtifact(MavenArtifact source)
    {
        Artifact artifact = ArtifactUtils.getPOMArtifact(source);
        return new MavenDetachedArtifact(artifact);
    }

    public static boolean isSnapshot(String version)
    {
        return ArtifactUtils.isSnapshot(version);
    }

    public static String getVersionLevelMetadataPath(MavenArtifact artifact)
    {
        return ArtifactUtils.getVersionLevelMetadataPath(artifact);
    }

}
