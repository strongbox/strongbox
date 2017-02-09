package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.data.domain.GenericEntity;

import com.google.common.base.Objects;

/**
 * @author Alex Oreshkevich
 */
public class BinaryConfiguration
        extends GenericEntity
{

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
        sb.append("data='")
          .append(data)
          .append('\'');
        sb.append('}');

        return sb.toString();
    }

}
