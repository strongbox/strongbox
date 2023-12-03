package org.carlspring.strongbox.converters;

import org.carlspring.strongbox.authorization.dto.RoleDto;
import org.carlspring.strongbox.converters.users.AccessModelFormToUserAccessModelDtoConverter;
import org.carlspring.strongbox.forms.RoleForm;
import org.carlspring.strongbox.users.dto.AccessModelDto;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 */
public class RoleFormToRoleConverter
        implements Converter<RoleForm, RoleDto>
{

    public static final RoleFormToRoleConverter INSTANCE = new RoleFormToRoleConverter();

    @Override
    public RoleDto convert(RoleForm roleForm)
    {
        AccessModelDto accessModelDto = AccessModelFormToUserAccessModelDtoConverter.INSTANCE.convert(roleForm.getAccessModel());
        RoleDto role = new RoleDto(roleForm.getName(), roleForm.getDescription(), accessModelDto);

        return role;
    }
    
}
