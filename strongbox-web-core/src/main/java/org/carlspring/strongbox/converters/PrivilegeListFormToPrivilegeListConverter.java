package org.carlspring.strongbox.converters;

import org.carlspring.strongbox.forms.PrivilegeForm;
import org.carlspring.strongbox.forms.PrivilegeListForm;
import org.carlspring.strongbox.authorization.dto.PrivilegeDto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 */
public class PrivilegeListFormToPrivilegeListConverter
        implements Converter<PrivilegeListForm, List<PrivilegeDto>>
{

    @Override
    public List<PrivilegeDto> convert(PrivilegeListForm privilegeListForm)
    {
        List<PrivilegeDto> privilegeList = new ArrayList<>();
        for (PrivilegeForm privilegeForm : privilegeListForm.getPrivileges())
        {
            PrivilegeDto privilege = new PrivilegeDto(privilegeForm.getName(), privilegeForm.getDescription());
            privilegeList.add(privilege);
        }

        return privilegeList;
    }
}
