package org.carlspring.strongbox.providers.layout;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.NugetHierarchicalArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactFile;
import org.carlspring.strongbox.io.ArtifactFileOutputStream;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.providers.storage.StorageProvider;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Layout provider for Nuget package repository.<br>
 * It provides hierarchical directory layout like follows: <br>
 * &lt;packageID&gt;<br>
 * └─&lt;version&gt;<br>
 * &emsp;├─&lt;packageID&gt;.&lt;version&gt;.nupkg<br>
 * &emsp;├─&lt;packageID&gt;.&lt;version&gt;.nupkg.sha512<br>
 * &emsp;└─&lt;packageID&gt;.nuspec<br>
 * 
 * 
 * @author Sergey Bespalov
 *
 */
@Component
public class NugetHierarchicalLayoutProvider extends AbstractLayoutProvider<NugetHierarchicalArtifactCoordinates>
{
    private static final Logger logger = LoggerFactory.getLogger(NugetHierarchicalLayoutProvider.class);

    public static final String ALIAS = "Nuget Hierarchical";

    @Override
    @PostConstruct
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
    public NugetHierarchicalArtifactCoordinates getArtifactCoordinates(String path)
    {
        return new NugetHierarchicalArtifactCoordinates(path);
    }

    @Override
    public ArtifactInputStream getInputStream(String storageId,
                                              String repositoryId,
                                              String path)
        throws IOException,
        NoSuchAlgorithmException,
        ArtifactTransportException
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

        NugetHierarchicalArtifactCoordinates coordinates = new NugetHierarchicalArtifactCoordinates(path);
        ArtifactFile artifactFile = new ArtifactFile(repository, coordinates);
        artifactFile.createParents();

        return new ArtifactFileOutputStream(artifactFile);
    }

    @Override
    public boolean containsArtifact(Repository repository,
                                    ArtifactCoordinates coordinates)
        throws IOException
    {
        return false;
    }

    @Override
    public boolean contains(String storageId,
                            String repositoryId,
                            String path)
        throws IOException
    {
        return false;
    }

    @Override
    public boolean containsPath(Repository repository,
                                String path)
        throws IOException
    {
        return false;
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