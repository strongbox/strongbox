package org.carlspring.strongbox.configuration;

import javax.persistence.Id;
import javax.persistence.Version;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;

/**
 * @author Alex Oreshkevich
 */
public class BinaryConfiguration
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

    private String data;

    public BinaryConfiguration()
    {
    }

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
    
        BinaryConfiguration that = (BinaryConfiguration) o;
    
        return Objects.equal(data, that.data);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(data);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("BinaryConfiguration{");
        sb.append("data='").append(data).append('\'');
        sb.append('}');
        
        return sb.toString();
    }
    
}
