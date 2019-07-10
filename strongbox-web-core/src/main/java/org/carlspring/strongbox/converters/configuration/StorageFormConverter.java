package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.forms.configuration.RepositoryForm;
import org.carlspring.strongbox.forms.configuration.StorageForm;
import org.carlspring.strongbox.storage.StorageDto;
import org.carlspring.strongbox.storage.repository.RepositoryDto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Przemyslaw Fusik
 */
public enum StorageFormConverter
        implements Converter<StorageForm, StorageDto>
{
    INSTANCE;

    @Override
    public StorageDto convert(final StorageForm source)
    {
        StorageDto result = new StorageDto();
        result.setBasedir(source.getBasedir());
        result.setId(source.getId());
        
        List<RepositoryForm> repositories = source.getRepositories();
        if (repositories != null)
        {
            Map<String, RepositoryDto> internalMap = new LinkedHashMap<>();
            result.setRepositories(internalMap);
            repositories.stream()
                        .map(RepositoryFormConverter.INSTANCE::convert)
                        .forEach(mr -> internalMap.put(mr.getId(), mr));
        }
        return result;
    }
}
