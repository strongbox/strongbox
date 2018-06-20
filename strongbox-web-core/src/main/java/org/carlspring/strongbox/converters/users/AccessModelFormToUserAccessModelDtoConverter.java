package org.carlspring.strongbox.converters.users;

import org.carlspring.strongbox.forms.users.AccessModelForm;
import org.carlspring.strongbox.users.dto.UserAccessModelDto;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 * @author Przemyslaw Fusik
 */
public class AccessModelFormToUserAccessModelDtoConverter
        implements Converter<AccessModelForm, UserAccessModelDto>
{

    @Override
    public UserAccessModelDto convert(AccessModelForm accessModelForm)
    {
        return accessModelForm.toDto();
    }
}
