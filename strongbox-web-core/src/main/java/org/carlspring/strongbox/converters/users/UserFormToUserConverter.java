package org.carlspring.strongbox.converters.users;

import org.carlspring.strongbox.forms.users.AccessModelForm;
import org.carlspring.strongbox.forms.users.UserForm;
import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.domain.User;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 */
public class UserFormToUserConverter
        implements Converter<UserForm, User>
{

    @Override
    public User convert(UserForm userForm)
    {
        User user = new User();
        user.setUsername(userForm.getUsername());
        user.setPassword(userForm.getPassword());
        user.setEnabled(userForm.isEnabled());
        user.setRoles(userForm.getRoles());
        user.setAccessModel(convertAccessModel(userForm.getAccessModel()));
        user.setSecurityTokenKey(userForm.getSecurityTokenKey());
        return user;
    }

    private AccessModel convertAccessModel(AccessModelForm accessModelForm)
    {
        AccessModel accessModel = null;
        if (accessModelForm != null)
        {
            accessModel = new AccessModel();
            accessModel.setRepositoryPrivileges(accessModelForm.getRepositoryPrivileges());
            accessModel.setUrlToPrivilegesMap(accessModelForm.getUrlToPrivilegesMap());
            accessModel.setWildCardPrivilegesMap(accessModelForm.getWildCardPrivilegesMap());
        }
        return accessModel;
    }
}
