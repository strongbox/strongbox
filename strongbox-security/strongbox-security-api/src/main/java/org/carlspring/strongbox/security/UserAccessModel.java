package org.carlspring.strongbox.security;

import javax.xml.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Objects;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
@XmlRootElement(name = "features")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserAccessModel
{

    @XmlElement(name = "storage")
    @XmlElementWrapper(name = "storages")
    private Set<UserStorage> storages = new LinkedHashSet<>();

    public UserAccessModel()
    {
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccessModel userAccessModel = (UserAccessModel) o;
        return Objects.equal(storages, userAccessModel.storages);
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
        final StringBuilder sb = new StringBuilder("UserAccessModel{");
        sb.append("storages=")
          .append(storages);
        sb.append('}');
        return sb.toString();
    }
}
