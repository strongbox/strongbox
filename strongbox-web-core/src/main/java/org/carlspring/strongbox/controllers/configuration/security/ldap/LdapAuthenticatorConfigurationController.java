package org.carlspring.strongbox.controllers.configuration.security.ldap;

import org.carlspring.strongbox.authentication.support.AuthoritiesExternalToInternalMapper;
import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.controllers.configuration.security.ldap.support.LdapGroupSearchResponseEntityBody;
import org.carlspring.strongbox.controllers.configuration.security.ldap.support.LdapMessages;
import org.carlspring.strongbox.controllers.configuration.security.ldap.support.LdapUserDnPatternsResponseEntityBody;
import org.carlspring.strongbox.controllers.configuration.security.ldap.support.LdapUserSearchResponseEntityBody;
import org.carlspring.strongbox.controllers.configuration.security.ldap.support.SpringSecurityLdapConfigurationTester;
import org.carlspring.strongbox.controllers.configuration.security.ldap.support.SpringSecurityLdapInternalsMutator;
import org.carlspring.strongbox.controllers.configuration.security.ldap.support.SpringSecurityLdapInternalsSupplier;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationForm;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationTestForm;
import org.carlspring.strongbox.validation.RequestBodyValidationException;

import javax.inject.Inject;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping(value = "/api/configuration/ldap")
@Api(value = "/api/configuration/ldap")
public class LdapAuthenticatorConfigurationController
        extends BaseController
{

    private static final String FAILED_PUT_LDAP = "LDAP configuration cannot be updated because the submitted form contains errors!";

    private static final String FAILED_PUT_LDAP_TEST = "LDAP configuration cannot be tested because the submitted form contains errors!";

    private static final String ERROR_PUT_LDAP = "LDAP configuration update succeeded";

    private static final String ERROR_DROP_LDAP = "LDAP configuration drop succeeded";

    private static final String SUCCESS_PUT_LDAP = "Failed to update LDAP configuration.";

    private static final String SUCCESS_DROP_LDAP = "Failed to drop LDAP configuration.";

    private static final String LDAP_TEST_PASSED = "LDAP configuration test passed";

    private static final String LDAP_TEST_FAILED = "LDAP configuration test failed";

    private static final String ERROR_PUT_LDAP_TEST = "Failed to test LDAP configuration.";

    private static final String SUCCESS_ADD_ROLE_MAPPING = "LDAP role mapping configuration update succeeded";

    @Inject
    private SpringSecurityLdapInternalsSupplier springSecurityLdapInternalsSupplier;

    @Inject
    private SpringSecurityLdapInternalsMutator springSecurityLdapInternalsMutator;

    @Inject
    private SpringSecurityLdapConfigurationTester springSecurityLdapInternalsTester;

    @ApiOperation(value = "Tests LDAP configuration settings")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "LDAP configuration test has passed.") })
    @PutMapping(value = "/test", produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity testLdapConfiguration(@RequestBody @Validated LdapConfigurationTestForm form,
                                                BindingResult bindingResult,
                                                @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_PUT_LDAP_TEST, bindingResult);
        }

        boolean result;
        try
        {
            result = springSecurityLdapInternalsTester.test(form);
        }
        catch (Exception e)
        {
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_PUT_LDAP_TEST, e, acceptHeader);
        }

        return getSuccessfulResponseEntity(result ? LDAP_TEST_PASSED : LDAP_TEST_FAILED, acceptHeader);
    }

    @ApiOperation(value = "Update the LDAP configuration settings")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "LDAP configuration updated successfully.") })
    @PutMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity putLdapConfiguration(@RequestBody @Validated LdapConfigurationForm form,
                                               BindingResult bindingResult,
                                               @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_PUT_LDAP, bindingResult);
        }

        try
        {
            springSecurityLdapInternalsMutator.saveLdapConfiguration(form);
        }
        catch (Exception e)
        {
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_PUT_LDAP, e, acceptHeader);
        }

        return getSuccessfulResponseEntity(SUCCESS_PUT_LDAP, acceptHeader);
    }

    @ApiOperation(value = "Drops LDAP configuration")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "LDAP configuration dropped.") })
    @DeleteMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity dropLdapConfiguration(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        try
        {
            springSecurityLdapInternalsMutator.dropLdapConfiguration();
        }
        catch (Exception e)
        {
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_DROP_LDAP, e, acceptHeader);
        }

        return getSuccessfulResponseEntity(SUCCESS_DROP_LDAP, acceptHeader);
    }

    @ApiOperation(value = "Returns LDAP configuration")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The LDAP configuration."),
                            @ApiResponse(code = 400, message = "LDAP is not enabled.") })
    @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getLdapConfiguration(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return getBadRequestResponseEntity(LdapMessages.NOT_CONFIGURED, acceptHeader);
        }

        Object url = ObjectUtils.defaultIfNull(getUrl(acceptHeader).getBody(),
                                               StringUtils.EMPTY);
        Object managerDn = ObjectUtils.defaultIfNull(getManagerDn(acceptHeader).getBody(),
                                                     StringUtils.EMPTY);
        Object rolesMapping = ObjectUtils.defaultIfNull(getRolesMapping(acceptHeader).getBody(),
                                                        StringUtils.EMPTY);
        Object groupSearchFilter = ObjectUtils.defaultIfNull(getGroupSearchFilter(acceptHeader).getBody(),
                                                             StringUtils.EMPTY);
        Object userDnPatterns = ObjectUtils.defaultIfNull(getUserDnPatterns(acceptHeader).getBody(),
                                                          StringUtils.EMPTY);
        Object userSearchFilter = ObjectUtils.defaultIfNull(getUserSearchFilter(acceptHeader).getBody(),
                                                            StringUtils.EMPTY);

        return ResponseEntity.ok(ImmutableSet.of(ImmutableMap.of("url", url),
                                                 ImmutableMap.of("managerDn", managerDn),
                                                 rolesMapping,
                                                 userDnPatterns,
                                                 ImmutableMap.of("groupSearchFilter", groupSearchFilter),
                                                 ImmutableMap.of("userSearchFilter", userSearchFilter))
        );
    }

    @ApiOperation(value = "Returns LDAP roles to strongbox internal roles mapping")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The mapping."),
                            @ApiResponse(code = 400, message = "LDAP is not enabled.") })
    @GetMapping(value = "/rolesMapping",
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getRolesMapping(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return getBadRequestResponseEntity(LdapMessages.NOT_CONFIGURED, acceptHeader);
        }

        AuthoritiesExternalToInternalMapper body = springSecurityLdapInternalsSupplier.getAuthoritiesMapper();
        return ResponseEntity.ok(body);
    }

    @ApiOperation(value = "Adds LDAP role mapping if the mapping does not exist. It doesn't override existing value.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "LDAP role mapping addition succeeded"),
                            @ApiResponse(code = 400, message = "LDAP is not enabled or LDAP role mapping already exists for given LDAP role"),
                            @ApiResponse(code = 500, message = "Failed to add LDAP role mapping") })
    @PostMapping(value = "/rolesMapping/{externalRole}/{internalRole}",
            produces = { MediaType.TEXT_PLAIN_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity addRoleMapping(@PathVariable String externalRole,
                                         @PathVariable String internalRole,
                                         @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return getBadRequestResponseEntity(LdapMessages.NOT_CONFIGURED, acceptHeader);
        }
        try
        {
            String previousInternalRole = springSecurityLdapInternalsSupplier.getAuthoritiesMapper()
                                                                             .addRoleMapping(externalRole,
                                                                                             internalRole);
            if (previousInternalRole != null)
            {
                return getBadRequestResponseEntity(LdapMessages.ROLE_ALREADY_EXISTS, acceptHeader);
            }
        }
        catch (Exception e)
        {
            String message = "Failed to add LDAP role mapping.";
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, message, e, acceptHeader);
        }

        return getSuccessfulResponseEntity(SUCCESS_ADD_ROLE_MAPPING, acceptHeader);
    }

    @ApiOperation(value = "Adds or updates LDAP role mapping")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "LDAP role mapping add or update succeeded"),
                            @ApiResponse(code = 400, message = "LDAP is not enabled"),
                            @ApiResponse(code = 500, message = "Failed to add or update LDAP role mapping") })
    @PutMapping(value = "/rolesMapping/{externalRole}/{internalRole}",
            produces = { MediaType.TEXT_PLAIN_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity setRoleMapping(@PathVariable String externalRole,
                                         @PathVariable String internalRole,
                                         @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return getBadRequestResponseEntity(LdapMessages.NOT_CONFIGURED, acceptHeader);
        }
        try
        {
            springSecurityLdapInternalsSupplier.getAuthoritiesMapper().putRoleMapping(externalRole, internalRole);
        }
        catch (Exception e)
        {
            String message = "Failed to add or update LDAP role mapping!";
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, message, e, acceptHeader);
        }

        return getSuccessfulResponseEntity("LDAP role mapping add or update succeeded", acceptHeader);
    }


    @ApiOperation(value = "Deletes LDAP role mapping")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "LDAP role mapping deletion succeeded"),
                            @ApiResponse(code = 400, message = "LDAP is not enabled or externalRole does not exist in the LDAP roles mapping"),
                            @ApiResponse(code = 500, message = "Failed to delete the LDAP role mapping!") })
    @DeleteMapping(value = "/rolesMapping/{externalRole}",
            produces = { MediaType.TEXT_PLAIN_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity deleteRoleMapping(@PathVariable String externalRole,
                                            @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return getBadRequestResponseEntity(LdapMessages.NOT_CONFIGURED, acceptHeader);
        }
        try
        {
            String removedInternalRole = springSecurityLdapInternalsSupplier.getAuthoritiesMapper()
                                                                            .deleteRoleMapping(externalRole);
            if (removedInternalRole == null)
            {
                String message = String.format("%s role does not exist in the LDAP roles mapping", externalRole);
                return getBadRequestResponseEntity(message, acceptHeader);
            }
        }
        catch (Exception e)
        {
            String message = "Failed to delete the LDAP role mapping!";
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, message, e, acceptHeader);
        }

        return getSuccessfulResponseEntity("LDAP role mapping deletion succeeded", acceptHeader);
    }

    @ApiOperation(value = "Returns user DN patterns. See http://docs.spring.io/spring-security/site/docs/current/reference/html/ldap.html#using-bind-authentication")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "User DN patterns."),
                            @ApiResponse(code = 204, message = "User DN patterns are empty."),
                            @ApiResponse(code = 400, message = "LDAP is not enabled.") })
    @GetMapping(value = "/userDnPatterns",
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getUserDnPatterns(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return getBadRequestResponseEntity(LdapMessages.NOT_CONFIGURED, acceptHeader);
        }
        List<String> userDnPatterns = springSecurityLdapInternalsSupplier.getUserDnPatterns();
        if (userDnPatterns == null)
        {
            return ResponseEntity.noContent()
                                 .build();
        }

        LdapUserDnPatternsResponseEntityBody body = new LdapUserDnPatternsResponseEntityBody(userDnPatterns);
        return ResponseEntity.ok(body);
    }

    @ApiOperation(value = "Returns user search filter. See http://docs.spring.io/spring-security/site/docs/current/reference/html/ldap.html#using-bind-authentication")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "User search filter."),
                            @ApiResponse(code = 204, message = "User search filter was not provided."),
                            @ApiResponse(code = 400, message = "LDAP is not enabled or userSearchFilter is not supported via this method.") })
    @GetMapping(value = "/userSearchFilter",
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getUserSearchFilter(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return getBadRequestResponseEntity(LdapMessages.NOT_CONFIGURED, acceptHeader);
        }
        LdapUserSearch userSearch = springSecurityLdapInternalsSupplier.getUserSearch();
        if (userSearch == null)
        {
            return ResponseEntity.noContent()
                                 .build();
        }
        if (!(userSearch instanceof FilterBasedLdapUserSearch))
        {
            String message = String.format(
                    "Unable to display userSearchFilter configuration. %s is not supported via this method.",
                    userSearch.getClass());
            return getBadRequestResponseEntity(message, acceptHeader);
        }

        LdapUserSearchResponseEntityBody body = springSecurityLdapInternalsSupplier.getUserSearchXmlHolder(
                (FilterBasedLdapUserSearch) userSearch);
        return ResponseEntity.ok(body);
    }

    @ApiOperation(value = "Returns LDAP url")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "LDAP url") })
    @GetMapping(value = "/url", produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getUrl(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return getBadRequestResponseEntity(LdapMessages.NOT_CONFIGURED, acceptHeader);
        }

        return ResponseEntity.ok(springSecurityLdapInternalsSupplier.getUrl());
    }

    @ApiOperation(value = "Returns user dn")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "User user dn") })
    @GetMapping(value = "/managerDn",
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getManagerDn(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return getBadRequestResponseEntity(LdapMessages.NOT_CONFIGURED, acceptHeader);
        }

        return ResponseEntity.ok(springSecurityLdapInternalsSupplier.getUserDn());
    }

    @ApiOperation(value = "Returns group search filter. See http://docs.spring.io/spring-security/site/docs/current/reference/html/ldap.html#loading-authorities")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Group search filter."),
                            @ApiResponse(code = 204, message = "Group search filter was not provided."),
                            @ApiResponse(code = 400, message = "LDAP is not enabled or groupSearchFilter is not supported via this method.") })
    @GetMapping(value = "/groupSearchFilter",
            produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getGroupSearchFilter(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return getBadRequestResponseEntity(LdapMessages.NOT_CONFIGURED, acceptHeader);
        }
        LdapAuthoritiesPopulator populator = springSecurityLdapInternalsSupplier.getAuthoritiesPopulator();
        if (populator == null)
        {
            return ResponseEntity.noContent()
                                 .build();
        }
        if (!(populator instanceof DefaultLdapAuthoritiesPopulator))
        {
            String message = String.format(
                    "Unable to display groupSearchFilter configuration. %s is not supported via this method.",
                    populator.getClass());
            return getBadRequestResponseEntity(message, acceptHeader);
        }

        LdapGroupSearchResponseEntityBody body = springSecurityLdapInternalsSupplier.ldapGroupSearchHolder(
                (DefaultLdapAuthoritiesPopulator) populator);
        return ResponseEntity.ok(body);
    }

}
