package org.carlspring.strongbox.converters.users;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;
import org.carlspring.strongbox.forms.users.AccessModelForm;
import org.carlspring.strongbox.forms.users.RepositoryAccessModelForm;
import org.carlspring.strongbox.users.dto.UserAccessModelDto;
import org.carlspring.strongbox.users.dto.UserPathPrivilegesDto;
import org.carlspring.strongbox.users.dto.UserRepositoryDto;
import org.carlspring.strongbox.users.dto.UserStorageDto;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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

        for (RepositoryAccessModelForm repositoryAccess : accessModelForm.getRepositoriesAccess())
        {
            UserStorageDto storage = userAccessModelDto.getStorage(repositoryAccess.getStorageId())
                                                       .orElseGet(
                                                               () ->
                                                               {
                                                                   UserStorageDto userStorageDto = new UserStorageDto();
                                                                   userStorageDto.setStorageId(
                                                                           repositoryAccess.getStorageId());
                                                                   userAccessModelDto.getStorages().add(userStorageDto);
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
                repository.getRepositoryPrivileges().addAll(pullPrivilegeDtos(repositoryAccess));
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
            pathPrivileges.getPrivileges().addAll(pullPrivilegeDtos(repositoryAccess));

        }
        return userAccessModelDto;
    }

    private Collection<PrivilegeDto> pullPrivilegeDtos(final RepositoryAccessModelForm repositoryAccess)
    {
        return repositoryAccess.getPrivileges()
                               .stream()
                               .map(p -> new PrivilegeDto(p, null))
                               .collect(Collectors.toList());
    }
}
