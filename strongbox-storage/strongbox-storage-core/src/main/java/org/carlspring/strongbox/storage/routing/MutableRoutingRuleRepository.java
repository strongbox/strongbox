package org.carlspring.strongbox.storage.routing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Przemyslaw Fusik
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "repository")
public class MutableRoutingRuleRepository
        implements Serializable
{

    @XmlAttribute(name = "storage-id")
    private String storageId;

    @XmlAttribute(name = "repository-id")
    private String repositoryId;

    public MutableRoutingRuleRepository()
    {
    }

    public MutableRoutingRuleRepository(String storageId,
                                        String repositoryId)
    {
        this.storageId = storageId;
        this.repositoryId = repositoryId;
    }

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }
}
