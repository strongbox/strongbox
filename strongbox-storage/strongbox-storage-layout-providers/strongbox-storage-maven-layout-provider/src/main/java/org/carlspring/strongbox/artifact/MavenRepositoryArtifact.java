package org.carlspring.strongbox.artifact;

import org.carlspring.maven.commons.DetachedArtifact;
import org.carlspring.strongbox.providers.io.RepositoryPath;

import com.google.common.io.Files;

import java.io.File;
import java.util.Optional;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.VersionRange;

/**
 * @author Przemyslaw Fusik
 */
public class MavenRepositoryArtifact
        extends DetachedArtifact
        implements MavenArtifact
{

    private RepositoryPath path;

    public MavenRepositoryArtifact(Artifact artifact, RepositoryPath path) {
        this(artifact);
        this.path = path;
    }
    
    public MavenRepositoryArtifact(Artifact artifact)
    {
        this(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getType(),
             artifact.getClassifier());
    }

    public MavenRepositoryArtifact(final String groupId,
                                 final String artifactId,
                                 final VersionRange version,
                                 final String type,
                                 final String classifier)
    {
        super(groupId, artifactId, version, type, classifier);
    }

    public MavenRepositoryArtifact(final String groupId,
                                 final String artifactId,
                                 final String version,
                                 final String type,
                                 final String classifier)
    {
        super(groupId, artifactId, version, type, classifier);
    }

    public MavenRepositoryArtifact(final String groupId,
                                 final String artifactId,
                                 final String version,
                                 final String type)
    {
        super(groupId, artifactId, version, type);
    }

    public MavenRepositoryArtifact(final String groupId,
                                 final String artifactId,
                                 final String version)
    {
        super(groupId, artifactId, version);
    }

    public MavenRepositoryArtifact(final String groupId,
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
    public RepositoryPath getPath()
    {
        return path;
    }

    @Override
    public void setPath(final RepositoryPath path)
    {
        this.path = path;
    }

    @Override
    public String getType()
    {
        String type = super.getType();
        if (type != null && type.trim().length() > 0) {
            return type;
        }
        
        String fileName = path.getFileName().toString();
        return Optional.of(fileName.lastIndexOf('.'))
                       .map(i -> (i == -1) ? "" : fileName.substring(i + 1))
                       .get();
    }
    
    
}
