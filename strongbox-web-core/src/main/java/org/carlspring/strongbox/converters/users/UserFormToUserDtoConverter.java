package org.carlspring.strongbox.converters.users;

import org.carlspring.strongbox.forms.users.UserForm;
import org.carlspring.strongbox.users.dto.UserDto;

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
        user.setRoles(userForm.getRoles());
        user.setSecurityTokenKey(userForm.getSecurityTokenKey());

        return user;
    }
}
