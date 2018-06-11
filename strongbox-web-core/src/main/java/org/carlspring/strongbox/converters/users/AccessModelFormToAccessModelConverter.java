package org.carlspring.strongbox.converters.users;

import org.carlspring.strongbox.forms.users.AccessModelForm;
import org.carlspring.strongbox.users.domain.MutableAccessModel;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 */
public class AccessModelFormToAccessModelConverter
        implements Converter<AccessModelForm, MutableAccessModel>
{

    @Override
    public MutableAccessModel convert(AccessModelForm accessModelForm)
    {
        MutableAccessModel accessModel = new MutableAccessModel();
        accessModel.setRepositoryPrivileges(accessModelForm.getRepositoryPrivileges());
        accessModel.setUrlToPrivilegesMap(accessModelForm.getUrlToPrivilegesMap());
        accessModel.setWildCardPrivilegesMap(accessModelForm.getWildCardPrivilegesMap());
        return accessModel;
    }
}
