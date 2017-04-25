package org.carlspring.strongbox.security;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;

import com.google.common.base.Objects;

/**
 * @author Alex Oreshkevich
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class UserPathPermission
        implements Serializable
{

    @XmlValue
    private String path;

    @XmlAttribute(name = "permission")
    private String permission;

    public UserPathPermission()
    {
        // assign default attribute value
        permission = "rw";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPathPermission that = (UserPathPermission) o;
        return Objects.equal(path, that.path) &&
               Objects.equal(permission, that.permission);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(path, permission);
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getPermission()
    {
        return permission;
    }

    public void setPermission(String permission)
    {
        this.permission = permission;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("UserPathPermission{");
        sb.append("path='")
          .append(path)
          .append('\'');
        sb.append(", permission='")
          .append(permission)
          .append('\'');
        sb.append('}');
        return sb.toString();
    }
}
