package org.carlspring.strongbox.client.config;

import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;

import javax.ws.rs.client.Client;

/**
 * @author ankit.tomar
 */
public interface ProxyRepositoryConnectionConfigurationService
{

    Client getClientForRepository(Repository repository);

    Client getClientForRepository(RemoteRepository remoteRepository);

}
