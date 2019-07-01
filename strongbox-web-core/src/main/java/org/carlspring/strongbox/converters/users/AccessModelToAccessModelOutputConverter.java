package org.carlspring.strongbox.converters.users;

import org.carlspring.strongbox.controllers.users.support.AccessModelOutput;
import org.carlspring.strongbox.controllers.users.support.RepositoryAccessModelOutput;
import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.dto.AccessModelData;
import org.carlspring.strongbox.users.dto.PathPrivelegiesData;
import org.carlspring.strongbox.users.dto.RepositoryPrivilegesData;
import org.carlspring.strongbox.users.dto.StoragePrivilegesData;

import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Przemyslaw Fusik
 */
public enum AccessModelToAccessModelOutputConverter
        implements Converter<AccessModel, AccessModelOutput>
{
    INSTANCE;
    
    @Override
    public AccessModelOutput convert(final AccessModel source)
    {
        if (source == null)
        {
            return null;
        }

        AccessModelOutput result = new AccessModelOutput();
        for (StoragePrivilegesData storage : source.getStorageAuthorities())
        {
            for (RepositoryPrivilegesData repository : storage.getRepositories())
            {
                if (CollectionUtils.isNotEmpty(repository.getRepositoryPrivileges()))
                {
                    RepositoryAccessModelOutput repositoryAccess = getRepositoryAccessOrAddNewOne(result,
                                                                                                  storage.getStorageId(),
                                                                                                  repository.getRepositoryId(),
                                                                                                  null,
                                                                                                  false);
                    repositoryAccess.getPrivileges()
                                    .addAll(repository.getRepositoryPrivileges()
                                                      .stream()
                                                      .map(p -> p.name())
                                                      .collect(Collectors.toSet()));
                }
                for (PathPrivelegiesData pathPrivilege : repository.getPathPrivileges())
                {
                    RepositoryAccessModelOutput repositoryAccess = getRepositoryAccessOrAddNewOne(result,
                                                                                                  storage.getStorageId(),
                                                                                                  repository.getRepositoryId(),
                                                                                                  pathPrivilege.getPath(),
                                                                                                  pathPrivilege.isWildcard());

                    repositoryAccess.getPrivileges().addAll(pathPrivilege.getPrivileges()
                                                                         .stream()
                                                                         .map(p -> p.name())
                                                                         .collect(Collectors.toSet()));
                }
            }
        }

        return result;
    }

    private RepositoryAccessModelOutput getRepositoryAccessOrAddNewOne(final AccessModelOutput result,
                                                                       final String storageId,
                                                                       final String repositoryId,
                                                                       final String path,
                                                                       final boolean wildcard)
    {
        return result.getRepositoryAccess(storageId,
                                          repositoryId,
                                          path,
                                          wildcard)
                     .orElseGet(() ->
                                {
                                    RepositoryAccessModelOutput repositoryAccess = new RepositoryAccessModelOutput();
                                    repositoryAccess.setRepositoryId(repositoryId);
                                    repositoryAccess.setStorageId(storageId);
                                    repositoryAccess.setPath(path);
                                    repositoryAccess.setWildcard(wildcard);
                                    result.getRepositoriesAccess().add(repositoryAccess);
                                    return repositoryAccess;
                                });
    }
}
