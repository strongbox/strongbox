package org.carlspring.strongbox.providers.layout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.spi.FileSystemProvider;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryFileSystemProvider;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.providers.storage.StorageProvider;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.repository.RepositoryFeatures;
import org.carlspring.strongbox.repository.RepositoryManagementStrategy;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.util.ArtifactFileUtils;
import org.carlspring.strongbox.util.MessageDigestUtils;
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
            throws IOException,
                   NoSuchAlgorithmException
    {
        Repository repository = getRepository(storageId, repositoryId);
        StorageProvider storageProvider = getStorageProvider(repository);

        RepositoryPath repositoryPath = resolve(repository, path);
        if (!Files.exists(repositoryPath))
        {
            throw new FileNotFoundException(repositoryPath.toString());
        }
        if (Files.isDirectory(repositoryPath))
        {
            throw new FileNotFoundException(String.format("The artifact path is a directory: [%s]",
                                                          repositoryPath.toString()));
        }
        
        T artifactCoordinates = (T) Files.getAttribute(repositoryPath, RepositoryFileAttributes.COORDINATES);
        
        InputStream is;
        if (artifactCoordinates != null)
        {
            is = storageProvider.getInputStreamImplementation(repositoryPath);
        }
        else
        {
            is = storageProvider.getInputStreamImplementation(repositoryPath);
        }

        logger.debug("Resolved " + path + "!");

        return decorateStream(storageId, repositoryId, path, is, artifactCoordinates);
    }

    protected StorageProvider getStorageProvider(Repository repository)
    {
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());
        return storageProvider;
    }

    protected Repository getRepository(String storageId,
                                       String repositoryId)
    {
        Storage storage = getConfiguration().getStorage(storageId);

        logger.debug("Checking in " + storage.getId() + ":" + repositoryId + "...");

        Repository repository = storage.getRepository(repositoryId);
        return repository;
    }

    @Override
    public ArtifactOutputStream getOutputStream(String storageId,
                                                String repositoryId,
                                                String path)
            throws IOException,
                   NoSuchAlgorithmException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        StorageProvider storageProvider = getStorageProvider(repository);

        RepositoryPath repositoryPath = resolve(repository, path);
        if (Files.exists(repositoryPath) && Files.isDirectory(repositoryPath))
        {
            throw new FileNotFoundException(String.format("The artifact path is a directory: [%s]",
                                                          repositoryPath.toString()));
        }
        
        Files.createDirectories(repositoryPath.getParent());
        T artifactCoordinates = (T) Files.getAttribute(repositoryPath, RepositoryFileAttributes.COORDINATES);
        
        OutputStream os;
        if (artifactCoordinates != null)
        {
            os = storageProvider.getOutputStreamImplementation(repositoryPath);
        }
        else
        {
            os = storageProvider.getOutputStreamImplementation(repositoryPath);
        }
       
        return decorateStream(path, os, artifactCoordinates);
    }

    @Override
    public boolean isExistChecksum(Repository repository,
                                   String path)
    {
        return getDigestAlgorithmSet()
                       .stream()
                       .map(algorithm ->
                            {
                                String checksumPath = path.concat(".")
                                                          .concat(algorithm.toLowerCase()
                                                                           .replaceAll(
                                                                                   "-",
                                                                                   ""));
                                RepositoryPath checksum = null;
                                try
                                {
                                    checksum = resolve(repository, checksumPath);
                                }
                                catch (IOException e)
                                {
                                    logger.error(e.getMessage());
                                }

                                return checksum;
                            })
                       .allMatch(checksum -> Files.exists(checksum));

    }

    protected ArtifactOutputStream decorateStream(String path,
                                                  OutputStream os,
                                                  T artifactCoordinates)
            throws NoSuchAlgorithmException
    {
        ArtifactOutputStream result = new ArtifactOutputStream(os, artifactCoordinates);
        // Add digest algorithm only if it is not a Checksum (we don't need a Checksum of Checksum).
        if (!ArtifactFileUtils.isChecksum(path))
        {
            getDigestAlgorithmSet().stream()
                                   .forEach(e ->
                                            {
                                                try
                                                {
                                                    result.addAlgorithm(e);
                                                }
                                                catch (NoSuchAlgorithmException t)
                                                {
                                                    logger.error(
                                                            String.format("Digest algorithm not supported: alg-[%s]",
                                                                          e), t);
                                                }
                                            });
        }
        return result;
    }

    protected ArtifactInputStream decorateStream(String storageId,
                                                 String repositoryId,
                                                 String path,
                                                 InputStream is,
                                                 T artifactCoordinates)
            throws NoSuchAlgorithmException
    {
        ArtifactInputStream result = new ArtifactInputStream(artifactCoordinates, is, getDigestAlgorithmSet());
        // Add digest algorithm only if it is not a Checksum (we don't need a Checksum of Checksum).
        if (!ArtifactFileUtils.isChecksum(path))
        {
            getDigestAlgorithmSet().stream()
                                   .forEach(a ->
                                            {
                                                String checksum = getChecksum(storageId, repositoryId, path, result, a);
                                                if (checksum == null)
                                                {
                                                    return;
                                                }

                                                result.getHexDigests().put(a, checksum);
                                            });
        }
        return result;
    }

    private String getChecksum(String storageId,
                               String repositoryId,
                               String path,
                               ArtifactInputStream is,
                               String digestAlgorithm)
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        String checksumExtension = ".".concat(digestAlgorithm.toLowerCase().replaceAll("-", ""));
        String checksumPath = path.concat(checksumExtension);
        String checksum = null;

        try
        {
            if (Files.exists(resolve(repository, checksumPath)) && new File(checksumPath).length() != 0)
            {
                checksum = MessageDigestUtils.readChecksumFile(getInputStream(storageId, repositoryId, checksumPath));
            }
            else
            {
                checksum = is.getMessageDigestAsHexadecimalString(digestAlgorithm);
            }
        }
        catch (IOException | NoSuchAlgorithmException e)
        {
            logger.error(String.format("Failed to read checksum: alg-[%s]; path-[%s];",
                                       digestAlgorithm, path + "." + checksumExtension), e);
        }

        return checksum;
    }

    public Set<String> getDigestAlgorithmSet()
    {
        return Stream.of(MessageDigestAlgorithms.MD5, MessageDigestAlgorithms.SHA_1)
                     .collect(Collectors.toSet());
    }

    @Override
    public RepositoryPath resolve(Repository repository,
                                ArtifactCoordinates coordinates)
        throws IOException
    {
        RepositoryFileSystem repositoryFileSystem = getRepositoryFileSystem(repository);
        RepositoryPath repositoryPath = repositoryFileSystem.getRootDirectory().resolve(coordinates.toPath());
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
        FileSystem storageFileSystem = getStorageProvider(repository).getFileSistem();
        RepositoryFileSystem repositoryFileSystem = new RepositoryLayoutFileSystem(repository, storageFileSystem,
                getProvider(repository));
        return repositoryFileSystem;
    }

    @Override
    public RepositoryPath resolve(Repository repository,
                                  String path)
        throws IOException
    {
        return resolve(repository).resolve(path);
    }

    public RepositoryFileSystemProvider getProvider(Repository repository)
    {
        FileSystemProvider storageFileSystemProvider = getStorageProvider(repository).getFileSystemProvider();
        RepositoryFileSystemProvider repositoryFileSystemProvider = new RepositoryLayoutFileSystemProvider(
                storageFileSystemProvider);
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
        // TODO: Implement
    }

    @Override
    public void move(String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId,
                     String path)
            throws IOException
    {
        // TODO: Implement
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

        RepositoryPath repositoryPath = resolve(repository, path);

        logger.debug("Checking in " + storageId + ":" + repositoryId + "(" + path + ")...");
        if (!Files.exists(repositoryPath))
        {
            logger.warn(String.format("Path not found: path-[%s]", repositoryPath));
            return;
        }
        
        RepositoryFileSystemProvider provider = getProvider(repository);
        provider.setAllowsForceDelete(force);
        provider.delete(repositoryPath);
        
        logger.debug("Removed /" + repositoryId + "/" + path);
    }

    @Override
    public void deleteTrash(String storageId,
                            String repositoryId)
            throws IOException
    {
        logger.debug("Emptying trash for repositoryId " + repositoryId + "...");

        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        RepositoryPath path = resolve(repository);

        getProvider(repository).deleteTrash(path);
    }

    @Override
    public void deleteTrash()
            throws IOException
    {
        for (Map.Entry entry : getConfiguration().getStorages()
                                                 .entrySet())
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
            }
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
        RepositoryPath artifactPath = resolve(repository, path);
        RepositoryFileSystemProvider provider = getProvider(repository);
        
        provider.undelete(artifactPath);
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
    }

    @Override
    public void undeleteTrash()
            throws IOException
    {
        for (Map.Entry entry : getConfiguration().getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            final Map<String, Repository> repositories = storage.getRepositories();
            for (Repository repository : repositories.values())
            {
                undeleteTrash(storage.getId(), repository.getId());
            }
        }
    }

    @Override
    public boolean contains(String storageId,
                            String repositoryId,
                            String path)
            throws IOException
    {
        Repository repository = getRepository(storageId, repositoryId);
        RepositoryPath artifactPath = resolve(repository, path);
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

    protected void storeChecksum(Repository repository,
                                 RepositoryPath basePath,
                                 boolean forceRegeneration)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException

    {
        File[] files = basePath.toFile().listFiles();

        if (files != null)
        {
            List<File> list = Arrays.asList(files);

            list.stream()
                .filter(File::isFile)
                .filter(e -> !ArtifactFileUtils.isChecksum(e.getPath()))
                .forEach(e ->
                         {
                             if (!isExistChecksum(repository, e.getPath()) || forceRegeneration)
                             {
                                 ArtifactInputStream is = null;
                                 try
                                 {
                                     String artifactPath = e.getPath().substring(repository.getBasedir().length() + 1);
                                     is = getInputStream(repository.getStorage().getId(),
                                                         repository.getId(),
                                                         artifactPath);
                                 }
                                 catch (IOException | NoSuchAlgorithmException e1)
                                 {
                                     logger.error(e1.getMessage(), e1);
                                 }

                                 writeChecksum(is, e);
                             }
                         });
        }
    }

    private void writeChecksum(ArtifactInputStream is,
                               File filePath)

    {
        getDigestAlgorithmSet().stream()
                               .forEach(e ->
                                        {
                                            String checksum = is.getHexDigests()
                                                                .get(e);
                                            String checksumExtension = ".".concat(e.toLowerCase().replaceAll("-", ""));

                                            try
                                            {
                                                MessageDigestUtils.writeChecksum(filePath, checksumExtension, checksum);
                                            }
                                            catch (IOException e1)
                                            {
                                                logger.error(
                                                        String.format("Failed to write checksum: alg-[%s]; path-[%s];",
                                                                      e, filePath + "." + checksumExtension), e1);
                                            }
                                        });
    }

    protected Map<String, Object> getRepositoryFileAttributes(RepositoryPath repositoryRelativePath)
    {
        RepositoryFileSystemProvider provider = repositoryRelativePath.getFileSystem().provider();

        Map<String, Object> result = new HashMap<>();
        boolean isChecksum = provider.isChecksum(repositoryRelativePath);
        result.put(RepositoryFileAttributes.CHECKSUM, isChecksum);
        boolean isIndex = repositoryRelativePath.startsWith(".index");
        result.put(RepositoryFileAttributes.INDEX, isIndex);
        boolean isTemp = repositoryRelativePath.startsWith(".temp");
        result.put(RepositoryFileAttributes.TEMP, isTemp);
        boolean isTrash = repositoryRelativePath.startsWith(".trash");
        result.put(RepositoryFileAttributes.TRASH, isTrash);
        boolean isMetadata = isMetadata(repositoryRelativePath.toString());
        result.put(RepositoryFileAttributes.METEDATA, isMetadata);

        Boolean isArtifact = !Files.isDirectory(repositoryRelativePath.getTarget()) && !isChecksum && !isIndex
                && !isTemp && !isTrash && !isMetadata;
        result.put(RepositoryFileAttributes.ARTIFACT, isArtifact);
        if (isArtifact)
        {
            result.put(RepositoryFileAttributes.COORDINATES, getArtifactCoordinates(repositoryRelativePath.toString()));
        }
        return result;
    }
    
    protected abstract boolean isMetadata(String string);
    
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

    public class RepositoryLayoutFileSystemProvider extends RepositoryFileSystemProvider
    {

        public RepositoryLayoutFileSystemProvider(FileSystemProvider storageFileSystemProvider)
        {
            super(storageFileSystemProvider);
        }

        @Override
        protected Map<String, Object> getRepositoryFileAttributes(RepositoryPath repositoryRelativePath)
        {
            return AbstractLayoutProvider.this.getRepositoryFileAttributes(repositoryRelativePath);
        }

    }
    
}
