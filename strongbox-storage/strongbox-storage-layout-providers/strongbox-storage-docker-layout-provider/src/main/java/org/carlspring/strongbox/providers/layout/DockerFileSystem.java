package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.providers.io.LayoutFileSystem;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.nio.file.FileSystem;
import java.util.Set;

/**
 * @author carlspring
 */
public class DockerFileSystem
        extends LayoutFileSystem
{

    @Inject
    private DockerLayoutProvider layoutProvider;

    public DockerFileSystem(PropertiesBooter propertiesBooter,
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
