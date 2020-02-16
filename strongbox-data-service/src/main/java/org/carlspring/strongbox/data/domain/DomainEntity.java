package org.carlspring.strongbox.data.domain;

import org.neo4j.ogm.annotation.Id;

/**
 * @author sbespalov
 *
 */
public class DomainEntity implements DomainObject
{

    @Id
    private String uuid;

    @Override
    public String getUuid()
    {
        return uuid;
    }

    @Override
    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (!(obj instanceof DomainEntity))
        {
            return false;
        }

        DomainEntity that = (DomainEntity) obj;
        if (this.uuid == null)
        {
            return false;
        }

        return this.uuid.equals(that.uuid);
    }

    @Override
    public int hashCode()
    {
        if (uuid == null)
        {
            return super.hashCode();
        }
        return uuid.hashCode();
    }

}
