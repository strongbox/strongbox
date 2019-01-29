package org.carlspring.strongbox.domain;

import javax.persistence.Entity;

/**
 * @author Przemyslaw Fusik
 */
@Entity
public class ArtifactIdGroup
        extends ArtifactGroup
{

    public ArtifactIdGroup()
    {
        super();
    }

    public ArtifactIdGroup(final String name)
    {
        super(name);
    }

    public String getArtifactId()
    {
        return getName();
    }

}
