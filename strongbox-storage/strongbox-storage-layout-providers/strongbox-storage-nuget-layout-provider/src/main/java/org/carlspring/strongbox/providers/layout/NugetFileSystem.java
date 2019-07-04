package org.carlspring.strongbox.providers.layout;

import java.nio.file.FileSystem;
import java.util.Set;

import javax.inject.Inject;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.providers.io.LayoutFileSystem;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * @author sbespalov
 *
 */
public class NugetFileSystem extends LayoutFileSystem
{

    @Inject
    private NugetLayoutProvider layoutProvider;

    public NugetFileSystem(PropertiesBooter propertiesBooter,
                           Repository repository,
                           FileSystem storageFileSystem,
                           LayoutFileSystemProvider provider)
    {
        super(propertiesBooter, repository, storageFileSystem, provider);
    }

    @Override
    public Set<String> getDigestAlgorithmSet()
    {
        return layoutProvider.getDigestAlgorithmSet();
    }

}
