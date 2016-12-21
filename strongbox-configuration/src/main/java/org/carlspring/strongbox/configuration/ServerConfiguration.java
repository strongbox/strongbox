package org.carlspring.strongbox.configuration;

import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author mtodorov
 */
@XmlAccessorType(XmlAccessType.FIELD)
@MappedSuperclass
@Inheritance
public abstract class ServerConfiguration
        implements Serializable
{

    @Id
    protected String databaseId;

    /**
     * Added to avoid a runtime error whereby the detachAll property is checked for existence but not actually used.
     */
    @JsonIgnore
    protected String detachAll;

    @Version
    @JsonIgnore
    protected Long dbVersion;

    public ServerConfiguration()
    {
    }

    public String getDatabaseId()
    {
        return databaseId;
    }

    public void setDatabaseId(String databaseId)
    {
        this.databaseId = databaseId;
    }
}
