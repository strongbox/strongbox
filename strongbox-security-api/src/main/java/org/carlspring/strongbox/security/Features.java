package org.carlspring.strongbox.security;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

/**
 * @author Alex Oreshkevich
 */
@XmlRootElement(name = "features")
@XmlAccessorType(XmlAccessType.FIELD)
public class Features
        extends GenericEntity
{

    @XmlElement
    private UserStorages storages;

    public Features()
    {
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Features features = (Features) o;
        return Objects.equal(storages, features.storages);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(storages);
    }

    public UserStorages getStorages()
    {
        return storages;
    }

    public void setStorages(UserStorages storages)
    {
        this.storages = storages;
    }


    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Features{");
        sb.append("storages=")
          .append(storages);
        sb.append('}');
        return sb.toString();
    }
}
