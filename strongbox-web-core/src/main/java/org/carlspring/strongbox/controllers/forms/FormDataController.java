package org.carlspring.strongbox.controllers.forms;

import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.forms.configuration.MavenRepositoryConfigurationForm;
import org.carlspring.strongbox.forms.configuration.NugetRepositoryConfigurationForm;
import org.carlspring.strongbox.forms.configuration.RawRepositoryConfigurationForm;
import org.carlspring.strongbox.providers.datastore.StorageProviderEnum;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryStatusEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.security.AuthoritiesProvider;
import org.carlspring.strongbox.util.FieldSpy;

import javax.inject.Inject;
import java.util.Arrays;

import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    @GetMapping(value = "/storages",
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getStoragesFormData()
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

}
