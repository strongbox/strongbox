package org.carlspring.strongbox.storage.validation.version;

import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.services.BasicRepositoryService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("redeploymentValidator")
public class RedeploymentValidator implements VersionValidator
{


    @Autowired
    private BasicRepositoryService basicRepositoryService;


    @Override
    public void validate(Repository repository,
                         Artifact artifact)
            throws VersionValidationException
    {
        if (!repository.allowsRedeployment() && basicRepositoryService.containsArtifact(repository, artifact))
        {
            throw new VersionValidationException("The " + repository.getStorage().getId() + ":" + repository.toString() +
                                                 " repository does not allow artifact re-deployment! (" +
                                                 ArtifactUtils.convertArtifactToPath(artifact) + ")");
        }
    }

}
