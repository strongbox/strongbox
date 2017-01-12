package org.carlspring.strongbox.providers.layout;

import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.io.RepositoryPath;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.Repository;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("maven2LayoutProvider")
public class Maven2LayoutProvider extends AbstractLayoutProvider<MavenArtifactCoordinates>
{

    private static final Logger logger = LoggerFactory.getLogger(Maven2LayoutProvider.class);

    public static final String ALIAS = "Maven 2";

    @Autowired
    private MavenMetadataManager mavenMetadataManager;


    @PostConstruct
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
    public MavenArtifactCoordinates getArtifactCoordinates(String path)
    {
        MavenArtifactCoordinates coordinates = null;
        if (isMetadata(path))
        {
            Artifact artifact = ArtifactUtils.convertPathToArtifact(path);
            coordinates = new MavenArtifactCoordinates(artifact);
        }
        else
        {
            coordinates = new MavenArtifactCoordinates(path);
        }
        return coordinates;
    }


    protected boolean isMetadata(String path)
    {
        return ArtifactUtils.isMetadata(path);
    }

    @Override
    protected void doDeletePath(RepositoryPath repositoryPath,
                                boolean force,
                                boolean deleteChecksum)
        throws IOException
    {
        RepositoryPath md5Path = repositoryPath.resolveSibling(repositoryPath.getFileName() + ".md5");
        RepositoryPath sha1Path = repositoryPath.resolveSibling(repositoryPath.getFileName() + ".sha1");
        super.doDeletePath(repositoryPath, force, deleteChecksum);
        if (deleteChecksum)
        {
            super.doDeletePath(md5Path, force, deleteChecksum);
            super.doDeletePath(sha1Path, force, deleteChecksum);
        }
    }
    
    @Override
    public void deleteMetadata(String storageId,
                               String repositoryId,
                               String metadataPath)
        throws IOException
    {
        // TODO: Further untangle the relationships of this so that the code below can be uncommented:

        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        RepositoryPath repositoryPath = resolve(repository);
        if (!Files.isDirectory(repositoryPath))
        {
            return;
        }

        try
        {
            String version = repositoryPath.getFileName().toString();
            java.nio.file.Path path = repositoryPath.getParent();
            Metadata metadata = mavenMetadataManager.readMetadata(path);
            if (metadata != null && metadata.getVersioning() != null
                    && metadata.getVersioning().getVersions().contains(version))
            {
                metadata.getVersioning().getVersions().remove(version);
                mavenMetadataManager.storeMetadata(path, null, metadata, MetadataType.ARTIFACT_ROOT_LEVEL);
            }
        }
        catch (IOException | NoSuchAlgorithmException | XmlPullParserException e)
        {
            // We won't do anything in this case because it doesn't have an impact to the deletion
        }
    }

}
