package org.carlspring.strongbox.artifact;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.javatuples.Pair;

import java.io.IOException;

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

    public static String getArtifactFileName(MavenArtifact artifact)
    {
        return ArtifactUtils.getArtifactFileName(artifact);
    }

    public static Pair<String, String> getArtifactGroupId(RepositoryPath repositoryPath) throws IOException{
        MavenArtifact tmpArtifact = MavenArtifactUtils.convertPathToArtifact(repositoryPath);

        return Pair.with(tmpArtifact.getGroupId(), tmpArtifact.getArtifactId());
    }
    
    public static MavenArtifact convertPathToArtifact(RepositoryPath repositoryPath) throws IOException
    {
        String path = RepositoryFiles.relativizePath(repositoryPath);
        Artifact artifact = ArtifactUtils.convertPathToArtifact(path);
        
        return new MavenRepositoryArtifact(artifact, repositoryPath);
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

    public static MavenArtifact getPOMArtifact(MavenArtifact source)
    {
        Artifact artifact = ArtifactUtils.getPOMArtifact(source);
        return new MavenRepositoryArtifact(artifact, source.getPath());
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
