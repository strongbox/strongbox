package org.carlspring.strongbox.providers.layout;


import java.io.IOException;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.NullArtifactCoordinates;
import org.carlspring.strongbox.providers.header.HeaderMappingRegistry;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repository.RawRepositoryFeatures;
import org.carlspring.strongbox.repository.RawRepositoryManagementStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("rawLayoutProvider")
public class RawLayoutProvider
        extends AbstractLayoutProvider<NullArtifactCoordinates>
{

    private static final Logger logger = LoggerFactory.getLogger(RawLayoutProvider.class);

    public static final String ALIAS = "Raw";

    public static final String USER_AGENT_PREFIX = "Raw";

    @Inject
    private HeaderMappingRegistry headerMappingRegistry;

    @Inject
    private RawRepositoryManagementStrategy rawRepositoryManagementStrategy;

    @Inject
    private RawRepositoryFeatures rawRepositoryFeatures;


    @PostConstruct
    public void register()
    {
        headerMappingRegistry.register(ALIAS, USER_AGENT_PREFIX);

        logger.info("Registered layout provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
    }

    protected NullArtifactCoordinates getArtifactCoordinates(RepositoryPath path) throws IOException
    {
        return new NullArtifactCoordinates(RepositoryFiles.relativizePath(path));
    }

    public boolean isArtifactMetadata(RepositoryPath path)
    {
        return false;
    }

    @Override
    public void deleteMetadata(RepositoryPath repositoryPath)
    {
        // Note: There's no known metadata for this format, hence no action is taken here
    }

    @Override
    public RawRepositoryManagementStrategy getRepositoryManagementStrategy()
    {
        return rawRepositoryManagementStrategy;
    }

    @Override
    public Set<String> getDefaultArtifactCoordinateValidators()
    {
        return rawRepositoryFeatures.getDefaultArtifactCoordinateValidators();
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

}
