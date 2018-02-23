package org.carlspring.strongbox.converters;

import org.carlspring.strongbox.forms.PrivilegeForm;
import org.carlspring.strongbox.forms.PrivilegeListForm;
import org.carlspring.strongbox.security.Privilege;
import org.carlspring.strongbox.security.Role;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * @author Pablo Tirado
 */
public class PrivilegeListFormToPrivilegeListConverter
        implements Converter<PrivilegeListForm, List<Privilege>>
{

    @Override
    public List<Privilege> convert(PrivilegeListForm privilegeListForm)
    {
        List<Privilege> privilegeList = new ArrayList<>();
        for (PrivilegeForm privilegeForm : privilegeListForm.getPrivileges())
        {
            Privilege privilege = new Privilege(privilegeForm.getName(), privilegeForm.getDescription());
            privilegeList.add(privilege);
        }

        return privilegeList;
    }
}
