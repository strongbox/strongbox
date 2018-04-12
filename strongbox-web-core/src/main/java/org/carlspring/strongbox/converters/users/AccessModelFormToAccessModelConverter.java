package org.carlspring.strongbox.converters.users;

import org.carlspring.strongbox.forms.users.AccessModelForm;
import org.carlspring.strongbox.users.domain.AccessModel;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 */
public class AccessModelFormToAccessModelConverter
        implements Converter<AccessModelForm, AccessModel>
{

    @Override
    public AccessModel convert(AccessModelForm accessModelForm)
    {
        AccessModel accessModel = new AccessModel();
        accessModel.setRepositoryPrivileges(accessModelForm.getRepositoryPrivileges());
        accessModel.setUrlToPrivilegesMap(accessModelForm.getUrlToPrivilegesMap());
        accessModel.setWildCardPrivilegesMap(accessModelForm.getWildCardPrivilegesMap());
        return accessModel;
    }
}
