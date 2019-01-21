package org.carlspring.strongbox.domain;

import javax.persistence.Entity;

/**
 * @author Przemyslaw Fusik
 */
@Entity
public class ArtifactIdGroup
        extends ArtifactGroup
{

    public String getArtifactId()
    {
        return getName();
    }

}
