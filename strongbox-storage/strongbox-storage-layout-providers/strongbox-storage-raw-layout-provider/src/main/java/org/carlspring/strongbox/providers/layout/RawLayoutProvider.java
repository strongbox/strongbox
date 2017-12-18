package org.carlspring.strongbox.providers.layout;


import org.carlspring.strongbox.artifact.coordinates.NullArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathHandler;
import org.carlspring.strongbox.repository.RawRepositoryFeatures;
import org.carlspring.strongbox.repository.RawRepositoryManagementStrategy;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.impl.RawArtifactManagementService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("rawLayoutProvider")
public class RawLayoutProvider
        extends AbstractLayoutProvider<NullArtifactCoordinates,
                                       RawRepositoryFeatures,
                                       RawRepositoryManagementStrategy>
        implements RepositoryPathHandler
{

    public static final String ALIAS = "Raw";

    private static final Logger logger = LoggerFactory.getLogger(RawLayoutProvider.class);
    
    @Inject
    private RawArtifactManagementService rawArtifactManagementService;

    @Inject
    private RawRepositoryManagementStrategy rawRepositoryManagementStrategy;

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
    public NullArtifactCoordinates getArtifactCoordinates(String path)
    {
        return new NullArtifactCoordinates(path);
    }

    @Override
    public void deleteMetadata(String storageId,
                               String repositoryId,
                               String metadataPath)
            throws IOException
    {
        // Note: There's no known metadata for this format, hence no action is taken here
    }

    @Override
    public void rebuildMetadata(String storageId,
                                String repositoryId,
                                String basePath)
            throws IOException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        // Note: There's no known metadata for this format, hence no action is taken here
    }

    @Override
    public void rebuildIndexes(String storageId,
                               String repositoryId,
                               String basePath,
                               boolean forceRegeneration)
            throws IOException
    {
        // Note: Artifacts aren't being added to an index of any form for this format, hence no action is taken here
    }

    @Override
    public boolean isMetadata(String path)
    {
        // Note: There's no known metadata for this format, hence no action is taken here
        // TODO: Implement
        return false;
    }

    @Override
    public void postProcess(RepositoryPath repositoryPath)
            throws IOException
    {
    }

    @Override
    public RawRepositoryManagementStrategy getRepositoryManagementStrategy()
    {
        return rawRepositoryManagementStrategy;
    }

    @Override
    public ArtifactManagementService getArtifactManagementService()
    {
        return rawArtifactManagementService;
    }

    protected RepositoryPathHandler getRepositoryPathHandler()
    {
        return this;
    }

}
