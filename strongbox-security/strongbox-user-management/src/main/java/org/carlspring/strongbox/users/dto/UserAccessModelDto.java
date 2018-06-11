package org.carlspring.strongbox.users.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Objects;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
@XmlRootElement(name = "features")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserAccessModelDto
{

    @XmlElement(name = "storage")
    @XmlElementWrapper(name = "storages")
    private Set<UserStorageDto> storages = new LinkedHashSet<>();

    public UserAccessModelDto()
    {
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccessModelDto userAccessModel = (UserAccessModelDto) o;
        return Objects.equal(storages, userAccessModel.storages);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(storages);
    }

    public Set<UserStorageDto> getStorages()
    {
        return storages;
    }

    public void setStorages(Set<UserStorageDto> storages)
    {
        this.storages = storages;
    }

    public UserStorageDto putIfAbsent(String storageId,
                                      UserStorageDto storage)
    {
        storage.setStorageId(storageId);
        if (storages == null)
        {
            storages = new HashSet<>();
        }
        UserStorageDto item = storages.stream().filter(stor -> storageId.equals(stor.getStorageId())).findFirst().orElse(
                storage);
        storages.add(item);
        return item;
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
