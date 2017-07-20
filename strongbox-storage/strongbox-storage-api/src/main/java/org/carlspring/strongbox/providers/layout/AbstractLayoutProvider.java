package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.event.repository.RepositoryEventListenerRegistry;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.io.*;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.providers.storage.StorageProvider;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.repository.RepositoryFeatures;
import org.carlspring.strongbox.repository.RepositoryManagementStrategy;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.spi.FileSystemProvider;
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
public abstract class AbstractLayoutProvider<T extends ArtifactCoordinates,
                                             U extends RepositoryFeatures,
                                             V extends RepositoryManagementStrategy>
        implements LayoutProvider<T>
{

    private static final Logger logger = LoggerFactory.getLogger(AbstractLayoutProvider.class);

    @Inject
    protected LayoutProviderRegistry layoutProviderRegistry;
    
    @Inject
    protected StorageProviderRegistry storageProviderRegistry;
    
    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private ArtifactEventListenerRegistry artifactEventListenerRegistry;

    @Inject
    private RepositoryEventListenerRegistry repositoryEventListenerRegistry;


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
                                  ArtifactCoordinates coordinates)
            throws IOException
    {
        RepositoryFileSystem repositoryFileSystem = getRepositoryFileSystem(repository);
        
        return repositoryFileSystem.getRootDirectory().resolve(coordinates.toPath());
    }

    @Override
    public RepositoryPath resolve(Repository repository)
            throws IOException
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
        artifactEventListenerRegistry.dispatchArtifactCopyingEvent(srcStorageId,
                                                                   srcRepositoryId,
                                                                   destStorageId,
                                                                   destRepositoryId,
                                                                   path);

        // TODO: Implement copying

        artifactEventListenerRegistry.dispatchArtifactCopiedEvent(srcStorageId,
                                                                  srcRepositoryId,
                                                                  destStorageId,
                                                                  destRepositoryId,
                                                                  path);
    }

    @Override
    public void move(String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId,
                     String path)
            throws IOException
    {
        artifactEventListenerRegistry.dispatchArtifactMovingEvent(srcStorageId,
                                                                  srcRepositoryId,
                                                                  destStorageId,
                                                                  destRepositoryId,
                                                                  path);

        // TODO: Implement moving

        artifactEventListenerRegistry.dispatchArtifactMovedEvent(srcStorageId,
                                                                 srcRepositoryId,
                                                                 destStorageId,
                                                                 destRepositoryId,
                                                                 path);
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

        logger.debug("Checking in " + storageId + ":" + repositoryId + "(" + path + ")...");
        
        if (!Files.exists(repositoryPath))
        {
            logger.warn(String.format("Path not found: path-[%s]", repositoryPath));
            
            return;
        }

        RepositoryFileSystemProvider provider = getProvider(repository);
        provider.setAllowsForceDelete(force);
        provider.delete(repositoryPath);

        artifactEventListenerRegistry.dispatchArtifactPathDeletedEvent(storageId, repositoryId, path);

        logger.debug("Removed /" + repositoryId + "/" + path);
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
            Storage storage = (Storage) entry.getValue();

            final Map<String, Repository> repositories = storage.getRepositories();
            for (Repository repository : repositories.values())
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
    public void undelete(String storageId,
                         String repositoryId,
                         String path)
            throws IOException
    {
        logger.debug(String.format("Attempting to restore: storageId-[%s]; repoId-[%s]; path-[%s]; ",
                                   storageId,
                                   repositoryId,
                                   path));

        Repository repository = getRepository(storageId, repositoryId);
        RepositoryPath artifactPath = resolve(repository).resolve(path);
        RepositoryFileSystemProvider provider = getProvider(repository);
        
        provider.undelete(artifactPath);

        repositoryEventListenerRegistry.dispatchUndeleteTrashEvent(storageId, repositoryId);

        logger.debug("The trash for " + storageId + ":" + repositoryId + " has been undeleted.");
    }

    @Override
    public void undeleteTrash(String storageId,
                              String repositoryId)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        logger.debug("Restoring all artifacts from the trash of " + storageId + ":" + repository.getId() + "...");

        if (!repository.isTrashEnabled())
        {
            logger.warn("Repository " + repository.getId() + " does not support removal of trash.");
        }

        RepositoryPath path = resolve(repository);
        getProvider(repository).undelete(path);

        repositoryEventListenerRegistry.dispatchUndeleteTrashEvent(storageId, repositoryId);

        logger.debug("The trash for all repositories in " + storageId + " has been undeleted.");
    }

    @Override
    public void undeleteTrash()
            throws IOException
    {
        boolean trashUndeleted = false;

        for (Map.Entry entry : getConfiguration().getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            final Map<String, Repository> repositories = storage.getRepositories();
            for (Repository repository : repositories.values())
            {
                undeleteTrash(storage.getId(), repository.getId());

                trashUndeleted = true;
            }
        }

        if (trashUndeleted)
        {
            repositoryEventListenerRegistry.dispatchUndeleteTrashForAllRepositoriesEvent();
        }
    }

    @Override
    public boolean contains(String storageId,
                            String repositoryId,
                            String path)
            throws IOException
    {
        Repository repository = getRepository(storageId, repositoryId);
        RepositoryPath artifactPath = resolve(repository).resolve(path);
        
        return Files.exists(artifactPath);
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
    
    protected Map<String, Object> getRepositoryFileAttributes(RepositoryPath repositoryRelativePath)
    {
        RepositoryFileSystemProvider provider = repositoryRelativePath.getFileSystem().provider();

        boolean isChecksum = provider.isChecksum(repositoryRelativePath);
        boolean isIndex = repositoryRelativePath.startsWith(".index");
        boolean isTemp = repositoryRelativePath.startsWith(".temp");
        boolean isTrash = repositoryRelativePath.startsWith(".trash");
        boolean isMetadata = isMetadata(repositoryRelativePath.toString());
        boolean isHidden = isTemp || isTrash || isMetadata;
        Boolean isArtifact = !isChecksum && !isIndex && !isHidden;

        Map<String, Object> result = new HashMap<>();
        result.put(RepositoryFileAttributes.CHECKSUM, isChecksum);
        result.put(RepositoryFileAttributes.INDEX, isIndex);
        result.put(RepositoryFileAttributes.TEMP, isTemp);
        result.put(RepositoryFileAttributes.TRASH, isTrash);
        result.put(RepositoryFileAttributes.METADATA, isMetadata);
        result.put(RepositoryFileAttributes.ARTIFACT, isArtifact);

        if (!Files.isDirectory(repositoryRelativePath.getTarget()) && isArtifact)
        {
            result.put(RepositoryFileAttributes.COORDINATES, getArtifactCoordinates(repositoryRelativePath.toString()));
        }
        
        return result;
    }
    
    protected abstract boolean isMetadata(String string);

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
