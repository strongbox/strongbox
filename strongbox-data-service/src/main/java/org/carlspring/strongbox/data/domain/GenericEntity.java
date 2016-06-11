package org.carlspring.strongbox.data.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;

/**
 * Base class for all entity classes in OrientDB.
 *
 * @author Alex Oreshkevich
 */
@MappedSuperclass
@Inheritance
public abstract class GenericEntity
        implements Serializable
{

    @Id
    protected String id;

    /**
     * Added to avoid a runtime error whereby the detachAll property is checked for existence but not actually used.
     */
    @JsonIgnore
    protected String detachAll;

    @Version
    @JsonIgnore
    protected Long version;

    public GenericEntity()
    {
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

}
