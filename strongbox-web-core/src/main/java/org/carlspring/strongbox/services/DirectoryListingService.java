package org.carlspring.strongbox.services;

import org.carlspring.strongbox.domain.DirectoryListing;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.util.StrongboxUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.web.util.UriComponentsBuilder;

public interface DirectoryListingService
{

    /**
     * Create a directory listing using a Map of storages.
     *
     * @param storages
     *
     * @return
     *
     * @throws IOException
     */
    DirectoryListing fromStorages(Map<String, ? extends Storage> storages)
            throws IOException;

    /**
     * Create a directory listing for a storageId using a Map of repositories.
     *
     * @param storageId
     * @param repositories
     *
     * @return
     *
     * @throws IOException
     */
    DirectoryListing fromRepositories(String storageId, Map<String, ? extends Repository> repositories)
            throws IOException;

    /**
     * Create a directory listing using a RepositoryPath.
     *
     * @param path
     *
     * @return
     *
     * @throws IOException
     */
    DirectoryListing fromRepositoryPath(RepositoryPath path)
            throws IOException;

    /**
     * Generic directory listing.
     *
     * @param directoryLinkBuilder preferably a {@link StrongboxUriComponentsBuilder} instance to use as "base" when
     *                             generating urls.
     * @param downloadLinkBuilder  download link builder - if null, should use `directoryLinkBuilder`.
     * @param root                 The root path in which directory listing is allowed. Used as a
     *                             precaution to prevent directory traversing.
     *                             When "path" is outside "rootPath" an exception will be thrown.
     * @param path                 The path which needs to be listed
     *
     * @return DirectoryListing
     *
     * @throws RuntimeException when path is not within rootPath.
     * @throws IOException
     */
    DirectoryListing fromPath(UriComponentsBuilder directoryLinkBuilder,
                              UriComponentsBuilder downloadLinkBuilder,
                              Path root,
                              Path path)
            throws IOException, RuntimeException;

}
