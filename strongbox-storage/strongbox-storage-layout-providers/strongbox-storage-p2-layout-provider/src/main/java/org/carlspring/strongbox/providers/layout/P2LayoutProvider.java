package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.P2ArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.p2.P2ArtifactReader;
import org.carlspring.strongbox.repository.P2RepositoryFeatures;
import org.carlspring.strongbox.repository.P2RepositoryManagementStrategy;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.impl.P2ArtifactManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.UnknownRepositoryTypeException;

import javax.inject.Inject;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P2LayoutProvider
        extends AbstractLayoutProvider<P2ArtifactCoordinates,
                                              P2RepositoryFeatures,
                                              P2RepositoryManagementStrategy>
{

    private static final Logger logger = LoggerFactory.getLogger(P2LayoutProvider.class);

    public static final String ALIAS = "P2 Repository";

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private P2RepositoryFeatures p2RepositoryFeatures;

    @Inject
    private P2RepositoryManagementStrategy p2RepositoryManagementStrategy;

    @Inject
    private P2ArtifactManagementService p2ArtifactManagementService;


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

    protected boolean isMetadata(String path)
    {
        return "content.xml".equals(path) || "artifacts.xml".equals(path) || "artifacts.jar".equals(path) ||
               "content.jar".equals(path);
    }

    @Override
    public void deleteMetadata(String storageId,
                               String repositoryId,
                               String metadataPath)
            throws IOException
    {

    }

    @Override
    public void rebuildMetadata(String storageId,
                                String repositoryId,
                                String basePath)
            throws IOException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void rebuildIndexes(String storageId,
                               String repositoryId,
                               String basePath,
                               boolean forceRegeneration)
            throws IOException
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void regenerateChecksums(Repository repository,
                                    List<String> versionDirectories,
                                    boolean forceRegeneration)
            throws IOException,
                   NoSuchAlgorithmException,
                   ProviderImplementationException,
                   UnknownRepositoryTypeException,
                   ArtifactTransportException
    {
        throw new UnsupportedOperationException("Not yet implemented!");
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
    public FilenameFilter getMetadataFilenameFilter()
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public P2RepositoryFeatures getRepositoryFeatures()
    {
        return p2RepositoryFeatures;
    }

    @Override
    public P2RepositoryManagementStrategy getRepositoryManagementStrategy()
    {
        return p2RepositoryManagementStrategy;
    }

    @Override
    public ArtifactManagementService getArtifactManagementService()
    {
        return p2ArtifactManagementService;
    }

}
