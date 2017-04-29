package org.carlspring.strongbox.security;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Objects;

/**
 * @author Alex Oreshkevich
 */
@XmlRootElement(name = "path-permissions")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserPathPermissions
        extends GenericEntity
{

    @XmlElement(name = "path")
    private Set<UserPathPermission> pathPermissions;

    public UserPathPermissions()
    {
        pathPermissions = new LinkedHashSet<>();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPathPermissions that = (UserPathPermissions) o;
        return Objects.equal(pathPermissions, that.pathPermissions);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(pathPermissions);
    }

    public Set<UserPathPermission> getPathPermissions()
    {
        if (pathPermissions == null)
        {
            pathPermissions = new LinkedHashSet<>();
        }

        return pathPermissions;
    }

    public void setPathPermissions(Set<UserPathPermission> pathPermissions)
    {
        this.pathPermissions = pathPermissions;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("UserPathPermissions{");
        sb.append("pathPermissions=")
          .append(pathPermissions);
        sb.append('}');
        return sb.toString();
    }
}
