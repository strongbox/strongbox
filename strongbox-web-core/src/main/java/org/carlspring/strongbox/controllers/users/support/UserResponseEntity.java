package org.carlspring.strongbox.controllers.users.support;

import org.carlspring.strongbox.authorization.domain.Role;
import org.carlspring.strongbox.users.domain.Privileges;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Steve Todorov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseEntity
{
    private UserOutput user;

    private List<AssignableRoleOutput> assignableRoles;

    private List<Privileges> assignablePrivileges;

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

    public List<AssignableRoleOutput> getAssignableRoles()
    {
        return assignableRoles;
    }

    public List<Privileges> getAssignablePrivileges()
    {
        return assignablePrivileges;
    }

    public void setAssignableRoles(Set<Role> assignableRoles)
    {
        this.assignableRoles = assignableRoles.stream()
                                              .sorted(Comparator.comparing(Role::getName))
                                              .map(AssignableRoleOutput::fromRole)
                                              .collect(Collectors.toCollection(ArrayList::new));
    }

    public void setAssignablePrivileges(List<Privileges> assignablePrivileges)
    {
        this.assignablePrivileges = assignablePrivileges;
    }
}
