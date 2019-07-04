package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.providers.layout.LayoutFileSystemProvider;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * @author sbespalov
 *
 */
@FunctionalInterface
public interface LayoutFileSystemProviderFactory
{

    LayoutFileSystemProvider create(Repository repository);

}
