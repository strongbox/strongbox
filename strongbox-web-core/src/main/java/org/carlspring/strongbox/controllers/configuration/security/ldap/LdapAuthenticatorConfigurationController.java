package org.carlspring.strongbox.controllers.configuration.security.ldap;

import org.carlspring.strongbox.authentication.support.AuthoritiesExternalToInternalMapper;
import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.controllers.configuration.security.ldap.support.*;

import javax.inject.Inject;
import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.web.bind.annotation.*;

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

    @Inject
    private SpringSecurityLdapInternalsSupplier springSecurityLdapInternalsSupplier;

    @Inject
    private SpringSecurityLdapInternalsUpdater springSecurityLdapInternalsUpdater;


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
                            @ApiResponse(code = 500, message = "Addition of LDAP role mapping failed with server error") })
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
            String message = "Addition of LDAP role mapping failed. Check server logs for more information.";
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, message, e, acceptHeader);
        }

        return getSuccessfulResponseEntity("LDAP role mapping addition succeeded", acceptHeader);
    }

    @ApiOperation(value = "Adds or updates LDAP role mapping")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "LDAP role mapping add or update succeeded"),
                            @ApiResponse(code = 400, message = "LDAP is not enabled"),
                            @ApiResponse(code = 500, message = "Insert or update LDAP role mapping failed with server error") })
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
            springSecurityLdapInternalsSupplier.getAuthoritiesMapper()
                                               .putRoleMapping(externalRole, internalRole);
        }
        catch (Exception e)
        {
            String message = "Add or update LDAP role mapping failed. Check server logs for more information.";
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, message, e, acceptHeader);
        }

        return getSuccessfulResponseEntity("LDAP role mapping add or update succeeded", acceptHeader);
    }


    @ApiOperation(value = "Deletes LDAP role mapping")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "LDAP role mapping deletion succeeded"),
                            @ApiResponse(code = 400, message = "LDAP is not enabled or externalRole does not exist in the LDAP roles mapping"),
                            @ApiResponse(code = 500, message = "LDAP role mapping deletion failed with server error") })
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
            String message = "LDAP role mapping deletion failed. Check server logs for more information.";
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

    @ApiOperation(value = "Removes the provided user DN pattern from the userDnPatterns.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "User DN pattern removal from the userDnPatterns succeeded"),
                            @ApiResponse(code = 400, message = "LDAP is not enabled or pattern does not match any existing userDnPatterns"),
                            @ApiResponse(code = 500, message = "User DN pattern removal from the userDnPatterns failed with server error") })
    @DeleteMapping(value = "/userDnPatterns/{pattern}",
                   produces = { MediaType.TEXT_PLAIN_VALUE,
                                MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity deleteUserDnPattern(@PathVariable String pattern,
                                              @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
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
        if (!userDnPatterns.remove(pattern))
        {
            return getBadRequestResponseEntity("Pattern does not match any existing userDnPatterns", acceptHeader);
        }
        try
        {
            springSecurityLdapInternalsUpdater.updateUserDnPatterns(userDnPatterns);
        }
        catch (Exception e)
        {
            String message = "User DN pattern removal failed. Check server logs for more information.";
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, message, e, acceptHeader);
        }

        String message = String.format("User DN pattern %s removed from the userDnPatterns", pattern);
        return getSuccessfulResponseEntity(message, acceptHeader);
    }

    @ApiOperation(value = "Adds the provided user DN pattern to the userDnPatterns.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "User DN pattern addition to the userDnPatterns succeeded"),
                            @ApiResponse(code = 400, message = "LDAP is not enabled or if userDnPatterns collection haven't changed"),
                            @ApiResponse(code = 500, message = "User DN pattern addition to the userDnPatterns failed with server error") })
    @PostMapping(value = "/userDnPatterns/{pattern}",
                 produces = { MediaType.TEXT_PLAIN_VALUE,
                              MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity addUserDnPattern(@PathVariable String pattern,
                                           @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
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
        if (!userDnPatterns.add(pattern))
        {
            return getBadRequestResponseEntity(LdapMessages.NOT_CONFIGURED, acceptHeader);
        }
        try
        {
            springSecurityLdapInternalsUpdater.updateUserDnPatterns(userDnPatterns);
        }
        catch (Exception e)
        {
            String message = "User DN pattern addition failed. Check server logs for more information.";
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, message, e, acceptHeader);
        }

        String message = String.format("User DN pattern %s added to the userDnPatterns", pattern);
        return getSuccessfulResponseEntity(message, acceptHeader);
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

    @ApiOperation(value = "Updates LDAP user search filter. See http://docs.spring.io/spring-security/site/docs/current/reference/html/ldap.html#using-bind-authentication")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "User search filter updated."),
                            @ApiResponse(code = 204, message = "AbstractLdapAuthenticator was not provided."),
                            @ApiResponse(code = 400, message = "LDAP is not enabled.") })
    @PutMapping(value = "/userSearchFilter/{searchBase}/{searchFilter}",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity updateUserSearchFilter(@PathVariable String searchBase,
                                                 @PathVariable String searchFilter,
                                                 @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return getBadRequestResponseEntity(LdapMessages.NOT_CONFIGURED, acceptHeader);
        }
        AbstractLdapAuthenticator abstractLdapAuthenticator = springSecurityLdapInternalsSupplier.getAuthenticator();
        if (abstractLdapAuthenticator == null)
        {
            return ResponseEntity.noContent()
                                 .build();
        }
        abstractLdapAuthenticator.setUserSearch(new FilterBasedLdapUserSearch(searchBase, searchFilter,
                                                                              (BaseLdapPathContextSource) springSecurityLdapInternalsSupplier.getContextSource()));

        return getSuccessfulResponseEntity("User search filter updated.", acceptHeader);
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

        LdapGroupSearchResponseEntityBody body = springSecurityLdapInternalsSupplier.ldapGroupSearchHolder((DefaultLdapAuthoritiesPopulator) populator);
        return ResponseEntity.ok(body);
    }

    @ApiOperation(value = "Updates LDAP group search filter. See http://docs.spring.io/spring-security/site/docs/current/reference/html/ldap.html#loading-authorities")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Group search filter updated."),
                            @ApiResponse(code = 204, message = "LdapAuthoritiesPopulator was not provided."),
                            @ApiResponse(code = 400, message = "LDAP is not enabled or ldapAuthoritiesPopulator class is not supported via this method.") })
    @PutMapping(value = "/groupSearchFilter/{searchBase}/{searchFilter}",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity updateGroupSearchFilter(@PathVariable String searchBase,
                                                  @PathVariable String searchFilter,
                                                  @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return getBadRequestResponseEntity(LdapMessages.NOT_CONFIGURED, acceptHeader);
        }
        LdapAuthoritiesPopulator ldapAuthoritiesPopulator = springSecurityLdapInternalsSupplier.getAuthoritiesPopulator();
        if (ldapAuthoritiesPopulator == null)
        {
            return ResponseEntity.noContent()
                                 .build();
        }
        if (!(ldapAuthoritiesPopulator instanceof DefaultLdapAuthoritiesPopulator))
        {
            return getBadRequestResponseEntity(
                    "Configured ldapAuthoritiesPopulator is not supported. LDAP has to be configured with DefaultLdapAuthoritiesPopulator.",
                    acceptHeader);
        }
        springSecurityLdapInternalsUpdater.updateGroupSearchFilter(
                (DefaultLdapAuthoritiesPopulator) ldapAuthoritiesPopulator, searchBase, searchFilter);

        return getSuccessfulResponseEntity("Group search filter updated.", acceptHeader);
    }

}
