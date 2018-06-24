package org.carlspring.strongbox.users.dto;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Objects;

/**
 * @author Alex Oreshkevich
 */
@XmlRootElement(name = "path-permissions")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserPathPermissionsDto
{

    @XmlElement(name = "path")
    private Set<UserPathPermissionDto> pathPermissions;

    public UserPathPermissionsDto()
    {
        pathPermissions = new LinkedHashSet<>();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPathPermissionsDto that = (UserPathPermissionsDto) o;
        return Objects.equal(pathPermissions, that.pathPermissions);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(pathPermissions);
    }

    public Set<UserPathPermissionDto> getPathPermissions()
    {
        if (pathPermissions == null)
        {
            pathPermissions = new LinkedHashSet<>();
        }

        return pathPermissions;
    }

    public void setPathPermissions(Set<UserPathPermissionDto> pathPermissions)
    {
        this.pathPermissions = pathPermissions;
    }

    public UserPathPermissionDto putIfAbsent(final String path,
                                             final UserPathPermissionDto userPathPermission)
    {
        if (pathPermissions == null)
        {
            pathPermissions = new HashSet<>();
        }
        UserPathPermissionDto item = pathPermissions.stream().filter(
                pathPerm -> path.equals(pathPerm.getPath())).findFirst().orElse(userPathPermission);
        pathPermissions.add(item);
        return item;
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
