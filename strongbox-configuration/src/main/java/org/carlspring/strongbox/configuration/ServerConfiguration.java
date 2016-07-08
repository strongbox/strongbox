package org.carlspring.strongbox.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * @author mtodorov
 */
@XmlAccessorType(XmlAccessType.FIELD)
@MappedSuperclass
@Inheritance
public abstract class ServerConfiguration<T>
        implements Serializable
{

    @Id
    protected String id;

    /**
     * Added to avoid a runtime error whereby the detachAll property is checked for existence but not actually used.
     */
    @JsonIgnore
    protected String detachAll;

    @Version
    @JsonIgnore
    protected Long version;


    public ServerConfiguration()
    {
    }

}
