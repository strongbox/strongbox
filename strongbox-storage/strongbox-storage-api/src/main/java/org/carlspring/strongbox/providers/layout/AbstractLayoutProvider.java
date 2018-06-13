package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.event.repository.RepositoryEventListenerRegistry;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.datastore.StorageProvider;
import org.carlspring.strongbox.providers.datastore.StorageProviderRegistry;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributeType;
import org.carlspring.strongbox.providers.io.RepositoryFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryFileSystemProvider;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathHandler;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.MutableRepository;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public abstract class AbstractLayoutProvider<T extends ArtifactCoordinates>
        implements LayoutProvider<T>
{

    private static final Logger logger = LoggerFactory.getLogger(AbstractLayoutProvider.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    protected LayoutProviderRegistry layoutProviderRegistry;
    
    @Inject
    protected StorageProviderRegistry storageProviderRegistry;

    @Inject
    private ArtifactEventListenerRegistry artifactEventListenerRegistry;

    @Inject
    private RepositoryEventListenerRegistry repositoryEventListenerRegistry;


    public abstract Set<String> getDefaultArtifactCoordinateValidators();

    public LayoutProviderRegistry getLayoutProviderRegistry()
    {
        return layoutProviderRegistry;
    }

    public void setLayoutProviderRegistry(LayoutProviderRegistry layoutProviderRegistry)
    {
        this.layoutProviderRegistry = layoutProviderRegistry;
    }

    public StorageProviderRegistry getStorageProviderRegistry()
    {
        return storageProviderRegistry;
    }

    public void setStorageProviderRegistry(StorageProviderRegistry storageProviderRegistry)
    {
        this.storageProviderRegistry = storageProviderRegistry;
    }

    public ConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    public Storage getStorage(String storageId)
    {
        return configurationManager.getConfiguration().getStorage(storageId);
    }

    protected StorageProvider getStorageProvider(Repository repository)
    {
        return storageProviderRegistry.getProvider(repository.getImplementation());
    }

    protected Repository getRepository(String storageId,
                                       String repositoryId)
    {
        Storage storage = getConfiguration().getStorage(storageId);

        logger.debug("Checking in " + storage.getId() + ":" + repositoryId + "...");

        return storage.getRepository(repositoryId);
    }

    public Set<String> getDigestAlgorithmSet()
    {
        return Stream.of(MessageDigestAlgorithms.MD5, MessageDigestAlgorithms.SHA_1)
                     .collect(Collectors.toSet());
    }
    
    @Override
    public final ArtifactInputStream getInputStream(RepositoryPath path) throws IOException
    {
        return (ArtifactInputStream) Files.newInputStream(path);
    }

    @Override
    public final ArtifactOutputStream getOutputStream(RepositoryPath path) throws IOException
    {
        return (ArtifactOutputStream) Files.newOutputStream(path);
    }
    
    @Override
    public RepositoryPath resolve(Repository repository,
                                  URI resource)
    {
        return resolve(repository).resolve(resource.toString());
    }

    @Override
    public RepositoryPath resolve(Repository repository,
                                  ArtifactCoordinates coordinates)
    {
        RepositoryFileSystem repositoryFileSystem = getRepositoryFileSystem(repository);
        
        return repositoryFileSystem.getRootDirectory().resolve(coordinates.toPath());
    }

    @Override
    public RootRepositoryPath resolve(Repository repository)
    {
        RepositoryFileSystem repositoryFileSystem = getRepositoryFileSystem(repository);
        
        return repositoryFileSystem.getRootDirectory();
    }
    
    public RepositoryFileSystem getRepositoryFileSystem(Repository repository)
    {
        FileSystem storageFileSystem = getStorageProvider(repository).getFileSystem();
        RepositoryFileSystem repositoryFileSystem = new RepositoryLayoutFileSystem(repository,
                                                                                   storageFileSystem,
                                                                                   getProvider(repository));
                                                                                   
        return repositoryFileSystem;
    }

    public RepositoryFileSystemProvider getProvider(Repository repository)
    {
        FileSystemProvider storageFileSystemProvider = getStorageProvider(repository).getFileSystemProvider();
        RepositoryLayoutFileSystemProvider repositoryFileSystemProvider = new RepositoryLayoutFileSystemProvider(
                storageFileSystemProvider, getRepositoryPathHandler(), this);
        
        return repositoryFileSystemProvider;
    }

    @Override
    public void copy(String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId,
                     String path)
            throws IOException
    {
//        artifactEventListenerRegistry.dispatchArtifactCopyingEvent(srcStorageId,
//                                                                   srcRepositoryId,
//                                                                   destStorageId,
//                                                                   destRepositoryId,
//                                                                   path);

        // TODO: Implement copying

//        artifactEventListenerRegistry.dispatchArtifactCopiedEvent(srcStorageId,
//                                                                  srcRepositoryId,
//                                                                  destStorageId,
//                                                                  destRepositoryId,
//                                                                  path);
    }

    @Override
    public void move(String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId,
                     String path)
            throws IOException
    {
//        artifactEventListenerRegistry.dispatchArtifactMovingEvent(srcStorageId,
//                                                                  srcRepositoryId,
//                                                                  destStorageId,
//                                                                  destRepositoryId,
//                                                                  path);

        // TODO: Implement moving

//        artifactEventListenerRegistry.dispatchArtifactMovedEvent(srcStorageId,
//                                                                 srcRepositoryId,
//                                                                 destStorageId,
//                                                                 destRepositoryId,
//                                                                 path);
    }

    @Override
    public void delete(String storageId,
                       String repositoryId,
                       String path,
                       boolean force)
            throws IOException, SearchException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        RepositoryPath repositoryPath = resolve(repository).resolve(path);
        delete(repositoryPath, force);
    }
    
    

    @Override
    public void delete(RepositoryPath repositoryPath,
                       boolean force)
        throws IOException,
        SearchException
    {
        logger.debug("Checking in (" + repositoryPath + ")...");
        
        if (!Files.exists(repositoryPath))
        {
            logger.warn(String.format("Path not found: path-[%s]", repositoryPath));
            
            return;
        }

        RepositoryFileSystemProvider provider = getProvider(repositoryPath.getRepository());
        provider.setAllowsForceDelete(force);
        provider.delete(repositoryPath);

        artifactEventListenerRegistry.dispatchArtifactPathDeletedEvent(repositoryPath);

        logger.debug(String.format("Removed [%s]", repositoryPath));        
    }

    @Override
    public void deleteTrash(String storageId,
                            String repositoryId)
            throws IOException
    {
        logger.debug("Emptying trash for " + storageId + ":" + repositoryId + "...");

        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        RepositoryPath path = resolve(repository);

        getProvider(repository).deleteTrash(path);

        repositoryEventListenerRegistry.dispatchEmptyTrashEvent(storageId, repositoryId);

        logger.debug("Trash for " + storageId + ":" + repositoryId + " removed.");
    }

    @Override
    public void deleteTrash()
            throws IOException
    {
        boolean trashRemoved = false;

        for (Map.Entry entry : getConfiguration().getStorages().entrySet())
        {
            MutableStorage storage = (MutableStorage) entry.getValue();

            final Map<String, MutableRepository> repositories = storage.getRepositories();
            for (MutableRepository repository : repositories.values())
            {
                if (!repository.allowsDeletion())
                {
                    logger.warn("Repository " + repository.getId() + " does not support removal of trash.");
                }
                
                deleteTrash(storage.getId(), repository.getId());

                trashRemoved = true;
            }
        }

        if (trashRemoved)
        {
            repositoryEventListenerRegistry.dispatchEmptyTrashForAllRepositoriesEvent();
        }
    }

    @Override
    public void undelete(RepositoryPath repositoryPath)
            throws IOException
    {
        logger.debug(String.format("Attempting to restore: path-[%s]; ",
                                   repositoryPath));

        Repository repository = repositoryPath.getFileSystem().getRepository();
        Storage storage = repository.getStorage();
        RepositoryFileSystemProvider provider = getProvider(repository);
        
        provider.undelete(repositoryPath);

        repositoryEventListenerRegistry.dispatchUndeleteTrashEvent(storage.getId(), repository.getId());

        logger.debug("The trash for " + storage.getId() + ":" + repository.getId() + " has been undeleted.");
    }

    @Override
    public void undeleteTrash()
            throws IOException
    {
        boolean trashUndeleted = false;

        for (Map.Entry<String, Storage> entry : getConfiguration().getStorages().entrySet())
        {
            Storage storage = entry.getValue();

            final Map<String, Repository> repositories = storage.getRepositories();
            for (Repository repository : repositories.values())
            {
                LayoutProvider provider = layoutProviderRegistry.getProvider(repository.getLayout());
                undelete(provider.resolve(repository));

                trashUndeleted = true;
            }
        }

        if (trashUndeleted)
        {
            repositoryEventListenerRegistry.dispatchUndeleteTrashForAllRepositoriesEvent();
        }
    }

    @Override
    public boolean containsArtifact(Repository repository,
                                    ArtifactCoordinates coordinates)
            throws IOException
    {
        RepositoryPath artifactPath = resolve(repository, coordinates);
        
        return Files.exists(artifactPath);
    }

    @Override
    public boolean containsPath(Repository repository,
                                String path)
            throws IOException
    {
        RepositoryPath repositoryPath = resolve(repository);

        return Files.exists(repositoryPath.resolve(path));
    }

    @Override
    public boolean isChecksum(RepositoryPath repositoryPath)
    {
        return isChecksum(repositoryPath.getFileName().toString());
    }

    @Override
    public void archive(String storageId,
                        String repositoryId,
                        String path)
            throws IOException
    {
        //artifactEventListenerRegistry.dispatchArtifactArchivingEvent(storageId, repositoryId, path);
        // TODO: Implement archiving
        //artifactEventListenerRegistry.dispatchArtifactArchivedEvent(storageId, repositoryId, path);
    }

    protected boolean isChecksum(String fileName)
    {
        for (String e : getDigestAlgorithmSet())
        {
            if (fileName.toString().endsWith("." + e.replaceAll("-", "").toLowerCase()))
            {
                return true;
            }
        }
        
        return false;
    }

    
    protected Map<RepositoryFileAttributeType, Object> getRepositoryFileAttributes(RepositoryPath repositoryPath,
                                                                                   RepositoryFileAttributeType... attributeTypes)
        throws IOException
    {
        if (attributeTypes == null || attributeTypes.length == 0)
        {
            return Collections.emptyMap();
        }

        Map<RepositoryFileAttributeType, Object> result = new HashMap<>();
        for (RepositoryFileAttributeType repositoryFileAttributeType : attributeTypes)
        {
            Object value;
            switch (repositoryFileAttributeType)
            {
            default:
                Map<RepositoryFileAttributeType, Object> attributesLocal;
                value = null;
                
                break;
            case CHECKSUM:
                value = isChecksum(repositoryPath);
                
                break;
            case INDEX:
                value = repositoryPath.isAbsolute()
                        && repositoryPath.startsWith(repositoryPath.getFileSystem()
                                                                   .getRootDirectory()
                                                                   .resolve(RepositoryFileSystem.INDEX));

                break;
            case TEMP:
                value = repositoryPath.isAbsolute()
                        && repositoryPath.startsWith(repositoryPath.getFileSystem()
                                                                   .getRootDirectory()
                                                                   .resolve(RepositoryFileSystem.TEMP));

                break;
            case TRASH:
                value = repositoryPath.isAbsolute()
                        && repositoryPath.startsWith(repositoryPath.getFileSystem()
                                                                   .getRootDirectory()
                                                                   .resolve(RepositoryFileSystem.TRASH));
                
                break;
            case METADATA:
                value = isMetadata(RepositoryFiles.stringValue(repositoryPath));
                
                break;
            case ARTIFACT:
                attributesLocal = getRepositoryFileAttributes(repositoryPath,
                                                              RepositoryFileAttributeType.METADATA,
                                                              RepositoryFileAttributeType.INDEX,
                                                              RepositoryFileAttributeType.CHECKSUM);
                
                boolean isMetadata = Boolean.TRUE.equals(attributesLocal.get(RepositoryFileAttributeType.METADATA));
                boolean isIndex = Boolean.TRUE.equals(attributesLocal.get(RepositoryFileAttributeType.INDEX));
                boolean isChecksum = Boolean.TRUE.equals(attributesLocal.get(RepositoryFileAttributeType.CHECKSUM));
                boolean isDirectory = Files.isDirectory(repositoryPath.getTarget());
                
                value = !isChecksum && !isIndex && !isMetadata && !isDirectory;
                
                break;
            case COORDINATES:
                attributesLocal = getRepositoryFileAttributes(repositoryPath,
                                                              RepositoryFileAttributeType.ARTIFACT);
                
                boolean isArtifact = Boolean.TRUE.equals(attributesLocal.get(RepositoryFileAttributeType.ARTIFACT));
                
                value = isArtifact ? getArtifactCoordinates(RepositoryFiles.stringValue(repositoryPath)) : null;
                break;

            }
            if (value != null)
            {
                result.put(repositoryFileAttributeType, value);
            }
        }

        return result;
    }
    
    protected RepositoryPathHandler getRepositoryPathHandler()
    {
        return null;
    }
    
    public class RepositoryLayoutFileSystem extends RepositoryFileSystem
    {

        public RepositoryLayoutFileSystem(Repository repository,
                                          FileSystem storageFileSystem,
                                          RepositoryFileSystemProvider provider)
        {
            super(repository, storageFileSystem, provider);
        }

        @Override
        public Set<String> getDigestAlgorithmSet()
        {
            return AbstractLayoutProvider.this.getDigestAlgorithmSet();
        }

    }

}
