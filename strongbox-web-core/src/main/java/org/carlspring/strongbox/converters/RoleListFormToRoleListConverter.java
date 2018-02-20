package org.carlspring.strongbox.converters;

import org.carlspring.strongbox.forms.RoleForm;
import org.carlspring.strongbox.forms.RoleListForm;
import org.carlspring.strongbox.security.Role;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 */
public class RoleListFormToRoleListConverter
        implements Converter<RoleListForm, List<Role>>
{

    @Override
    public List<Role> convert(RoleListForm roleListForm)
    {
        List<Role> roleList = new ArrayList<>();
        for (RoleForm roleForm : roleListForm.getRoles())
        {
            Role role = new Role(roleForm.getName(), roleForm.getDescription());
            role.setRepository(roleForm.getRepository());
            role.setPrivileges(roleForm.getPrivileges());
            roleList.add(role);
        }

        return roleList;
    }
}