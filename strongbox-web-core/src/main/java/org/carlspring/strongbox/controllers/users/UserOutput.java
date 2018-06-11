package org.carlspring.strongbox.controllers.users;

import org.carlspring.strongbox.users.domain.MutableAccessModel;
import org.carlspring.strongbox.users.domain.User;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 * @JsonInclude used because org.carlspring.strongbox.users.domain.User is annotated with it
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserOutput
        extends BaseUserDto
{

    static UserOutput fromUser(User user)
    {
        final UserOutput output = new UserOutput();
        output.setEnabled(user.isEnabled());
        output.setRoles(user.getRoles());
        output.setUsername(user.getUsername());
        output.setAccessModel(new MutableAccessModel(user.getAccessModel()));
        output.setSecurityTokenKey(user.getSecurityTokenKey());
        return output;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("UserOutput{");
        sb.append("username='").append(username).append('\'');
        sb.append(", enabled=").append(enabled);
        sb.append(", roles=").append(roles);
        sb.append(", securityTokenKey='").append(securityTokenKey).append('\'');
        sb.append(", accessModel=").append(accessModel);
        sb.append('}');
        return sb.toString();
    }
}
