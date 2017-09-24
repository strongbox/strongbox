package org.carlspring.strongbox.controllers.users;

import org.carlspring.strongbox.users.domain.User;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Przemyslaw Fusik
 * @JsonInclude used because org.carlspring.strongbox.users.domain.User is annotated with it
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class UserInput
        extends BaseUserDto
{

    private String password;

    static UserInput fromUser(User user)
    {
        UserInput dto = new UserInput();
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setEnabled(user.isEnabled());
        dto.setRoles(user.getRoles());
        dto.setAccessModel(user.getAccessModel());
        dto.setSecurityTokenKey(user.getSecurityTokenKey());
        return dto;
    }

    User asUser()
    {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEnabled(enabled);
        user.setRoles(roles);
        user.setAccessModel(accessModel);
        user.setSecurityTokenKey(securityTokenKey);
        return user;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
