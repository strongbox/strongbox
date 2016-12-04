package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.P2ArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactFile;
import org.carlspring.strongbox.io.ArtifactFileOutputStream;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.providers.layout.p2.P2ArtifactReader;
import org.carlspring.strongbox.providers.storage.StorageProvider;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

public class P2LayoutProvider
        extends AbstractLayoutProvider<P2ArtifactCoordinates>
{

    private static final Logger logger = LoggerFactory.getLogger(P2LayoutProvider.class);

    public static final String ALIAS = "P2 Repository";

    @Autowired
    private LayoutProviderRegistry layoutProviderRegistry;

    @Autowired
    private StorageProviderRegistry storageProviderRegistry;

    @Override
    public void register()
    {
        layoutProviderRegistry.addProvider(ALIAS, this);

        logger.info("Registered layout provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public P2ArtifactCoordinates getArtifactCoordinates(String path)
    {
        return P2ArtifactCoordinates.create(path);
    }

    @Override
    public ArtifactInputStream getInputStream(String storageId,
                                              String repositoryId,
                                              String path)
            throws IOException, NoSuchAlgorithmException, ArtifactTransportException
    {
        Storage storage = getConfiguration().getStorage(storageId);

        logger.debug("Checking in " + storage.getId() + ":" + repositoryId + "...");

        Repository repository = storage.getRepository(repositoryId);
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

        final File repoPath = storageProvider.getFileImplementation(storage.getRepository(repositoryId).getBasedir());
        final File artifactFile = storageProvider.getFileImplementation(repoPath.getPath(), path).getCanonicalFile();

        logger.debug(" -> Checking for " + artifactFile.getCanonicalPath() + "...");

        if (artifactFile.exists())
        {
            logger.debug("Resolved " + artifactFile.getCanonicalPath() + "!");

            ArtifactInputStream ais = storageProvider.getInputStreamImplementation(artifactFile.getAbsolutePath());
            ais.setLength(artifactFile.length());

            return ais;
        }

        return null;
    }

    @Override
    public OutputStream getOutputStream(String storageId,
                                        String repositoryId,
                                        String path)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

        final String repoPath = storageProvider.getFileImplementation(
                storage.getRepository(repositoryId).getBasedir()).getPath();
        ArtifactFile artifactFile = null;
        if (!"content.xml".equals(path) && !"artifacts.xml".equals(path) && !"artifacts.jar".equals(path) &&
            !"content.jar".equals(path))
        {
            P2ArtifactCoordinates artifact = P2ArtifactReader.getArtifact(repoPath, path);
            artifactFile = new ArtifactFile(new File(repoPath, artifact.getFilename()));
        }
        else
        {
            artifactFile = new ArtifactFile(storageProvider.getFileImplementation(repoPath, path).getCanonicalFile());
        }

        artifactFile.createParents();

        return new ArtifactFileOutputStream(artifactFile);
    }

    @Override
    public boolean containsArtifact(Repository repository,
                                    ArtifactCoordinates coordinates)
            throws IOException
    {
        if (coordinates != null)
        {
            P2ArtifactCoordinates artifact = P2ArtifactReader.getArtifact(repository.getBasedir(),
                                                                          coordinates.toPath());
            return coordinates.equals(artifact);
        }
        return false;
    }

    @Override
    public boolean contains(String storageId,
                            String repositoryId,
                            String path)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        return containsArtifact(repository, P2ArtifactCoordinates.create(path));
    }

    @Override
    public boolean containsPath(Repository repository,
                                String path)
            throws IOException
    {
        return containsArtifact(repository, P2ArtifactCoordinates.create(path));
    }

    @Override
    public String getPathToArtifact(Repository repository,
                                    ArtifactCoordinates coordinates)
            throws IOException
    {
        return null;
    }

    @Override
    public void copy(String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId,
                     String path)
            throws IOException
    {


    }

    @Override
    public void move(String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId,
                     String path)
            throws IOException
    {

    }

    @Override
    public void delete(String storageId,
                       String repositoryId,
                       String path,
                       boolean force)
            throws IOException
    {

    }

    @Override
    public void deleteMetadata(String storageId,
                               String repositoryId,
                               String metadataPath)
            throws IOException
    {

    }

    @Override
    public void deleteTrash(String storageId,
                            String repositoryId)
            throws IOException
    {

    }

    @Override
    public void deleteTrash()
            throws IOException
    {

    }

    @Override
    public void undelete(String storageId,
                         String repositoryId,
                         String path)
            throws IOException
    {

    }

    @Override
    public void undeleteTrash(String storageId,
                              String repositoryId)
            throws IOException
    {

    }

    @Override
    public void undeleteTrash()
            throws IOException
    {

    }
}
