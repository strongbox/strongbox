package org.carlspring.strongbox.services;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.carlspring.strongbox.domain.DirectoryListing;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

public interface DirectoryListingService
{

    DirectoryListing fromStorages(Map<String, ? extends Storage> storages) throws IOException;

    DirectoryListing fromRepositories(Map<String, ? extends Repository> repositories) throws IOException;

    DirectoryListing fromRepositoryPath(RepositoryPath path)
        throws IOException;
    
    DirectoryListing fromPath(Path root, Path path)
            throws IOException;

}
