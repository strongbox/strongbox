package org.carlspring.strongbox.providers.layout;

import org.carlspring.commons.io.reloading.FSReloadableInputStreamHandler;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.event.repository.RepositoryEventListenerRegistry;
import org.carlspring.strongbox.io.ByteRangeInputStream;
import org.carlspring.strongbox.io.LayoutInputStream;
import org.carlspring.strongbox.io.LayoutOutputStream;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributeType;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.StorageFileSystemProvider;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.util.MessageDigestUtils;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class decorates {@link StorageFileSystemProvider} with common layout specific
 * logic. <br>
 * 
 * @author sbespalov
 * 
 * @see LayoutProvider
 */
public abstract class LayoutFileSystemProvider extends StorageFileSystemProvider
{
    private static final Logger logger = LoggerFactory.getLogger(LayoutFileSystemProvider.class);

    @Inject
    private ArtifactEventListenerRegistry artifactEventListenerRegistry;
    
    @Inject
    private RepositoryEventListenerRegistry repositoryEventListenerRegistry;
    
    @Inject
    private ArtifactEntryService artifactEntryService;
    
    public LayoutFileSystemProvider(FileSystemProvider storageFileSystemProvider)
    {
        super(storageFileSystemProvider);
    }

    protected abstract AbstractLayoutProvider getLayoutProvider();
    
    @Override
    public LayoutInputStream newInputStream(Path path,
                                              OpenOption... options)
        throws IOException
    {
        if (!Files.exists(path))
        {
            throw new FileNotFoundException(path.toString());
        }
        if (Files.isDirectory(path))
        {
            throw new FileNotFoundException(String.format("The artifact path is a directory: [%s]",
                                                          path.toString()));
        }
        
        InputStream is = super.newInputStream(path, options);
        ByteRangeInputStream bris;
        try
        {
            bris = new ByteRangeInputStream(is);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IOException(e);
        }
        bris.setReloadableInputStreamHandler(new FSReloadableInputStreamHandler(path.toFile()));
        bris.setLength(Files.size(path));
        
        try
        {
            return decorateStream((RepositoryPath) path, bris);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IOException(e);
        }
    }

    protected LayoutInputStream decorateStream(RepositoryPath path,
                                               InputStream is)
            throws NoSuchAlgorithmException, IOException
    {
        Set<String> digestAlgorithmSet = path.getFileSystem().getDigestAlgorithmSet();
        LayoutInputStream result = new LayoutInputStream(is, digestAlgorithmSet);
        
        // Add digest algorithm only if it is not a Checksum (we don't need a Checksum of Checksum).
        if (Boolean.TRUE.equals(RepositoryFiles.isChecksum(path)))
        {
            return result;
        }
        
        digestAlgorithmSet.stream().forEach(a -> {
            String checksum = null;
            try
            {
                checksum = getChecksum(path, result, a);
            }
            catch (IOException e)
            {
                logger.error(String.format("Failed to get checksum for [%s]", path), e);
            }
            if (checksum == null)
            {
                return;
            }

            result.getHexDigests().put(a, checksum);
        });
        
        return result;
    }

    private String getChecksum(RepositoryPath path,
                               LayoutInputStream is,
                               String digestAlgorithm) throws IOException
    {
        RepositoryPath checksumPath = getChecksumPath(path, digestAlgorithm);
        
        String checksum = null;
        if (Files.exists(checksumPath) && Files.size(checksumPath) != 0)
        {
            checksum = MessageDigestUtils.readChecksumFile(Files.newInputStream(checksumPath));
        }
        else
        {
            checksum = is.getMessageDigestAsHexadecimalString(digestAlgorithm);
        }

        return checksum;
    }

    public RepositoryPath getChecksumPath(RepositoryPath path,
                                          String digestAlgorithm)
    {
        String checksumExtension = ".".concat(digestAlgorithm.toLowerCase().replaceAll("-", ""));

        return path.resolveSibling(path.getFileName().toString().concat(checksumExtension));
    }
    
    @Override
    public OutputStream newOutputStream(Path path,
                                        OpenOption... options)
        throws IOException
    {
        if (Files.exists(path) && Files.isDirectory(path))
        {
            throw new FileNotFoundException(String.format("The artifact path is a directory: [%s]",
                                                          path.toString()));
        }
        
        Files.createDirectories(path.getParent());
        
        OutputStream os = super.newOutputStream(path, options);
        try
        {
            return decorateStream((RepositoryPath) path, os);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IOException(e);
        }
    }

    protected LayoutOutputStream decorateStream(RepositoryPath path,
                                                OutputStream os)
            throws NoSuchAlgorithmException, IOException
    {
        Set<String> digestAlgorithmSet = path.getFileSystem().getDigestAlgorithmSet();
        LayoutOutputStream result = new LayoutOutputStream(os);
        
        // Add digest algorithm only if it is not a Checksum (we don't need a Checksum of Checksum).
        if (Boolean.TRUE.equals(RepositoryFiles.isChecksum(path)))
        {
            return result;
        }
        digestAlgorithmSet.stream()
                          .forEach(e -> {
                              try
                              {
                                  result.addAlgorithm(e);
                              }
                              catch (NoSuchAlgorithmException t)
                              {
                                  logger.error(String.format("Digest algorithm not supported: alg-[%s]", e), t);
                              }
                          });
        return result;
    }
    
    public void storeChecksum(RepositoryPath basePath,
                              boolean forceRegeneration)
        throws IOException
    {
        Files.walk(basePath)
             .filter(p -> !Files.isDirectory(p))
             .filter(p -> {
                 try
                 {
                     return !Boolean.TRUE.equals(RepositoryFiles.isChecksum((RepositoryPath) p));
                 }
                 catch (IOException e)
                 {
                     logger.error(String.format("Failed to read attributes for [%s]", p), e);
                 }
                 return false;
             })
             .forEach(p -> {
                 try
                 {
                     writeChecksum((RepositoryPath) p, forceRegeneration);
                 }
                 catch (IOException e)
                 {
                     logger.error(String.format("Failed to write checksum for [%s]", p), e);
                 }
             });
    }

    
    public void writeChecksum(RepositoryPath path,
                              boolean force)
        throws IOException
    {
        try (LayoutInputStream is = newInputStream(path))
        {
            Set<String> digestAlgorithmSet = path.getFileSystem().getDigestAlgorithmSet();
            digestAlgorithmSet.stream()
                              .forEach(p ->
                                       {
                                           String checksum = is.getHexDigests().get(p);
                                           RepositoryPath checksumPath = getChecksumPath(path, p);
                                           if (Files.exists(checksumPath) && !force)
                                           {
                                               return;
                                           }
                                           try
                                           {
                                               Files.write(checksumPath, checksum.getBytes());
                                           }
                                           catch (IOException e)
                                           {
                                               logger.error(String.format("Failed to write checksum for [%s]",
                                                                          checksumPath.toString()), e);
                                           }
                                       });
        }
    }

    @Override
    public void delete(Path path,
                       boolean force)
        throws IOException
    {
        logger.debug("Deleting in (" + path + ")...");

        RepositoryPath repositoryPath=(RepositoryPath)path;
        
        deleteMetadata(repositoryPath);
        
        if (!Files.exists(path))
        {
            logger.warn(String.format("Path not found: path-[%s]", path));
            
            return;
        }
        

                
        super.delete(path, force);
        
        artifactEventListenerRegistry.dispatchArtifactPathDeletedEvent(path);

        logger.debug(String.format("Removed [%s]", path));
    }
    
    @Override
    protected void doDeletePath(RepositoryPath repositoryPath,
                                boolean force)
        throws IOException
    {
        if (!RepositoryFiles.isArtifact(repositoryPath))
        {
            super.doDeletePath(repositoryPath, force);
            return;
        }
        
        ArtifactEntry artifactEntry = Optional.ofNullable(repositoryPath.getArtifactEntry())
                                              .orElseGet(() -> fetchArtifactEntry(repositoryPath));
        if (artifactEntry != null)
        {
            artifactEntryService.delete(artifactEntry);
        }
        
        super.doDeletePath(repositoryPath, force);
    }

    private ArtifactEntry fetchArtifactEntry(RepositoryPath repositoryPath)
    {
        Repository repository = repositoryPath.getRepository();
        String path;
        try
        {
            path = RepositoryFiles.relativizePath(repositoryPath);
        }
        catch (IOException e)
        {
            logger.error(String.format("Failed to fetch ArtifactEntry for [%s]", repositoryPath), e);
            return null;
        }

        return artifactEntryService.findOneArtifact(repository.getStorage().getId(),
                                                    repository.getId(),
                                                    path);
    }

    @Override
    public void deleteTrash(RepositoryPath path)
        throws IOException
    {
        Repository repository = path.getRepository();
        Storage storage = repository.getStorage();

        logger.debug("Emptying trash for " + storage.getId() + ":" + repository.getId() + "...");

        super.deleteTrash(path);

        repositoryEventListenerRegistry.dispatchEmptyTrashEvent(storage.getId(), repository.getId());

        logger.debug("Trash for " + storage.getId() + ":" + repository.getId() + " removed.");
    }

    
    
    @Override
    public void undelete(RepositoryPath path)
        throws IOException
    {
        Repository repository = path.getRepository();
        Storage storage = repository.getStorage();

        logger.debug(String.format("Attempting to restore: path-[%s]; ", path));
        
        super.undelete(path);

        repositoryEventListenerRegistry.dispatchUndeleteTrashEvent(storage.getId(), repository.getId());

        logger.debug("The trash for " + storage.getId() + ":" + repository.getId() + " has been undeleted.");
    }

    @Override
    protected Map<RepositoryFileAttributeType, Object> getRepositoryFileAttributes(RepositoryPath repositoryRelativePath,
                                                                                   RepositoryFileAttributeType... attributeTypes)
        throws IOException
    {
        return getLayoutProvider().getRepositoryFileAttributes(repositoryRelativePath, attributeTypes);
    }
    
    protected void deleteMetadata(RepositoryPath repositoryPath)
            throws IOException
    {
    	
    };

    
}
