package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryListing
{

    private static final Logger logger = LoggerFactory.getLogger(DirectoryListing.class);

    private List<FileContent> directories;

    private List<FileContent> files;

    public static DirectoryListing fromStorages(Map<String, Storage> storages)
    {
        DirectoryListing directoryListing = new DirectoryListing();

        for (Map.Entry<String, Storage> entry : storages.entrySet())
        {
            Storage storage = entry.getValue();
            directoryListing.getDirectories().add(new FileContent(storage.getId()));
        }

        return directoryListing;
    }

    public static DirectoryListing fromRepositories(Map<String, Repository> repositories)
    {
        DirectoryListing directoryListing = new DirectoryListing();

        for (Map.Entry<String, Repository> entry : repositories.entrySet())
        {
            Repository repository = entry.getValue();
            FileContent fileContent = new FileContent(repository.getId());
            directoryListing.getDirectories().add(fileContent);
        }

        return directoryListing;
    }

    /**
     * @param rootPath The root path in which directory listing is allowed. Used as a precaution to prevent directory traversing.
     *                 When "path" is outside "rootPath" an exception will be thrown.
     * @param path     The path which needs to be listed
     * @return DirectoryListing
     * @throws RuntimeException when path is not within rootPath.
     */
    public static DirectoryListing fromPath(Path rootPath,
                                            Path path)
            throws RuntimeException, IOException
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

        DirectoryListing directoryListing = new DirectoryListing();

        Map<String, List<FileContent>> content = generateDirectoryListing(path);

        directoryListing.setDirectories(content.get("directories"));
        directoryListing.setFiles(content.get("files"));

        return directoryListing;
    }

    private static Map<String, List<FileContent>> generateDirectoryListing(Path path)
            throws IOException
    {
        List<FileContent> directories = new ArrayList<>();
        List<FileContent> files = new ArrayList<>();

        List<Path> contentPaths = Files.list(path)
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

        for (Path contentPath : contentPaths)
        {
            FileContent file = new FileContent(contentPath.toFile().getName());

            if (Files.isDirectory(contentPath))
            {
                directories.add(file);
            }
            else
            {
                BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);

                file.setLastModified(new Date(attributes.lastModifiedTime().toMillis()));
                file.setSize(attributes.size());

                files.add(file);
            }
        }

        Map<String, List<FileContent>> listing = new HashMap<>();
        listing.put("directories", directories);
        listing.put("files", files);

        return listing;
    }


    public List<FileContent> getDirectories()
    {
        if (directories == null)
        {
            directories = new ArrayList<>();
        }

        return directories;
    }

    public List<FileContent> getFiles()
    {
        if (files == null)
        {
            files = new ArrayList<>();
        }

        return files;
    }

    public void setDirectories(List<FileContent> directories)
    {
        this.directories = directories;
    }

    public void setFiles(List<FileContent> files)
    {
        this.files = files;
    }

}
