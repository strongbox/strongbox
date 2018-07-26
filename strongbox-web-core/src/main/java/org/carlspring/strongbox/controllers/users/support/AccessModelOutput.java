package org.carlspring.strongbox.controllers.users.support;

import org.carlspring.strongbox.users.domain.AccessModel;

import java.util.*;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Przemyslaw Fusik
 */
public class AccessModelOutput
{

    @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
    private List<PathPrivilege> repositoryPrivileges = new ArrayList<>();

    @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
    private List<PathPrivilege> urlToPrivileges = new ArrayList<>();

    @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
    private List<PathPrivilege> wildCardPrivileges = new ArrayList<>();

    public AccessModelOutput()
    {

    }

    public AccessModelOutput(final AccessModel accessModel)
    {
        if (accessModel != null)
        {
            for (Map.Entry<String, Collection<String>> entry : accessModel.getRepositoryPrivileges().entrySet())
            {
                repositoryPrivileges.add(new PathPrivilege(entry.getKey(), entry.getValue()));
            }

            for (Map.Entry<String, Collection<String>> entry : accessModel.getUrlToPrivilegesMap().entrySet())
            {
                urlToPrivileges.add(new PathPrivilege(entry.getKey(), entry.getValue()));
            }

            for (Map.Entry<String, Collection<String>> entry : accessModel.getWildCardPrivilegesMap().entrySet())
            {
                wildCardPrivileges.add(new PathPrivilege(entry.getKey(), entry.getValue()));
            }
        }
    }

    public List<PathPrivilege> getRepositoryPrivileges()
    {
        return repositoryPrivileges;
    }

    public List<PathPrivilege> getUrlToPrivileges()
    {
        return urlToPrivileges;
    }

    public List<PathPrivilege> getWildCardPrivileges()
    {
        return wildCardPrivileges;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("AccessModelOutput {\n");
        sb.append(" repositoryPrivileges=")
          .append(repositoryPrivileges);
        sb.append(",\n urlToPrivileges=")
          .append(urlToPrivileges);
        sb.append(",\n wildCardPrivileges=")
          .append(wildCardPrivileges);
        sb.append("\n}");
        return sb.toString();
    }
}
