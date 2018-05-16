package org.carlspring.strongbox.artifact;

import org.carlspring.strongbox.domain.ArtifactGroupIdentifier;

import javax.persistence.Entity;

/**
 * @author Przemyslaw Fusik
 */
@Entity
public class MavenArtifactGroupIdentifier
        extends ArtifactGroupIdentifier
{

    private String groupId;

    private String artifactId;

    private String version;

    public MavenArtifactGroupIdentifier(final String artifactId,
                                        final String groupId,
                                        final String version)
    {
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.version = version;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        return version;
    }
}
