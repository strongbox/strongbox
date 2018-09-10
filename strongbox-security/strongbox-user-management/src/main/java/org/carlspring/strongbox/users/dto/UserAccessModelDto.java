package org.carlspring.strongbox.users.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "features")
@XmlAccessorType(XmlAccessType.NONE)
public class UserAccessModelDto
{

    @XmlElement(name = "storage")
    @XmlElementWrapper(name = "storages")
    private Set<UserStorageDto> storages = new LinkedHashSet<>();

    public Set<UserStorageDto> getStorages()
    {
        return storages;
    }
}
