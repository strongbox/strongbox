package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("redeploymentValidator")
public class RedeploymentValidator
        implements VersionValidator
{

    @Autowired
    private LayoutProviderRegistry layoutProviderRegistry;


    @Override
    public void validate(Repository repository,
                         Artifact artifact)
            throws VersionValidationException,
                   ProviderImplementationException,
                   IOException
    {
        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        if (repository.getPolicy().equals(RepositoryPolicyEnum.RELEASE.getPolicy()) &&
            (!repository.allowsRedeployment() && layoutProvider.containsArtifact(repository, artifact)))
        {
            throw new VersionValidationException("The " + repository.getStorage().getId() + ":" +
                                                 repository.toString() +
                                                 " repository does not allow artifact re-deployment! (" +
                                                 ArtifactUtils.convertArtifactToPath(artifact) + ")");
        }
    }

}
