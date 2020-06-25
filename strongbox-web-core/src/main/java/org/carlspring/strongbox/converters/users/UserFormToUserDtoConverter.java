package org.carlspring.strongbox.converters.users;

import org.carlspring.strongbox.domain.UserRole;
import org.carlspring.strongbox.domain.UserRoleEntity;
import org.carlspring.strongbox.forms.users.UserForm;
import org.carlspring.strongbox.users.dto.UserDto;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 * @author Przemyslaw Fusik
 */
public enum UserFormToUserDtoConverter
        implements Converter<UserForm, UserDto>
{

    INSTANCE;

    @Override
    public UserDto convert(UserForm userForm)
    {
        UserDto user = new UserDto();
        user.setUsername(userForm.getUsername());
        user.setPassword(userForm.getPassword());
        user.setEnabled(userForm.isEnabled());
        Set<UserRole> roles = userForm.getRoles()
                                      .stream()
                                      .map(role -> new UserRoleEntity(role))
                                      .collect(Collectors.toSet());
        user.setRoles(roles);
        user.setSecurityTokenKey(userForm.getSecurityTokenKey());

        return user;
    }
}
