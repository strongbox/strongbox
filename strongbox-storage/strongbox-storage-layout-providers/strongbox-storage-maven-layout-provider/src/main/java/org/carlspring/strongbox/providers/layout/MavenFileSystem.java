package org.carlspring.strongbox.providers.layout;

import java.nio.file.FileSystem;
import java.util.Set;

import javax.inject.Inject;

import org.carlspring.strongbox.providers.io.RepositoryFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryFileSystemProvider;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * @author sbespalov
 *
 */
public class MavenFileSystem extends RepositoryFileSystem
{

    @Inject
    private Maven2LayoutProvider layoutProvider;

    public MavenFileSystem(Repository repository,
                           FileSystem storageFileSystem,
                           RepositoryFileSystemProvider provider)
    {
        super(repository, storageFileSystem, provider);
    }

    @Override
    public Set<String> getDigestAlgorithmSet()
    {
        return layoutProvider.getDigestAlgorithmSet();
    }

}
