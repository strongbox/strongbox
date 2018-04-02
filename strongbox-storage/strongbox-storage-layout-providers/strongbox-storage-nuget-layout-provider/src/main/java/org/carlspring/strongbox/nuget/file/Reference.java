package org.carlspring.strongbox.nuget.file;

import java.io.Serializable;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAttribute;

public class Reference
        implements Serializable
{

    @XmlAttribute(
            name = "file"
    )
    private String file;

    public Reference()
    {
    }

    public Reference setFile(String file)
    {
        this.file = file;
        return this;
    }

    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (!(obj instanceof Reference))
        {
            return false;
        }
        else
        {
            Reference o = (Reference) obj;
            return Objects.equals(o.file, this.file);
        }
    }

    public int hashCode()
    {
        return Objects.hash(new Object[]{ this.file });
    }
}
