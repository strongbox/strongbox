package org.carlspring.strongbox.services;

import org.carlspring.strongbox.domain.DirectoryListing;
import org.carlspring.strongbox.domain.FileContent;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributeType;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.util.StrongboxUriComponentsBuilder;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DirectoryListingServiceImpl
        implements DirectoryListingService
{

    private static final Logger logger = LoggerFactory.getLogger(DirectoryListingService.class);

    @Inject
    private StrongboxUriComponentsBuilder uriBuilder;

    public DirectoryListingServiceImpl()
    {
    }

    @Override
    public DirectoryListing fromStorages(Map<String, ? extends Storage> storages)
            throws IOException
    {
        DirectoryListing directoryListing = new DirectoryListing();
        directoryListing.setLink(uriBuilder.getCurrentRequestURL());

        for (Storage storage : storages.values())
        {
            FileContent fileContent = new FileContent(storage.getId());
            directoryListing.getDirectories().add(fileContent);

            fileContent.setStorageId(storage.getId());
            fileContent.setUrl(uriBuilder.browseUriBuilder(storage.getId()).build().toUri().toURL());
        }

        return directoryListing;
    }

    @Override
    public DirectoryListing fromRepositories(String storageId, Map<String, ? extends Repository> repositories)
            throws IOException
    {
        DirectoryListing directoryListing = new DirectoryListing();
        directoryListing.setLink(uriBuilder.getCurrentRequestURL());

        for (Repository repository : repositories.values())
        {
            FileContent fileContent = new FileContent(repository.getId());
            directoryListing.getDirectories().add(fileContent);

            fileContent.setStorageId(repository.getStorage().getId());
            fileContent.setRepositoryId(repository.getId());

            fileContent.setUrl(uriBuilder.browseUriBuilder(repository.getStorage().getId(),
                                                           repository.getId(),
                                                           (String) null)
                                         .build()
                                         .toUri()
                                         .toURL());
        }

        return directoryListing;
    }

    @Override
    public DirectoryListing fromRepositoryPath(RepositoryPath path)
            throws IOException
    {
        UriComponentsBuilder directoryListingBuilder = uriBuilder.browseUriBuilder(path.getStorage().getId(),
                                                                                   path.getRepository().getId(),
                                                                                   (String) null);

        UriComponentsBuilder downloadListingBuilder = uriBuilder.storageUriBuilder(path.getStorage().getId(),
                                                                                   path.getRepository().getId(),
                                                                                   (String) null);

        return generateDirectoryListing(path.normalize(), directoryListingBuilder, downloadListingBuilder);
    }

    @Override
    public DirectoryListing fromPath(UriComponentsBuilder directoryLinkBuilder,
                                     UriComponentsBuilder downloadLinkBuilder,
                                     Path rootPath,
                                     Path path)
            throws IOException, RuntimeException
    {
        rootPath = rootPath.normalize();
        path = path.normalize();

        if (!path.equals(rootPath) && !path.startsWith(rootPath))
        {
            String message = String.format(
                    "Requested directory listing for [%s] is outside the scope of the root path [%s]! Possible intrusion attack or misconfiguration!",
                    path, rootPath);
            logger.error(message);
            throw new RuntimeException(message);
        }

        return generateDirectoryListing(path, directoryLinkBuilder, downloadLinkBuilder);
    }

    /**
     * Generate a directory listing for a path.
     *
     * @param path The path which will be listed
     * @param directoryLinkBuilder UriComponentsBuilder to be used for generating directory links
     * @param downloadLinkBuilder UriComponentsBuilder to be used for generating download links;
     *                            If null - file.url will be null as well.
     *
     * @return
     *
     * @throws IOException
     */
    private DirectoryListing generateDirectoryListing(Path path,
                                                      UriComponentsBuilder directoryLinkBuilder,
                                                      UriComponentsBuilder downloadLinkBuilder)
            throws IOException
    {

        DirectoryListing listing = new DirectoryListing();
        listing.setLink(uriBuilder.getCurrentRequestURL());

        List<Path> contentPaths;
        try (Stream<Path> pathStream = Files.list(path))
        {
            contentPaths = pathStream
                                   .filter(p -> !p.toFile().getName().startsWith("."))
                                   .filter(p -> {
                                       try
                                       {
                                           return !Files.isHidden(p);
                                       }
                                       catch (IOException e)
                                       {
                                           logger.debug("Error accessing path {}", p);
                                           return false;
                                       }
                                   })
                                   .sorted()
                                   .collect(Collectors.toList());
        }

        for (Path contentPath : contentPaths)
        {
            FileContent file = new FileContent(contentPath.getFileName().toString());

            Map<String, Object> fileAttributes = Files.readAttributes(contentPath, "*");

            file.setStorageId((String) fileAttributes.get(RepositoryFileAttributeType.STORAGE_ID.getName()));
            file.setRepositoryId((String) fileAttributes.get(RepositoryFileAttributeType.REPOSITORY_ID.getName()));
            file.setArtifactPath((String) fileAttributes.get(RepositoryFileAttributeType.ARTIFACT_PATH.getName()));

            if (Boolean.TRUE.equals(fileAttributes.get("isDirectory")))
            {
                // TODO: Use `UriComponentsBuilder.clone()` when spring-framework/issues/24772 is fixed.
                URL fileUrl = UriComponentsBuilder.fromUriString(directoryLinkBuilder.toUriString())
                                                  .path(sanitizedPath(file.getArtifactPath()))
                                                  .build()
                                                  .toUri()
                                                  .toURL();

                file.setUrl(fileUrl);

                listing.getDirectories().add(file);
            }
            else
            {
                if(downloadLinkBuilder != null)
                {
                    // TODO: Use `UriComponentsBuilder.clone()` when spring-framework/issues/24772 is fixed.
                    URL fileUrl = UriComponentsBuilder.fromUriString(downloadLinkBuilder.toUriString())
                                                      .path(sanitizedPath(file.getArtifactPath()))
                                                      .build()
                                                      .toUri()
                                                      .toURL();
                    file.setUrl(fileUrl);
                }

                file.setLastModified(new Date(((FileTime) fileAttributes.get("lastModifiedTime")).toMillis()));
                file.setSize((Long) fileAttributes.get("size"));
                listing.getFiles().add(file);
            }
        }

        return listing;
    }

    private String sanitizedPath(String path)
    {
        return "/" + StringUtils.removeStart(path, "/");
    }

}
