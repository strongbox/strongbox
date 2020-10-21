package org.carlspring.strongbox.storage.repository.remote.heartbeat;

import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;

/**
 * @author sbespalov
 *
 */
public interface RemoteRepositoryAlivenessService
{

    boolean isAlive(RemoteRepository remoteRepository);

    void put(RemoteRepository remoteRepository,
             boolean aliveness);
    
}
