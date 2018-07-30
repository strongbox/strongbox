package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.forms.configuration.RepositoryForm;
import org.carlspring.strongbox.forms.configuration.StorageForm;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.MutableRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Przemyslaw Fusik
 */
public enum StorageFormToMutableStorageConverter
        implements Converter<StorageForm, MutableStorage>
{
    INSTANCE;

    @Override
    public MutableStorage convert(final StorageForm source)
    {
        MutableStorage result = new MutableStorage();
        result.setBasedir(source.getBasedir());
        result.setId(source.getId());
        List<RepositoryForm> repositories = source.getRepositories();
        if (repositories != null)
        {
            Map<String, MutableRepository> internalMap = new LinkedHashMap<>();
            result.setRepositories(internalMap);
            repositories.stream()
                        .map(RepositoryFormToMutableRepositoryConverter.INSTANCE::convert)
                        .forEach(mr -> internalMap.put(mr.getId(), mr));
        }
        return result;
    }
}
