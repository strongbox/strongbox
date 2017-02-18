package org.carlspring.strongbox.data.domain;

import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.orientechnologies.orient.core.annotation.OVersion;

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

    // objectId may differ's because of internal OrientDB layout
    // at the first time it will be something like #-1:-2
    // and then this object will be placed in some cluster in async way
    // and it will have different objectId
    @Id
    @JsonIgnore
    protected String objectId;

    /**
     * Added to avoid a runtime error whereby the detachAll property is checked for existence but not actually used.
     */
    @JsonIgnore
    protected String detachAll;

    @OVersion
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
