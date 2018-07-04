package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.persistence.Entity;
import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
@Entity
public class ArtifactArchiveListing
        extends GenericEntity
{

    private Set<String> filenames;

    public Set<String> getFilenames()
    {
        return filenames;
    }

    public void setFilenames(final Set<String> filenames)
    {
        this.filenames = filenames;
    }
}
