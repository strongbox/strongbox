package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * @author Przemyslaw Fusik
 * @see <a href="https://youtrack.carlspring.org/issue/SB-984">SB-984</a>
 */
@Entity
public class ArtifactGroup
        extends GenericEntity
{

    @ManyToOne(cascade = CascadeType.ALL)
    private ArtifactGroupIdentifier groupIdentifier;
}
