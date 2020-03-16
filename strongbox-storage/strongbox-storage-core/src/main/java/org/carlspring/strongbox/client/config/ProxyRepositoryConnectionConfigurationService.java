package org.carlspring.strongbox.client.config;

import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;

import javax.ws.rs.client.Client;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

/**
 * @author ankit.tomar
 */
public interface ProxyRepositoryConnectionConfigurationService
{

    Client getClientForRepository(Repository repository)
        throws IllegalAccessException,
        InvocationTargetException,
        MalformedURLException;

    Client getClientForRepository(RemoteRepository remoteRepository)
        throws MalformedURLException;

}
