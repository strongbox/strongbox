package org.carlspring.strongbox.storage.validation.version;

import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.repository.RepositoryProviderRegistry;
import org.carlspring.strongbox.providers.storage.StorageProvider;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.carlspring.strongbox.providers.storage.StorageProviderRegistry.getStorageProvider;

/**
 * @author mtodorov
 */
@Component("redeploymentValidator")
public class RedeploymentValidator implements VersionValidator
{


    @Autowired
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Autowired
    private StorageProviderRegistry storageProviderRegistry;


    @Override
    public void validate(Repository repository,
                         Artifact artifact)
            throws VersionValidationException,
                   ProviderImplementationException
    {
        StorageProvider storageProvider = getStorageProvider(repository, storageProviderRegistry);

        if (repository.getPolicy().equals(RepositoryPolicyEnum.RELEASE.getPolicy()) &&
            (!repository.allowsRedeployment() && storageProvider.containsArtifact(repository, artifact)))
        {
            throw new VersionValidationException("The " + repository.getStorage().getId() + ":" + repository.toString() +
                                                 " repository does not allow artifact re-deployment! (" +
                                                 ArtifactUtils.convertArtifactToPath(artifact) + ")");
        }
    }

}
