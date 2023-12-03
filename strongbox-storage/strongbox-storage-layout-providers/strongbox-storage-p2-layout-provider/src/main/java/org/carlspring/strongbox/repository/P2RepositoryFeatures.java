package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.storage.validation.artifact.version.GenericReleaseVersionValidator;
import org.carlspring.strongbox.storage.validation.artifact.version.GenericSnapshotVersionValidator;
import org.carlspring.strongbox.storage.validation.deployment.RedeploymentValidator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class P2RepositoryFeatures
        implements RepositoryFeatures
{

    @Inject
    private RedeploymentValidator redeploymentValidator;

    @Inject
    private GenericReleaseVersionValidator genericReleaseVersionValidator;

    @Inject
    private GenericSnapshotVersionValidator genericSnapshotVersionValidator;

    private Set<String> defaultArtifactCoordinateValidators;


    @PostConstruct
    public void init()
    {
        defaultArtifactCoordinateValidators = new LinkedHashSet<>(Arrays.asList(redeploymentValidator.getAlias(),
                                                                                genericReleaseVersionValidator.getAlias(),
                                                                                genericSnapshotVersionValidator.getAlias()));
    }

    @Override
    public Set<String> getDefaultArtifactCoordinateValidators()
    {
        return defaultArtifactCoordinateValidators;
    }

}
