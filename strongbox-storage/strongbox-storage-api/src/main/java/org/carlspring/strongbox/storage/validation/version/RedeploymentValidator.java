package org.carlspring.strongbox.storage.validation.version;

import org.apache.maven.artifact.Artifact;
import org.carlspring.strongbox.storage.repository.Repository;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class RedeploymentValidator implements VersionValidator
{


    @Override
    public void validate(Repository repository,
                         Artifact artifact)
            throws VersionValidationException
    {
        if (!repository.allowsRedeployment() && repository.containsArtifact(artifact))
        {
            throw new VersionValidationException("This repository does not allow artifact re-deployment!");
        }
    }

}
