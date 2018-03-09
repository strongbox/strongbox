package org.carlspring.strongbox.controllers.configuration.security.ldap;

import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.controllers.configuration.security.ldap.support.LdapConstants;
import org.carlspring.strongbox.controllers.configuration.security.ldap.support.LdapUserDnPatternsResponseEntityBody;
import org.carlspring.strongbox.controllers.configuration.security.ldap.support.SpringSecurityLdapInternalsSupplier;
import org.carlspring.strongbox.controllers.configuration.security.ldap.support.SpringSecurityLdapInternalsUpdater;

import javax.inject.Inject;
import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Przemyslaw Fusik
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
    @RequestMapping(value = "/rolesMapping", method = RequestMethod.GET, produces = { MediaType.APPLICATION_XML_VALUE,
                                                                                      MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getRolesMapping()
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return toResponseEntityError(LdapConstants.LdapMessages.NOT_CONFIGURED, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(springSecurityLdapInternalsSupplier.getAuthoritiesMapper());
    }

    @ApiOperation(value = "Adds LDAP role mapping if the mapping does not exist. It doesn't override existing value.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "LDAP role mapping addition succeeded"),
                            @ApiResponse(code = 400, message = "LDAP is not enabled or LDAP role mapping already exists for given LDAP role"),
                            @ApiResponse(code = 500, message = "Addition of LDAP role mapping failed with server error") })
    @RequestMapping(value = "/rolesMapping/{externalRole}/{internalRole}", method = RequestMethod.POST)
    public ResponseEntity<String> addRoleMapping(@PathVariable String externalRole,
                                                 @PathVariable String internalRole)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return ResponseEntity.badRequest()
                                 .body(LdapConstants.LdapMessages.NOT_CONFIGURED);
        }
        try
        {
            String previousInternalRole = springSecurityLdapInternalsSupplier.getAuthoritiesMapper()
                                                                             .addRoleMapping(externalRole,
                                                                                             internalRole);
            if (previousInternalRole != null)
            {
                return ResponseEntity.badRequest()
                                     .body("LDAP role mapping already exists for given LDAP role");
            }
        }
        catch (Exception e)
        {
            return toError("Addition of LDAP role mapping failed with server error: " + e.getLocalizedMessage());
        }

        return ResponseEntity.ok("LDAP role mapping addition succeeded");
    }

    @ApiOperation(value = "Adds or updates LDAP role mapping")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "LDAP role mapping add or update succeeded"),
                            @ApiResponse(code = 400, message = "LDAP is not enabled"),
                            @ApiResponse(code = 500, message = "Insert or update LDAP role mapping failed with server error") })
    @RequestMapping(value = "/rolesMapping/{externalRole}/{internalRole}", method = RequestMethod.PUT)
    public ResponseEntity<String> setRoleMapping(@PathVariable String externalRole,
                                                 @PathVariable String internalRole)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return ResponseEntity.badRequest()
                                 .body(LdapConstants.LdapMessages.NOT_CONFIGURED);
        }
        try
        {
            springSecurityLdapInternalsSupplier.getAuthoritiesMapper()
                                               .putRoleMapping(externalRole, internalRole);
        }
        catch (Exception e)
        {
            return toError("Add or update LDAP role mapping failed with server error: " + e.getLocalizedMessage());
        }

        return ResponseEntity.ok("LDAP role mapping add or update succeeded");
    }


    @ApiOperation(value = "Deletes LDAP role mapping")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "LDAP role mapping deletion succeeded"),
                            @ApiResponse(code = 400, message = "LDAP is not enabled or externalRole does not exist in the LDAP roles mapping"),
                            @ApiResponse(code = 500, message = "LDAP role mapping deletion failed with server error") })
    @RequestMapping(value = "/rolesMapping/{externalRole}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteRoleMapping(@PathVariable String externalRole)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return ResponseEntity.badRequest()
                                 .body(LdapConstants.LdapMessages.NOT_CONFIGURED);
        }
        try
        {
            String removedInternalRole = springSecurityLdapInternalsSupplier.getAuthoritiesMapper()
                                                                            .deleteRoleMapping(externalRole);
            if (removedInternalRole == null)
            {
                return ResponseEntity.badRequest()
                                     .body(externalRole + "role does not exist in the LDAP roles mapping");
            }
        }
        catch (Exception e)
        {
            return toError("LDAP role mapping deletion failed with server error: " + e.getLocalizedMessage());
        }

        return ResponseEntity.ok("LDAP role mapping deletion succeeded");
    }

    @ApiOperation(value = "Returns user DN patterns. See http://docs.spring.io/spring-security/site/docs/current/reference/html/ldap.html#using-bind-authentication")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "User DN patterns."),
                            @ApiResponse(code = 204, message = "User DN patterns are empty."),
                            @ApiResponse(code = 400, message = "LDAP is not enabled.") })
    @RequestMapping(value = "/userDnPatterns", method = RequestMethod.GET, produces = { MediaType.APPLICATION_XML_VALUE,
                                                                                        MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getUserDnPatterns()
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return toResponseEntityError(LdapConstants.LdapMessages.NOT_CONFIGURED, HttpStatus.BAD_REQUEST);
        }
        List<String> userDnPatterns = springSecurityLdapInternalsSupplier.getUserDnPatterns();
        if (userDnPatterns == null)
        {
            return ResponseEntity.noContent()
                                 .build();
        }
        return ResponseEntity.ok(new LdapUserDnPatternsResponseEntityBody(userDnPatterns));
    }

    @ApiOperation(value = "Removes the provided user DN pattern from the userDnPatterns.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "User DN pattern removal from the userDnPatterns succeeded"),
                            @ApiResponse(code = 400, message = "LDAP is not enabled or pattern does not match any existing userDnPatterns"),
                            @ApiResponse(code = 500, message = "User DN pattern removal from the userDnPatterns failed with server error") })
    @RequestMapping(value = "/userDnPatterns/{pattern}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteUserDnPattern(@PathVariable String pattern)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return ResponseEntity.badRequest()
                                 .body(LdapConstants.LdapMessages.NOT_CONFIGURED);
        }
        List<String> userDnPatterns = springSecurityLdapInternalsSupplier.getUserDnPatterns();
        if (userDnPatterns == null)
        {
            return ResponseEntity.noContent().build();
        }
        if (!userDnPatterns.remove(pattern))
        {
            return ResponseEntity.badRequest()
                                 .body("Pattern does not match any existing userDnPatterns");
        }
        try
        {
            springSecurityLdapInternalsUpdater.updateUserDnPatterns(userDnPatterns);
        }
        catch (Exception e)
        {
            return toError("User DN pattern removal failed with server error: " + e.getLocalizedMessage());
        }

        return ResponseEntity.ok("user DN pattern " + pattern + " removed from the userDnPatterns");
    }

    @ApiOperation(value = "Adds the provided user DN pattern to the userDnPatterns.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "User DN pattern addition to the userDnPatterns succeeded"),
                            @ApiResponse(code = 400, message = "LDAP is not enabled or if userDnPatterns collection haven't changed"),
                            @ApiResponse(code = 500, message = "User DN pattern addition to the userDnPatterns failed with server error") })
    @RequestMapping(value = "/userDnPatterns/{pattern}", method = RequestMethod.POST)
    public ResponseEntity<String> addUserDnPattern(@PathVariable String pattern)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return ResponseEntity.badRequest()
                                 .body(LdapConstants.LdapMessages.NOT_CONFIGURED);
        }
        List<String> userDnPatterns = springSecurityLdapInternalsSupplier.getUserDnPatterns();
        if (userDnPatterns == null)
        {
            return ResponseEntity.noContent()
                                 .build();
        }
        if (!userDnPatterns.add(pattern))
        {
            return ResponseEntity.badRequest()
                                 .body(LdapConstants.LdapMessages.NOT_CONFIGURED);
        }
        try
        {
            springSecurityLdapInternalsUpdater.updateUserDnPatterns(userDnPatterns);
        }
        catch (Exception e)
        {
            return toError("User DN pattern addition failed with server error: " + e.getLocalizedMessage());
        }

        return ResponseEntity.ok("user DN pattern " + pattern + " added to the userDnPatterns");
    }

    @ApiOperation(value = "Returns user search filter. See http://docs.spring.io/spring-security/site/docs/current/reference/html/ldap.html#using-bind-authentication")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "User search filter."),
                            @ApiResponse(code = 204, message = "User search filter was not provided."),
                            @ApiResponse(code = 400, message = "LDAP is not enabled or userSearchFilter is not supported via this method.") })
    @RequestMapping(value = "/userSearchFilter", method = RequestMethod.GET, produces = { MediaType.APPLICATION_XML_VALUE,
                                                                                          MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getUserSearchFilter()
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return toResponseEntityError(LdapConstants.LdapMessages.NOT_CONFIGURED, HttpStatus.BAD_REQUEST);
        }
        LdapUserSearch userSearch = springSecurityLdapInternalsSupplier.getUserSearch();
        if (userSearch == null)
        {
            return ResponseEntity.noContent()
                                 .build();
        }
        if (!(userSearch instanceof FilterBasedLdapUserSearch))
        {
            return toResponseEntityError("Unable to display userSearchFilter configuration. " + userSearch.getClass() +
                                         " is not supported via this method.", HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(
                springSecurityLdapInternalsSupplier.getUserSearchXmlHolder((FilterBasedLdapUserSearch) userSearch));
    }

    @ApiOperation(value = "Updates LDAP user search filter. See http://docs.spring.io/spring-security/site/docs/current/reference/html/ldap.html#using-bind-authentication")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "User search filter updated."),
                            @ApiResponse(code = 204, message = "AbstractLdapAuthenticator was not provided."),
                            @ApiResponse(code = 400, message = "LDAP is not enabled.") })
    @RequestMapping(value = "/userSearchFilter/{searchBase}/{searchFilter}", method = RequestMethod.PUT)
    public ResponseEntity<String> updateUserSearchFilter(@PathVariable String searchBase,
                                                         @PathVariable String searchFilter)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return ResponseEntity.badRequest()
                                 .body(LdapConstants.LdapMessages.NOT_CONFIGURED);
        }
        AbstractLdapAuthenticator abstractLdapAuthenticator = springSecurityLdapInternalsSupplier.getAuthenticator();
        if (abstractLdapAuthenticator == null)
        {
            return ResponseEntity.noContent()
                                 .build();
        }
        abstractLdapAuthenticator.setUserSearch(new FilterBasedLdapUserSearch(searchBase, searchFilter,
                                                                              (BaseLdapPathContextSource) springSecurityLdapInternalsSupplier.getContextSource()));

        return ResponseEntity.ok("User search filter updated.");
    }

    @ApiOperation(value = "Returns group search filter. See http://docs.spring.io/spring-security/site/docs/current/reference/html/ldap.html#loading-authorities")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Group search filter."),
                            @ApiResponse(code = 204, message = "Group search filter was not provided."),
                            @ApiResponse(code = 400, message = "LDAP is not enabled or groupSearchFilter is not supported via this method.") })
    @RequestMapping(value = "/groupSearchFilter", method = RequestMethod.GET, produces = { MediaType.APPLICATION_XML_VALUE,
                                                                                           MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getGroupSearchFilter()
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return toResponseEntityError(LdapConstants.LdapMessages.NOT_CONFIGURED, HttpStatus.BAD_REQUEST);
        }
        LdapAuthoritiesPopulator populator = springSecurityLdapInternalsSupplier.getAuthoritiesPopulator();
        if (populator == null)
        {
            return ResponseEntity.noContent()
                                 .build();
        }
        if (!(populator instanceof DefaultLdapAuthoritiesPopulator))
        {
            return toResponseEntityError("Unable to display groupSearchFilter configuration. " + populator.getClass() +
                                         " is not supported via this method.", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(
                springSecurityLdapInternalsSupplier.ldapGroupSearchHolder((DefaultLdapAuthoritiesPopulator) populator));
    }

    @ApiOperation(value = "Updates LDAP group search filter. See http://docs.spring.io/spring-security/site/docs/current/reference/html/ldap.html#loading-authorities")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Group search filter updated."),
                            @ApiResponse(code = 204, message = "LdapAuthoritiesPopulator was not provided."),
                            @ApiResponse(code = 400, message = "LDAP is not enabled or ldapAuthoritiesPopulator class is not supported via this method.") })
    @RequestMapping(value = "/groupSearchFilter/{searchBase}/{searchFilter}", method = RequestMethod.PUT)
    public ResponseEntity<String> updateGroupSearchFilter(@PathVariable String searchBase,
                                                          @PathVariable String searchFilter)
    {
        if (!springSecurityLdapInternalsSupplier.isLdapAuthenticationEnabled())
        {
            return ResponseEntity.badRequest()
                                 .body(LdapConstants.LdapMessages.NOT_CONFIGURED);
        }
        LdapAuthoritiesPopulator ldapAuthoritiesPopulator = springSecurityLdapInternalsSupplier.getAuthoritiesPopulator();
        if (ldapAuthoritiesPopulator == null)
        {
            return ResponseEntity.noContent()
                                 .build();
        }
        if (!(ldapAuthoritiesPopulator instanceof DefaultLdapAuthoritiesPopulator))
        {
            return ResponseEntity.badRequest()
                                 .body(
                                         "Configured ldapAuthoritiesPopulator is not supported. LDAP has to be configured with DefaultLdapAuthoritiesPopulator.");
        }
        springSecurityLdapInternalsUpdater.updateGroupSearchFilter(
                (DefaultLdapAuthoritiesPopulator) ldapAuthoritiesPopulator, searchBase, searchFilter);

        return ResponseEntity.ok("Group search filter updated.");
    }

}
