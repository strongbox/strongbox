package org.carlspring.strongbox.providers.layout;

import java.nio.file.FileSystem;
import java.util.Set;

import javax.inject.Inject;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.providers.io.LayoutFileSystem;
import org.carlspring.strongbox.storage.repository.RepositoryData;

/**
 * @author sbespalov
 *
 */
public class NpmFileSystem extends LayoutFileSystem
{

    @Inject
    private NpmLayoutProvider layoutProvider;

    public NpmFileSystem(PropertiesBooter propertiesBooter,
                         RepositoryData repository,
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
