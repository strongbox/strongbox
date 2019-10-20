package org.carlspring.strongbox.data.domain;

import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.orientechnologies.orient.core.annotation.OId;
import com.orientechnologies.orient.core.annotation.OVersion;
import org.apache.commons.lang.StringUtils;

/**
 * Base class for all entities that have to be stored in OrientDB.
 * <p>
 * <b>Implementation notice</b>: don't declare variables with the same names as it's in this class ({@link #objectId},
 * {@link #uuid} etc.) It will hide that variables and change behaviour of persistence subsystem to unpredictable.
 *
 * @see {@link GenericEntityHook}
 *
 * @author Alex Oreshkevich
 * @author Sergey Bespalov
 */
@MappedSuperclass
@Inheritance
public abstract class GenericEntity
        implements Serializable
{
    /**
     * This is internal `OrientDB` object identifier which may differ's because of internal OrientDB layout. At the first
     * time it will be something like #-1:-2 and then this object will be placed in some cluster in async way and it
     * will have different objectId.
     */
    @Id
    @OId
    @JsonIgnore
    @XmlTransient
    protected String objectId;

    protected String uuid;

    @OVersion
    @JsonIgnore
    protected Long entityVersion;

    public GenericEntity()
    {
    }

    @XmlTransient
    public final String getObjectId()
    {
        return objectId;
    }

    public final void setObjectId(String objectId)
    {
        this.objectId = objectId;
    }

    @XmlTransient
    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    @JsonIgnore
    @XmlTransient
    public Long getEntityVersion()
    {
        return entityVersion;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (!(obj instanceof GenericEntity))
        {
            return false;
        }

        GenericEntity that = (GenericEntity) obj;
        if (this.objectId == null && that.objectId == null)
        {
            return false;
        }

        return StringUtils.equals(objectId, that.objectId);
    }

    @Override
    public int hashCode()
    {
        if (objectId == null)
        {
            return super.hashCode();
        }
        return objectId.hashCode();
    }

}
