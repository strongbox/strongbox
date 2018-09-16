package org.carlspring.strongbox.converters.users;

import org.carlspring.strongbox.controllers.users.support.AccessModelOutput;
import org.carlspring.strongbox.controllers.users.support.RepositoryAccessModelOutput;
import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.domain.AccessModelPathPrivileges;
import org.carlspring.strongbox.users.domain.AccessModelRepository;
import org.carlspring.strongbox.users.domain.AccessModelStorage;

import org.apache.commons.collections.CollectionUtils;
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
        for (AccessModelStorage storage : source.getStorages())
        {
            for (AccessModelRepository repository : storage.getRepositories())
            {
                if (CollectionUtils.isNotEmpty(repository.getRepositoryPrivileges()))
                {
                    RepositoryAccessModelOutput repositoryAccess = getRepositoryAccessOrAddNewOne(result,
                                                                                                  storage.getStorageId(),
                                                                                                  repository.getRepositoryId(),
                                                                                                  null,
                                                                                                  false);
                    repositoryAccess.getPrivileges().addAll(repository.getRepositoryPrivileges());
                }
                for (AccessModelPathPrivileges pathPrivilege : repository.getPathPrivileges())
                {
                    RepositoryAccessModelOutput repositoryAccess = getRepositoryAccessOrAddNewOne(result,
                                                                                                  storage.getStorageId(),
                                                                                                  repository.getRepositoryId(),
                                                                                                  pathPrivilege.getPath(),
                                                                                                  pathPrivilege.isWildcard());

                    repositoryAccess.getPrivileges().addAll(pathPrivilege.getPrivileges());
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
