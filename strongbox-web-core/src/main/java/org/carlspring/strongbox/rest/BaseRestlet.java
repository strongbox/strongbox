package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.security.jaas.authentication.AuthenticationException;
import org.carlspring.strongbox.security.jaas.authentication.basic.BasicAuthenticationDecoder;
import org.carlspring.strongbox.security.jaas.authorization.AuthorizationException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Martin Todorov
 */
@Component
public abstract class BaseRestlet
{

    private static Logger logger = LoggerFactory.getLogger(BaseRestlet.class);

    private boolean httpBasicEnabled = true;

    private boolean sslEnabled = false;

    @Autowired
    private ConfigurationManager configurationManager;


    public boolean requiresAuthentication(String storageId,
                                          String repositoryId,
                                          String path,
                                          String protocol)
    {
        // Get a Repository object.
        // TODO: Check if the repository allows anonymous
        // TODO: If anonymous is allowed, return false.
        // TODO: If anonymous is forbidden, check the authentication for:

        // logger.debug("Protocol: " + protocol);

        final boolean required = configurationManager.getConfiguration().getStorage(storageId).getRepository(repositoryId).isSecured() ||
                                 protocol.equalsIgnoreCase("http");

        logger.debug("Resource: /storages/" + storageId + "/" + repositoryId + "/" + path + " requires authentication? " + required);

        return required;
    }

    public boolean validateAuthentication(String storageId,
                                          String repositoryId,
                                          String path,
                                          HttpHeaders headers,
                                          String protocol)
            throws AuthenticationException
    {
        // TODO: Check for the "Authentication:" header. If it's set to Basic, handle HTTP Basic authentication.
        // TODO: - HTTP Basic (default); return true if valid,
        // TODO: - SSL
        final List<String> authorizationHeader = headers.getRequestHeader("authorization");
        if (isHttpBasicEnabled() && (authorizationHeader != null && !authorizationHeader.isEmpty()))
        {
            return handleHTTPBasicAuthentication(headers, path);
        }
        if (isSslEnabled())
        {
            logger.debug("Security: HTTPS");
            return handleSSLAuthentication();
        }

        return false;
    }

    public void handleAuthentication(String storageId,
                                     String repositoryId,
                                     String path,
                                     HttpHeaders headers,
                                     HttpServletRequest request)
            throws AuthenticationException
    {
        // SB-3
        /*
        String protocol = request.getRequestURL().toString().split(":")[0];

        if (requiresAuthentication(storageId, repositoryId, path, protocol) &&
            (!validateAuthentication(storageId, repositoryId, path, headers, protocol)))
        {
            // Return HTTP 401
            throw new AuthorizationException("You are not authorized to deploy artifacts to this repository.");
        }
        */
    }

    public boolean handleSSLAuthentication()
    {
        return false;
    }

    public boolean handleHTTPBasicAuthentication(HttpHeaders headers,
                                                 String path)
            throws AuthenticationException
    {
        String authorizationHeader;
        if (headers.getRequestHeader("authorization") != null)
        {
            authorizationHeader = headers.getRequestHeader("authorization").get(0);

            if (authorizationHeader == null)
            {
                // If there is no authorization header, this must fail, if attempting PUT, or DELETE.
                throw new AuthenticationException("The requested path (" + path  + ") requires authorization.");
            }

            String[] credentials = BasicAuthenticationDecoder.decode(authorizationHeader);

            //If login or password fail
            if (credentials == null || credentials.length != 2)
            {
                // throw new LoginException("Unauthorized access to [ADD RESOURCE HERE]...");
            }

            String username = credentials[0];
            String password = credentials[1];

            logger.debug("Security: HTTP Basic (" + username +  "|" + password + ")");

            // TODO: Add proper implementation
            if ((username != null && username.equals("maven")) &&
                (password != null && password.equals("password")))
            {
                return true;
            }

            /*
            if (!validateCredentials(username, password))
            {
                throw new AuthenticationException();
            }
            */
        }

        return false;
    }

    public boolean isHttpBasicEnabled()
    {
        return httpBasicEnabled;
    }

    public void setHttpBasicEnabled(boolean httpBasicEnabled)
    {
        this.httpBasicEnabled = httpBasicEnabled;
    }

    public boolean isSslEnabled()
    {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled)
    {
        this.sslEnabled = sslEnabled;
    }

}
