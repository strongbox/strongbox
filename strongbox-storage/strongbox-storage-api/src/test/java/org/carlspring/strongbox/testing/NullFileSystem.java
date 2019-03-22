package org.carlspring.strongbox.testing;

import java.nio.file.FileSystem;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
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
        return Stream.of(MessageDigestAlgorithms.MD5)
                     .collect(Collectors.toSet());

    }

}
