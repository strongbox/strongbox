package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.persistence.Entity;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
@Entity
public class ArtifactArchiveListing
        extends GenericEntity
{

    private Set<String> filenames = new LinkedHashSet<>();

    public Set<String> getFilenames()
    {
        return filenames;
    }

    public void setFilenames(final Set<String> filenames)
    {
        this.filenames = filenames;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("ArtifactArchiveListing{");
        sb.append("filenames=").append(filenames);
        sb.append(", objectId='").append(objectId).append('\'');
        sb.append(", uuid='").append(uuid).append('\'');
        sb.append(", entityVersion=").append(entityVersion);
        sb.append('}');
        return sb.toString();
    }
}
