package org.carlspring.strongbox.testing;

import java.nio.file.FileSystem;
import java.util.Collections;
import java.util.Set;

import org.carlspring.strongbox.providers.io.LayoutFileSystem;
import org.carlspring.strongbox.providers.io.StorageFileSystemProvider;
import org.carlspring.strongbox.storage.repository.Repository;

public class NullFileSystem extends LayoutFileSystem
{

    public NullFileSystem(Repository repository,
                          FileSystem storageFileSystem,
                          StorageFileSystemProvider provider)
    {
        super(repository, storageFileSystem, provider);
    }

    @Override
    public Set<String> getDigestAlgorithmSet()
    {
        return Collections.emptySet();
    }

}
