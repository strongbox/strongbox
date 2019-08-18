package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.storage.validation.deployment.RedeploymentValidator;
import org.carlspring.strongbox.testing.storage.repository.RepositorySetup;

import java.util.Collections;

import com.google.common.collect.Sets;

/**
 * @author Pablo Tirado
 */
public class MavenRedeploymentValidatorRepositorySetup
        implements RepositorySetup
{
    @Override
    public void setup(RepositoryDto repository)
    {
        repository.setArtifactCoordinateValidators(
                Sets.newLinkedHashSet(Collections.singletonList(RedeploymentValidator.ALIAS)));
    }
    
}
