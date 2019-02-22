package org.carlspring.strongbox.converters;

import org.carlspring.strongbox.forms.RoleForm;
import org.carlspring.strongbox.forms.RoleListForm;
import org.carlspring.strongbox.authorization.dto.RoleDto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 */
public class RoleListFormToRoleListConverter
        implements Converter<RoleListForm, List<RoleDto>>
{

    private RoleFormToRoleConverter roleFormToRoleConverter = new RoleFormToRoleConverter();
    
    @Override
    public List<RoleDto> convert(RoleListForm roleListForm)
    {
        List<RoleDto> roleList = new ArrayList<>();
        for (RoleForm roleForm : roleListForm.getRoles())
        {
            RoleDto role = roleFormToRoleConverter.convert(roleForm);
            roleList.add(role);
        }

        return roleList;
    }
    
}
