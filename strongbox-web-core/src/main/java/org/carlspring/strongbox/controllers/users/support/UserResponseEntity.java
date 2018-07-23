package org.carlspring.strongbox.controllers.users.support;

import org.carlspring.strongbox.authorization.domain.Role;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Steve Todorov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseEntity
{

    @JsonProperty("user")
    private UserOutput user;

    @JsonProperty("assignableRoles")
    private ArrayList<AssignableRoleOutput> assignableRoles;

    public UserResponseEntity()
    {

    }

    public UserResponseEntity(UserOutput user)
    {
        this.setUser(user);
    }

    public UserOutput getUser()
    {
        return user;
    }

    public void setUser(UserOutput user)
    {
        this.user = user;
    }

    public ArrayList<AssignableRoleOutput> getAssignableRoles()
    {
        return assignableRoles;
    }

    @JsonIgnore
    public void setAssignableRoles(Set<Role> assignableRoles)
    {
        this.assignableRoles = assignableRoles.stream()
                                              .sorted(Comparator.comparing(Role::getName))
                                              .map(AssignableRoleOutput::fromRole)
                                              .collect(Collectors.toCollection(ArrayList::new));
    }

    public void setAssignableRoles(ArrayList<AssignableRoleOutput> assignableRoles)
    {
        this.assignableRoles = assignableRoles;
    }
}
