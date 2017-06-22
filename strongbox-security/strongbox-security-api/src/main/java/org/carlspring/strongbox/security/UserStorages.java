package org.carlspring.strongbox.security;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Objects;

/**
 * @author Alex Oreshkevich
 */
@XmlRootElement(name = "storages")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserStorages
        extends GenericEntity
{

    @XmlElement(name = "storage")
    private Set<UserStorage> storages = new LinkedHashSet<>();


    public UserStorages()
    {
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserStorages storages1 = (UserStorages) o;
        return Objects.equal(storages, storages1.storages);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(storages);
    }

    public Set<UserStorage> getStorages()
    {
        return storages;
    }

    public void setStorages(Set<UserStorage> storages)
    {
        this.storages = storages;
    }


    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Storages{");
        sb.append("storages=")
          .append(storages);
        sb.append('}');
        return sb.toString();
    }
}
