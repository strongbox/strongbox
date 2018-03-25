package org.carlspring.strongbox.artifact;

import org.carlspring.maven.commons.DetachedArtifact;

import java.io.File;
import java.nio.file.Path;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.VersionRange;

/**
 * @author Przemyslaw Fusik
 */
public class MavenDetachedArtifact
        extends DetachedArtifact
        implements MavenArtifact
{

    private Path path;

    public MavenDetachedArtifact(Artifact artifact)
    {
        this(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getType(),
             artifact.getClassifier());
    }

    public MavenDetachedArtifact(final String groupId,
                                 final String artifactId,
                                 final VersionRange version,
                                 final String type,
                                 final String classifier)
    {
        super(groupId, artifactId, version, type, classifier);
    }

    public MavenDetachedArtifact(final String groupId,
                                 final String artifactId,
                                 final String version,
                                 final String type,
                                 final String classifier)
    {
        super(groupId, artifactId, version, type, classifier);
    }

    public MavenDetachedArtifact(final String groupId,
                                 final String artifactId,
                                 final String version,
                                 final String type)
    {
        super(groupId, artifactId, version, type);
    }

    public MavenDetachedArtifact(final String groupId,
                                 final String artifactId,
                                 final String version)
    {
        super(groupId, artifactId, version);
    }

    public MavenDetachedArtifact(final String groupId,
                                 final String artifactId)
    {
        super(groupId, artifactId);
    }

    @Override
    public File getFile()
    {
        throw new UnsupportedOperationException("Use getPath instead");
    }

    @Override
    public void setFile(File destination)
    {
        throw new UnsupportedOperationException("Use setPath instead");
    }

    @Override
    public Path getPath()
    {
        return path;
    }

    @Override
    public void setPath(final Path path)
    {
        this.path = path;
    }
}
