package org.carlspring.strongbox.converters.users;

import org.carlspring.strongbox.forms.users.AccessModelForm;
import org.carlspring.strongbox.forms.users.UserForm;
import org.carlspring.strongbox.users.domain.MutableAccessModel;
import org.carlspring.strongbox.users.domain.MutableUser;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 */
public class UserFormToUserConverter
        implements Converter<UserForm, MutableUser>
{

    @Override
    public MutableUser convert(UserForm userForm)
    {
        MutableUser user = new MutableUser();
        user.setUsername(userForm.getUsername());
        user.setPassword(userForm.getPassword());
        user.setEnabled(userForm.isEnabled());
        user.setRoles(userForm.getRoles());
        user.setAccessModel(convertAccessModel(userForm.getAccessModel()));
        user.setSecurityTokenKey(userForm.getSecurityTokenKey());
        return user;
    }

    private MutableAccessModel convertAccessModel(AccessModelForm accessModelForm)
    {
        MutableAccessModel accessModel = null;
        if (accessModelForm != null)
        {
            accessModel = new MutableAccessModel();
            accessModel.setRepositoryPrivileges(accessModelForm.getRepositoryPrivileges());
            accessModel.setUrlToPrivilegesMap(accessModelForm.getUrlToPrivilegesMap());
            accessModel.setWildCardPrivilegesMap(accessModelForm.getWildCardPrivilegesMap());
        }
        return accessModel;
    }
}
