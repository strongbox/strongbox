package org.carlspring.strongbox.jaas.authentication.basic;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.jaas.authentication.AuthenticationException;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.security.auth.login.LoginException;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author mtodorov
 */
public class BasicAuthenticationHandler
{

    @Autowired
    private ConfigurationManager configurationManager;

    private static Logger logger = LoggerFactory.getLogger(BasicAuthenticationHandler.class);


    public boolean authenticate(HttpHeaders headers,
                                String storage,
                                String repositoryName,
                                String path,
                                String method)
            throws LoginException
    {
        // TODO: This should actually check if the repository is anonymous, or not.
        final Configuration configuration = configurationManager.getConfiguration();
        final Repository repository = configuration.getStorages().get(storage).getRepositories().get(repositoryName);

        boolean requiresAuthentication = repository.isSecured();

        if (requiresAuthentication)
        {
            logger.debug("Repository " + repositoryName + " requires authentication.");

            String authorizationHeader;
            if (headers.getRequestHeader("authorization") != null)
            {
                authorizationHeader = headers.getRequestHeader("authorization").get(0);

                if (authorizationHeader == null)
                {
                    // If there is no authorization header, this must fail, if attempting PUT, or DELETE.
                    throw new AuthenticationException("HTTP PUT and DELETE operations require authentication!");
                }

                String[] credentials = BasicAuthenticationDecoder.decode(authorizationHeader);

                //If login or password fail
                if (credentials == null || credentials.length != 2)
                {
                    // throw new LoginException("Unauthorized access to [ADD RESOURCE HERE]...");
                }

                String username = credentials[0];
                String password = credentials[1];

                System.out.println("Username: " + username);
                System.out.println("Password: " + password);

                /*
                if (!validateCredentials(username, password))
                {
                    throw new AuthenticationException();
                }
                */
            }
            else
            {
                // Allow anonymous mode:
                return true;
            }
        }
        else
        {
            logger.debug("Repository " + repositoryName + " does not require authentication.");
        }

        return false;
    }

    public ConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

}
