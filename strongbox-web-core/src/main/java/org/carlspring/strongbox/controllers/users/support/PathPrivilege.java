package org.carlspring.strongbox.controllers.users.support;

import java.util.Collection;

public class PathPrivilege
{

    private String path;
    private Collection<String> privileges;

    public PathPrivilege()
    {
    }

    public PathPrivilege(String path,
                         Collection<String> privileges)
    {
        this.path = path;
        this.privileges = privileges;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public Collection<String> getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(Collection<String> privileges)
    {
        this.privileges = privileges;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(PathPrivilege.class.getSimpleName() + " {");
        sb.append("path='")
          .append(path)
          .append('\'');
        sb.append(", privileges=")
          .append(privileges);
        sb.append('}');
        return sb.toString();
    }
}
