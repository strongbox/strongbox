package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import java.io.IOException;

import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("redeploymentValidator")
public class RedeploymentValidator
        implements VersionValidator
{

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;


    @Override
    public void validate(Repository repository,
                         ArtifactCoordinates coordinates)
            throws VersionValidationException,
                   ProviderImplementationException,
                   IOException
    {
        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        if (RepositoryPolicyEnum.RELEASE.getPolicy().equals(repository.getPolicy()) &&
            (!repository.allowsRedeployment() && layoutProvider.containsArtifact(repository, coordinates)))
        {
            throw new VersionValidationException("The " + repository.getStorage().getId() + ":" +
                                                 repository.toString() +
                                                 " repository does not allow artifact re-deployment! (" +
                                                 coordinates.toPath() + ")");
        }
    }

}
