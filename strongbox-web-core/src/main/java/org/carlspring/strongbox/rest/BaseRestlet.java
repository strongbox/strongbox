package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.storage.DataCenter;

import javax.ws.rs.core.HttpHeaders;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Martin Todorov
 */
public abstract class BaseRestlet
{

    private boolean httpBasicEnabled = true;

    private boolean sslEnabled = false;

    @Autowired
    private DataCenter dataCenter;


    public boolean requiresAuthentication(String storage,
                                          String repository,
                                          String path)
    {
        // TODO: Get a Repository object.
        // TODO: Check if the repository allows anonymous
        // TODO: If anonymous is allowed, return false.
        // TODO: If anonymous is forbidden, check the authentication for:

        return dataCenter.getStorage(storage).getRepository(repository).isSecured();
    }

    public boolean validateAuthentication(String storage,
                                          String repository,
                                          String path,
                                          HttpHeaders headers,
                                          String protocol)
    {
        // TODO: Check for the "Authentication:" header. If it's set to Basic, handle HTTP Basic authentication.
        // TODO: - HTTP Basic (default); return true if valid,
        // TODO: - SSL
        final List<String> authorizationHeader = headers.getRequestHeader("authorization");
        if (isHttpBasicEnabled() && (authorizationHeader != null && !authorizationHeader.isEmpty()))
        {
            return handleHTTPBasicAuthentication(headers);
        }
        if (isSslEnabled())
        {
            return handleSSLAuthentication();
        }

        return false;
    }

    public boolean handleSSLAuthentication()
    {
        return false;
    }

    public boolean handleHTTPBasicAuthentication(HttpHeaders headers)
    {
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

    public DataCenter getDataCenter()
    {
        return dataCenter;
    }

    public void setDataCenter(DataCenter dataCenter)
    {
        this.dataCenter = dataCenter;
    }

}
