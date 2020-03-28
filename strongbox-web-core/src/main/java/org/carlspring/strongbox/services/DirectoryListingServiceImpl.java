package org.carlspring.strongbox.services;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.domain.DirectoryListing;
import org.carlspring.strongbox.domain.FileContent;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributeType;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.util.StrongboxUriComponentsBuilder;

import javax.inject.Inject;
import java.io.File;
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
    private PropertiesBooter propertiesBooter;

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
    public DirectoryListing fromRepositories(String storageId,
                                             Map<String, ? extends Repository> repositories)
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
    public DirectoryListing fromRepositoryPath(RepositoryPath repositoryPath)
            throws IOException
    {
        UriComponentsBuilder directoryLinkBuilder = uriBuilder.browseUriBuilder(repositoryPath.getStorage().getId(),
                                                                                repositoryPath.getRepository().getId(),
                                                                                (String) null);

        UriComponentsBuilder fileLinkBuilder = uriBuilder.storageUriBuilder(repositoryPath.getStorage().getId(),
                                                                            repositoryPath.getRepository().getId(),
                                                                            (String) null);

        List<Path> contentPaths = fetchContent(repositoryPath);

        return generateDirectoryListing(null, contentPaths, directoryLinkBuilder, fileLinkBuilder);
    }

    @Override
    public DirectoryListing fromPath(UriComponentsBuilder directoryLinkBuilder,
                                     UriComponentsBuilder fileLinkBuilder,
                                     Path rootPath,
                                     Path requestedPath)
            throws IOException, RuntimeException
    {
        rootPath = rootPath.normalize();
        requestedPath = requestedPath.normalize();

        if (!requestedPath.equals(rootPath) && !requestedPath.startsWith(rootPath))
        {
            String message = "Requested directory listing is outside the scope of the root path! Possible intrusion attack or misconfiguration!";
            logger.error(message);
            throw new RuntimeException(message);
        }

        Path relativePath = rootPath.relativize(requestedPath);

        List<Path> contentPaths = fetchContent(requestedPath);

        return generateDirectoryListing(relativePath, contentPaths, directoryLinkBuilder, fileLinkBuilder);
    }

    /**
     * Generate a directory listing for a path.
     *
     * @param relativePath         A relative path to the artifact within the scope of (directory|file)LinkBuilder
     *                             This field is only needed/used when listing non-storage paths.
     * @param contentPaths         List of Paths to iterate over to generate the DirectoryListing.
     * @param directoryLinkBuilder UriComponentsBuilder to be used for generating directory links
     * @param fileLinkBuilder      UriComponentsBuilder to be used for generating download links;
     *                             If null - file.url will be null as well.
     *
     * @return
     *
     * @throws IOException
     */
    private DirectoryListing generateDirectoryListing(Path relativePath,
                                                      List<Path> contentPaths,
                                                      UriComponentsBuilder directoryLinkBuilder,
                                                      UriComponentsBuilder fileLinkBuilder)
            throws IOException
    {
        DirectoryListing listing = new DirectoryListing();
        listing.setLink(uriBuilder.getCurrentRequestURL());

        for (Path contentPath : contentPaths)
        {
            FileContent file = new FileContent(contentPath.getFileName().toString());

            Map<String, Object> fileAttributes = Files.readAttributes(contentPath, "*");

            file.setStorageId((String) fileAttributes.get(RepositoryFileAttributeType.STORAGE_ID.getName()));
            file.setRepositoryId((String) fileAttributes.get(RepositoryFileAttributeType.REPOSITORY_ID.getName()));
            file.setArtifactPath((String) fileAttributes.get(RepositoryFileAttributeType.ARTIFACT_PATH.getName()));
            file.setLastModified(new Date(((FileTime) fileAttributes.get("lastModifiedTime")).toMillis()));
            file.setSize((Long) fileAttributes.get("size"));

            // Depending on scanned file/path this might not be a `storage` artifact (i.e. it could be a log file)
            // in which case we need to set the artifactPath to the file's path.
            if (file.getArtifactPath() == null)
            {
                file.setArtifactPath(sanitizedPath(relativePath.toString() + "/" + file.getName()));
            }

            UriComponentsBuilder builder = null;
            if (Boolean.TRUE.equals(fileAttributes.get("isDirectory")))
            {
                // TODO: Use `UriComponentsBuilder.clone()` when spring-framework/issues/24772 is fixed and released
                builder = UriComponentsBuilder.fromUriString(directoryLinkBuilder.toUriString());
                listing.getDirectories().add(file);
            }
            else
            {
                // TODO: Use `UriComponentsBuilder.clone()` when spring-framework/issues/24772 is fixed and released
                if(fileLinkBuilder != null)
                {
                    builder = UriComponentsBuilder.fromUriString(fileLinkBuilder.toUriString());
                }

                listing.getFiles().add(file);
            }

            if(builder != null)
            {
                URL url = builder.path("/" + file.getArtifactPath())
                                 .build()
                                 .toUri()
                                 .toURL();

                file.setUrl(url);
            }
        }

        return listing;
    }

    private List<Path> fetchContent(Path path)
            throws IOException
    {
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

        return contentPaths;
    }

    private String sanitizedPath(final String rawPath)
    {
        String sanitizedPath = rawPath;

        if(!File.pathSeparator.equals("/")) {
            sanitizedPath = sanitizedPath.replaceAll("\\\\", "/");
        }

        sanitizedPath = StringUtils.removeStart(sanitizedPath, "/");

        return sanitizedPath;
    }

}
