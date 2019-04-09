package org.carlspring.strongbox.testing.storage.repository;

import org.carlspring.strongbox.storage.repository.MutableRepository;

/**
 * @author sbespalov
 *
 */
public interface RepositorySetup
{

    void setup(MutableRepository repository);
    
}
