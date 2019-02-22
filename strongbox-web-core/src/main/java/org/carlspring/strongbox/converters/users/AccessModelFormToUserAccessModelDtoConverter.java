package org.carlspring.strongbox.converters.users;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.strongbox.forms.users.AccessModelForm;
import org.carlspring.strongbox.forms.users.RepositoryAccessModelForm;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserPathPrivilegesDto;
import org.carlspring.strongbox.users.dto.UserRepositoryDto;
import org.carlspring.strongbox.users.dto.UserStorageDto;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 * @author Przemyslaw Fusik
 */
public enum AccessModelFormToUserAccessModelDtoConverter
        implements Converter<AccessModelForm, UserAccessModelDto>
{

    INSTANCE;

    @Override
    public UserAccessModelDto convert(AccessModelForm accessModelForm)
    {
        if (accessModelForm == null)
        {
            return null;
        }
        
        UserAccessModelDto userAccessModelDto = new UserAccessModelDto();
        accessModelForm.getApiAcess()
                       .stream()
                       .map(p -> Privileges.valueOf(p))
                       .forEach(p -> userAccessModelDto.getApiAuthorities().add(p));
        
        for (RepositoryAccessModelForm repositoryAccess : accessModelForm.getRepositoriesAccess())
        {
            UserStorageDto storage = userAccessModelDto.getStorage(repositoryAccess.getStorageId())
                                                       .orElseGet(
                                                               () ->
                                                               {
                                                                   UserStorageDto userStorageDto = new UserStorageDto();
                                                                   userStorageDto.setStorageId(
                                                                           repositoryAccess.getStorageId());
                                                                   userAccessModelDto.getStorageAuthorities().add(userStorageDto);
                                                                   return userStorageDto;
                                                               });

            UserRepositoryDto repository = storage.getRepository(repositoryAccess.getRepositoryId())
                                                  .orElseGet(
                                                          () ->
                                                          {
                                                              UserRepositoryDto userRepositoryDto = new UserRepositoryDto();
                                                              userRepositoryDto.setRepositoryId(
                                                                      repositoryAccess.getRepositoryId());
                                                              storage.getRepositories().add(userRepositoryDto);
                                                              return userRepositoryDto;
                                                          });

            if (StringUtils.isBlank(repositoryAccess.getPath()))
            {
                repository.getRepositoryPrivileges().addAll(pullPrivileges(repositoryAccess));
                continue;
            }

            UserPathPrivilegesDto pathPrivileges = repository.getPathPrivilege(repositoryAccess.getPath(),
                                                                               repositoryAccess.isWildcard())
                                                             .orElseGet(
                                                                     () ->
                                                                     {
                                                                         UserPathPrivilegesDto pathPrivilegesDto = new UserPathPrivilegesDto();
                                                                         pathPrivilegesDto.setPath(
                                                                                 repositoryAccess.getPath());
                                                                         pathPrivilegesDto.setWildcard(
                                                                                 repositoryAccess.isWildcard());
                                                                         repository.getPathPrivileges().add(
                                                                                 pathPrivilegesDto);
                                                                         return pathPrivilegesDto;
                                                                     });
            pathPrivileges.getPrivileges().addAll(pullPrivileges(repositoryAccess));

        }
        return userAccessModelDto;
    }

    private Collection<Privileges> pullPrivileges(final RepositoryAccessModelForm repositoryAccess)
    {
        return repositoryAccess.getPrivileges()
                               .stream()
                               .map(p -> Privileges.valueOf(p))
                               .collect(Collectors.toSet());
    }
}
