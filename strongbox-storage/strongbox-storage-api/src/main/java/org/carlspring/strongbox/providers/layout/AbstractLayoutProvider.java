package org.carlspring.strongbox.providers.layout;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.io.ArtifactPath;
import org.carlspring.strongbox.io.RepositoryFileSystemProvider;
import org.carlspring.strongbox.io.RepositoryPath;
import org.carlspring.strongbox.providers.storage.StorageProvider;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author mtodorov
 */
public abstract class AbstractLayoutProvider<T extends ArtifactCoordinates> implements LayoutProvider<T>
{
    private static final Logger logger = LoggerFactory.getLogger(AbstractLayoutProvider.class);

    @Autowired
    protected LayoutProviderRegistry layoutProviderRegistry;

    @Autowired
    protected StorageProviderRegistry storageProviderRegistry;

    @Autowired
    private ConfigurationManager configurationManager;


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

    @Override
    public ArtifactInputStream getInputStream(String storageId,
                                              String repositoryId,
                                              String path)
            throws IOException, NoSuchAlgorithmException
    {
        Storage storage = getConfiguration().getStorage(storageId);

        logger.debug("Checking in " + storage.getId() + ":" + repositoryId + "...");

        Repository repository = storage.getRepository(repositoryId);
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

        ArtifactInputStream ais;
        if (isArtifact(repository, path, true))
        {
            ArtifactPath artifactPath = resolve(repository, getArtifactCoordinates(path));
            ais = storageProvider.getInputStreamImplementation(artifactPath);
        }
        else
        {
            RepositoryPath repositoryPath = resolve(repository);
            ais = storageProvider.getInputStreamImplementation(repositoryPath, path);
        }
        logger.debug("Resolved " + path + "!");
        return ais;
    }

    @Override
    public ArtifactOutputStream getOutputStream(String storageId,
                                                String repositoryId,
                                                String path)
        throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());
        OutputStream os;
        if (isArtifact(repository, path, false))
        {
            ArtifactPath artifactPath = resolve(repository, getArtifactCoordinates(path));
            os = storageProvider.getOutputStreamImplementation(artifactPath);
        }
        else
        {
            RepositoryPath repositoryPath = resolve(repository);
            os = storageProvider.getOutputStreamImplementation(repositoryPath, path);
        }

        return os instanceof ArtifactOutputStream ? (ArtifactOutputStream) os : new ArtifactOutputStream(os, null);
    }
    
    protected abstract boolean isMetadata(String path);
    
    protected abstract boolean isChecksum(String path);
    
    protected boolean isTrash(String path){
        return path.contains(".trash");
    }
    
    protected boolean isTemp(String path){
        return path.contains(".temp");
    }

    protected boolean isIntex(String path){
        return path.contains(".index");
    }
    
    protected boolean isArtifact(Repository repository, String path, boolean strict) throws IOException{
        RepositoryPath artifactPath = resolve(repository, path);
        boolean exists = Files.exists(artifactPath);
        if (!exists && strict){
            throw new FileNotFoundException(artifactPath.toString());
        }
        if (exists && Files.isDirectory(artifactPath)){
            throw new FileNotFoundException(String.format("This is directory: path-[%s]", artifactPath.toString()));
        }
        return !isMetadata(path) && !isChecksum(path) && !isServiceFolder(path);
    }
    
    protected boolean isServiceFolder(String path){
        return isTemp(path) || isTrash(path) || isIntex(path);
    }
    
    protected RepositoryPath resolve(Repository repository)
        throws IOException
    {
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());
        return storageProvider.resolve(repository);
    }

    protected RepositoryPath resolve(Repository repository,
                                     String path)
        throws IOException
    {
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());
        return storageProvider.resolve(repository, path);
    }
    
    protected ArtifactPath resolve(String storageId,
                                 String repositoryId,
                                 String path)
        throws IOException
    {
        return resolve(storageId, repositoryId, getArtifactCoordinates(path));
    }

    protected ArtifactPath resolve(Repository repository,
                                   ArtifactCoordinates coordinates)
        throws IOException
    {
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());
        return storageProvider.resolve(repository, coordinates);
    }

    protected ArtifactPath resolve(String storageId,
                                   String repositoryId,
                                   ArtifactCoordinates coordinates)
        throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        return resolve(repository, coordinates);
    }

    protected RepositoryFileSystemProvider getProvider(RepositoryPath artifactPath)
    {
        return (RepositoryFileSystemProvider) artifactPath.getFileSystem().provider();
    }

}
