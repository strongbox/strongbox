package org.carlspring.strongbox.locator.handlers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.metadata.VersionCollectionRequest;
import org.carlspring.strongbox.storage.repository.UnknownRepositoryTypeException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class GenerateMavenMetadataOperation
        extends AbstractMavenArtifactLocatorOperation
{

    private static final Logger logger = LoggerFactory.getLogger(GenerateMavenMetadataOperation.class);

    private MavenMetadataManager mavenMetadataManager;


    public GenerateMavenMetadataOperation()
    {
    }

    @Override
    public void executeOperation(VersionCollectionRequest request,
                                 RepositoryPath artifactPath,
                                 List<RepositoryPath> versionDirectories)
    {
        try
        {
            mavenMetadataManager.generateMetadata(artifactPath.getFileSystem().getRepository(), artifactPath.toString(), request);
        }
        catch (IOException |
                       XmlPullParserException |
                       NoSuchAlgorithmException |
                       ProviderImplementationException |
                       UnknownRepositoryTypeException e)
        {
            logger.error("Failed to generate metadata for " + artifactPath, e);
        }

    }

    public GenerateMavenMetadataOperation(MavenMetadataManager mavenMetadataManager)
    {
        this.mavenMetadataManager = mavenMetadataManager;
    }

    public MavenMetadataManager getMavenMetadataManager()
    {
        return mavenMetadataManager;
    }

    public void setMavenMetadataManager(MavenMetadataManager mavenMetadataManager)
    {
        this.mavenMetadataManager = mavenMetadataManager;
    }

}
