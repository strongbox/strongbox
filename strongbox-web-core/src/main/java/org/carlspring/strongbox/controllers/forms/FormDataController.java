package org.carlspring.strongbox.controllers.forms;

import org.carlspring.strongbox.authorization.service.AuthorizationConfigService;
import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.forms.configuration.MavenRepositoryConfigurationForm;
import org.carlspring.strongbox.forms.configuration.NugetRepositoryConfigurationForm;
import org.carlspring.strongbox.forms.configuration.RawRepositoryConfigurationForm;
import org.carlspring.strongbox.providers.datastore.StorageProviderEnum;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryStatusEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.security.AuthoritiesProvider;
import org.carlspring.strongbox.util.FieldSpy;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Przemyslaw Fusik
 */
@RestController
@RequestMapping("/api/formData")
@Api(value = "/api/formData")
public class FormDataController
        extends BaseController
{

    private static final String FILTER_PARAM_NAME = "filter";

    @Inject
    private AuthorizationConfigService authorizationConfigService;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private AuthoritiesProvider authoritiesProvider;

    @ApiOperation(value = "Used to retrieve all assignable user roles and privileges")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Collection of all assignable user roles and privileges") })
    @PreAuthorize("hasAuthority('CREATE_USER') or hasAuthority('UPDATE_USER')")
    @GetMapping(value = "/userFields",
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getUserFields()
    {
        return ResponseEntity.ok(new FormDataValuesCollection(ImmutableList.of(
                FormDataValues.fromCollection("assignableRoles", authoritiesProvider.getAssignableRoles()),
                FormDataValues.fromCollection("assignablePrivileges", Arrays.asList(Privileges.values())))));
    }

    @ApiOperation(value = "Used to retrieve collection of storage form data")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Collection of storage form data") })
    @PreAuthorize("hasAuthority('CONFIGURATION_ADD_UPDATE_STORAGE') or hasAuthority('CONFIGURATION_ADD_UPDATE_REPOSITORY')")
    @GetMapping(value = "/storageFields",
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getStorageFields()
    {
        return ResponseEntity.ok(new FormDataValuesCollection(ImmutableList.of(
                FormDataValues.fromDescribableEnum("policy", RepositoryPolicyEnum.class),
                FormDataValues.fromDescribableEnum("status", RepositoryStatusEnum.class),
                FormDataValues.fromDescribableEnum("type", RepositoryTypeEnum.class),
                FormDataValues.fromDescribableEnum("implementation", StorageProviderEnum.class),
                FormDataValues.fromCollection("layout", Arrays.asList(
                        FormDataValues.fromCollection(Maven2LayoutProvider.ALIAS,
                                                      FieldSpy.getAllFieldsInfo(
                                                              MavenRepositoryConfigurationForm.class)),
                        FormDataValues.fromCollection(NugetLayoutProvider.ALIAS,
                                                      FieldSpy.getAllFieldsInfo(
                                                              NugetRepositoryConfigurationForm.class)),
                        FormDataValues.fromCollection(RawLayoutProvider.ALIAS,
                                                      FieldSpy.getAllFieldsInfo(RawRepositoryConfigurationForm.class))))
        )));
    }

    @ApiOperation(value = "Used to retrieve storage names")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Storage names") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_STORAGE_CONFIGURATION')")
    @GetMapping(value = "/storageNames", produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getStorageNames(@ApiParam(value = "Storage name filter")
                                          @RequestParam(value = FILTER_PARAM_NAME, required = false) String filter)
    {
        Set<String> storageNames = configurationManagementService.getConfiguration().getStorages().keySet();
        if (StringUtils.isNotBlank(filter))
        {
            storageNames = storageNames.stream().filter(name -> StringUtils.containsIgnoreCase(name, filter)).collect(
                    Collectors.toSet());
        }
        storageNames = ImmutableSet.copyOf(Iterables.limit(storageNames, 10));

        return ResponseEntity.ok(new FormDataValuesCollection(ImmutableList.of(
                FormDataValues.fromCollection("storageNames", storageNames))));
    }

    @ApiOperation(value = "Used to retrieve repository names by storageId")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Repository names by storageId") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_REPOSITORY')")
    @GetMapping(value = "/repositoryNames",
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getRepositoryNames(@ApiParam(value = "storageId")
                                             @RequestParam(value = "storageId") String storageId,
                                             @ApiParam(value = "Repository name filter")
                                             @RequestParam(value = FILTER_PARAM_NAME, required = false) String filter)
    {
        Set<String> repositoryNames = Collections.emptySet();
        Storage storage = configurationManagementService.getConfiguration().getStorage(storageId);
        if (storage != null)
        {
            repositoryNames = storage.getRepositories().keySet();
            if (StringUtils.isNotBlank(filter))
            {
                repositoryNames = repositoryNames.stream().filter(
                        name -> StringUtils.containsIgnoreCase(name, filter)).collect(
                        Collectors.toSet());
            }
            repositoryNames = ImmutableSet.copyOf(Iterables.limit(repositoryNames, 10));
        }

        return ResponseEntity.ok(new FormDataValuesCollection(ImmutableList.of(
                FormDataValues.fromCollection("repositoryNames", repositoryNames))));
    }

}
