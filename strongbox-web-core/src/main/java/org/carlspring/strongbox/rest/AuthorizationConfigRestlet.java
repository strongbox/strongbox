package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.security.jaas.Privilege;
import org.carlspring.strongbox.security.jaas.Role;
import org.carlspring.strongbox.users.security.AuthorizationConfig;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * Defines REST API for managing security configuration: privileges, roles etc..
 *
 * @author Alex Oreshkevich
 */
@Component
@Path("/configuration/authorization")
@Api(value = "/configuration/authorization")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
@PreAuthorize("hasAuthority('ADMIN')")
public class AuthorizationConfigRestlet
        extends BaseArtifactRestlet
{

    @Autowired
    AuthorizationConfigProvider configProvider;

    @Autowired
    OObjectDatabaseTx databaseTx;

    // ----------------------------------------------------------------------------------------------------------------
    // Add privilege
    @POST
    @Path("privilege")
    @ApiOperation(value = "Used to add new privileges")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The privilege was created successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    public synchronized Response addPrivilege(String json)
    {
        databaseTx.activateOnCurrentThread();
        Optional<AuthorizationConfig> configOptional = configProvider.getConfig();

        if (configOptional.isPresent())
        {
            AuthorizationConfig config = configOptional.get();
            Privilege privilege = read(json, Privilege.class);
            config.getPrivileges().getPrivileges().add(privilege);
            configProvider.updateConfig(config);
            return Response.ok().build();
        }
        else
        {
            return toError("Unable to locate AuthorizationConfig to update...");
        }
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Add role
    @POST
    @Path("role")
    @ApiOperation(value = "Used to add new roles")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The role was created successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    public synchronized Response addRole(String json)
    {
        databaseTx.activateOnCurrentThread();
        Optional<AuthorizationConfig> configOptional = configProvider.getConfig();

        if (configOptional.isPresent())
        {
            AuthorizationConfig config = configOptional.get();
            Role role = read(json, Role.class);
            config.getRoles().getRoles().add(role);
            configProvider.updateConfig(config);
            return Response.ok().build();
        }
        else
        {
            return toError("Unable to locate AuthorizationConfig to update...");
        }
    }


    // View authorization config as XML file
    @GET
    @Path("/xml")
    @Produces({ MediaType.APPLICATION_XML,
                MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieves the security-authorization.xml configuration file.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    public synchronized Response getAuthorizationConfig()
    {
        databaseTx.activateOnCurrentThread();
        Optional<AuthorizationConfig> configOptional = configProvider.getConfig();

        if (configOptional.isPresent())
        {
            AuthorizationConfig config = configOptional.get();
            return Response.ok(config).build();
        }
        else
        {
            return toError("Unable to locate AuthorizationConfig to retrieving...");
        }
    }
}
