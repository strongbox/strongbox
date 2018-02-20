package org.carlspring.strongbox.forms;

import org.carlspring.strongbox.validation.UniqueRoleName;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Sets;

/**
 * @author Pablo Tirado
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleForm
{

    @NotEmpty(message = "This field must not be empty.")
    @UniqueRoleName(message = "Role is already registered.")
    private String name;

    private String description;

    /**
     * The repository this role is associated with.
     */
    private String repository;

    private Set<String> privileges = Sets.newHashSet();

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getRepository()
    {
        return repository;
    }

    public void setRepository(String repository)
    {
        this.repository = repository;
    }

    public Set<String> getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(Set<String> privileges)
    {
        this.privileges = privileges;
    }
}
