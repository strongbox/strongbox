package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.forms.configuration.RepositoryForm;
import org.carlspring.strongbox.forms.configuration.StorageForm;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.MutableRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Przemyslaw Fusik
 */
public enum StorageFormConverter
        implements Converter<StorageForm, MutableStorage>
{
    INSTANCE;

    @Override
    public MutableStorage convert(final StorageForm source)
    {
        MutableStorage result = new MutableStorage();
        if (StringUtils.isNotBlank(source.getBasedir()))
        {
            result.setBasedir(source.getBasedir());
        }
        else
        {
            result.initDefaultBasedir(source.getId());
        }
        result.setId(source.getId());
        List<RepositoryForm> repositories = source.getRepositories();
        if (repositories != null)
        {
            Map<String, MutableRepository> internalMap = new LinkedHashMap<>();
            result.setRepositories(internalMap);
            repositories.stream()
                        .map(RepositoryFormConverter.INSTANCE::convert)
                        .forEach(mr -> internalMap.put(mr.getId(), mr));
        }
        return result;
    }
}
