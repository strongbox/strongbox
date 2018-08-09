package org.carlspring.strongbox.domain;

import javax.persistence.Embeddable;
import javax.persistence.EntityManager;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
@Embeddable
public class ArtifactArchiveListing
        implements Serializable
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

    public ArtifactArchiveListing detach(EntityManager entityManager) {
        ArtifactArchiveListing result = ((OObjectDatabaseTx)entityManager.getDelegate()).detach(this, true);
        result.filenames = new HashSet<>(result.filenames);
        
        return result;
    }
    
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("ArtifactArchiveListing{");
        sb.append("filenames=").append(filenames);
        sb.append('}');
        return sb.toString();
    }
}
