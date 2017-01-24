package org.carlspring.strongbox.data.domain;

import java.io.Serializable;

import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base class for all entities that have to be stored in OrientDB.
 * <p>
 * <b>Implementation notice</b>: don't declare variables with the same names as it's in this class ({@link #objectId},
 * {@link #detachAll} etc.) It will hide that variables and change behaviour of persistence subsystem to unpredictable.
 *
 * @author Alex Oreshkevich
 */
@MappedSuperclass
@Inheritance
public abstract class GenericEntity
        implements Serializable
{

    @Id
    protected String objectId;

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

    public final String getObjectId()
    {
        return objectId;
    }

    public final void setObjectId(String objectId)
    {
        this.objectId = objectId;
    }

}
