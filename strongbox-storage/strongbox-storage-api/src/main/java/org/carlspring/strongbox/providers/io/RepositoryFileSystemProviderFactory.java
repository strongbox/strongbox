package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.providers.layout.RepositoryLayoutFileSystemProvider;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * @author sbespalov
 *
 */
@FunctionalInterface
public interface RepositoryFileSystemProviderFactory
{

    RepositoryLayoutFileSystemProvider create(Repository repository);

}
