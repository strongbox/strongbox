package org.carlspring.strongbox.storage.validation.deployment;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidatorRegistry;
import org.carlspring.strongbox.storage.validation.artifact.version.VersionValidationException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import org.carlspring.strongbox.providers.io.RepositoryFiles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("redeploymentValidator")
public class RedeploymentValidator
        implements ArtifactDeploymentValidator
{

    private static final Logger logger = LoggerFactory.getLogger(RedeploymentValidator.class);

    public static final String ALIAS = "redeployment-validator";

    public static final String DESCRIPTION = "Re-deployment validator";
    
    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private ArtifactCoordinatesValidatorRegistry artifactCoordinatesValidatorRegistry;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @PostConstruct
    @Override
    public void register()
    {
        artifactCoordinatesValidatorRegistry.addProvider(ALIAS, this);

        logger.info("Registered artifact coordinates validator '{}' with alias '{}'.",
                    getClass().getCanonicalName(), ALIAS);
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public String getDescription()
    {
        return DESCRIPTION;
    }

    @Override
    public void validate(Repository repository,
                         ArtifactCoordinates coordinates)
            throws VersionValidationException,
                   IOException
    {
        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, coordinates);
        
        if (repository.acceptsReleases() &&
            (!repository.allowsDeployment() && RepositoryFiles.artifactExists(repositoryPath)))
        {
            throw new VersionValidationException("The " + repository.getStorage().getId() + ":" +
                                                 repository.toString() +
                                                 " repository does not allow artifact re-deployment! (" +
                                                 coordinates.buildPath() + ")");
        }
    }

}
