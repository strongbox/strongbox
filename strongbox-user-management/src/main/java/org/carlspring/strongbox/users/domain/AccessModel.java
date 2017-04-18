package org.carlspring.strongbox.users.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Objects;

/**
 * Additional user settings (for example, per-repository access settings).
 *
 * @author Alex Oreshkevich
 */
public class AccessModel
        extends GenericEntity
{

    // maps URL tails including wildcards for regular expressions on set of privileges that was assigned
    // example:
    //      key:    storage0/act-releases-1/pro/redsoft/bar/.*
    //      value:  {ARTIFACTS_VIEW, ARTIFACTS_RESOLVE, ARTIFACTS_DEPLOY}
    private Map<String, Collection<String>> urlToPrivilegesMap;

    public AccessModel()
    {
        urlToPrivilegesMap = new HashMap<>();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessModel that = (AccessModel) o;
        return Objects.equal(urlToPrivilegesMap, that.urlToPrivilegesMap);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(urlToPrivilegesMap);
    }

    public Map<String, Collection<String>> getUrlToPrivilegesMap()
    {
        return urlToPrivilegesMap;
    }

    public void setUrlToPrivilegesMap(Map<String, Collection<String>> urlToPrivilegesMap)
    {
        this.urlToPrivilegesMap = urlToPrivilegesMap;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("AccessModel{");
        sb.append("urlToPrivilegesMap=")
          .append(urlToPrivilegesMap);
        sb.append('}');
        return sb.toString();
    }
}
