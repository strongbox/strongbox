package org.carlspring.strongbox.services.support;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;

import javax.inject.Inject;
import java.io.IOException;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 * @see <a href="https://github.com/strongbox/strongbox/wiki/Artifact-Routing-Rules">Artifact Routing Rules</a>
 */
@Component
public class ArtifactRoutingRulesChecker
{

    @Inject
    private ConfigurationManager configurationManager;

    public boolean isDenied(String groupRepositoryId,
                            RepositoryPath repositoryPath)
            throws IOException
    {
        final RoutingRules routingRules = configurationManager.getConfiguration().getRoutingRules();
        final RuleSet denyRules = routingRules.getDenyRules(groupRepositoryId);
        final RuleSet wildcardDenyRules = routingRules.getWildcardDeniedRules();
        final RuleSet acceptRules = routingRules.getAcceptRules(groupRepositoryId);
        final RuleSet wildcardAcceptRules = routingRules.getWildcardAcceptedRules();

        if (fitsRoutingRules(repositoryPath, denyRules) ||
            fitsRoutingRules(repositoryPath, wildcardDenyRules))
        {
            if (!(fitsRoutingRules(repositoryPath, acceptRules) ||
                  fitsRoutingRules(repositoryPath, wildcardAcceptRules)))
            {
                return true;
            }

        }

        return false;
    }

    public boolean isAccepted(String groupRepositoryId,
                              RepositoryPath repositoryPath)
            throws IOException
    {
        return !isDenied(groupRepositoryId, repositoryPath);
    }

    private boolean fitsRoutingRules(RepositoryPath repositoryPath,
                                     RuleSet denyRules)
            throws IOException
    {
        Repository repository = repositoryPath.getRepository();
        if (denyRules != null && !denyRules.getRoutingRules().isEmpty())
        {
            String artifactPath = RepositoryFiles.relativizePath(repositoryPath);
            for (RoutingRule rule : denyRules.getRoutingRules())
            {
                if (rule.getRepositories().contains(repository.getId())
                    && rule.getRegex().matcher(artifactPath).matches())
                {
                    return true;
                }
            }
        }

        return false;
    }

}
