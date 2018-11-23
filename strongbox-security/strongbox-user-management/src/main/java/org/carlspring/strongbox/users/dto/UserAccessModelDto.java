package org.carlspring.strongbox.users.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "features")
@XmlAccessorType(XmlAccessType.NONE)
public class UserAccessModelDto implements UserAccessModelReadContract
{

    @XmlElement(name = "storage")
    @XmlElementWrapper(name = "storages")
    private Set<UserStorageDto> storages = new LinkedHashSet<>();

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.users.dto.UserAccessModelReadContract#getStorages()
     */
    public Set<UserStorageDto> getStorages()
    {
        return storages;
    }

    public Optional<UserStorageDto> getStorage(final String storageId)
    {
        return storages.stream().filter(s -> s.getStorageId().equals(storageId)).findFirst();
    }
}
