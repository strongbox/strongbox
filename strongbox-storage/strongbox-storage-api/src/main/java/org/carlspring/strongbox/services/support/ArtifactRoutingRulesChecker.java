package org.carlspring.strongbox.services.support;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 *
 * @see <a href="https://github.com/strongbox/strongbox/wiki/Artifact-Routing-Rules">Artifact Routing Rules</a>
 */
@Component
public class ArtifactRoutingRulesChecker
{

    @Inject
    private ConfigurationManager configurationManager;

    public boolean isDenied(String groupRepositoryId,
                            String childRepositoryId,
                            String artifactPath)
    {
        final RuleSet denyRules = getRoutingRules().getDenyRules(groupRepositoryId);
        final RuleSet wildcardDenyRules = getRoutingRules().getWildcardDeniedRules();
        final RuleSet acceptRules = getRoutingRules().getAcceptRules(groupRepositoryId);
        final RuleSet wildcardAcceptRules = getRoutingRules().getWildcardAcceptedRules();

        if (fitsRoutingRules(childRepositoryId, artifactPath, denyRules) ||
            fitsRoutingRules(childRepositoryId, artifactPath, wildcardDenyRules))
        {
            if (!(fitsRoutingRules(childRepositoryId, artifactPath, acceptRules) ||
                  fitsRoutingRules(childRepositoryId, artifactPath, wildcardAcceptRules)))
            {
                return true;
            }

        }

        return false;
    }

    public boolean isAccepted(String groupRepositoryId,
                              String childRepositoryId,
                              String artifactPath)
    {
        return !isDenied(groupRepositoryId, childRepositoryId, artifactPath);
    }

    private RoutingRules getRoutingRules()
    {
        return getConfiguration().getRoutingRules();
    }

    private Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    private boolean fitsRoutingRules(String repositoryId,
                                     String artifactPath,
                                     RuleSet denyRules)
    {
        if (denyRules != null && !denyRules.getRoutingRules().isEmpty())
        {
            for (RoutingRule rule : denyRules.getRoutingRules())
            {
                if (rule.getRepositories().contains(repositoryId) && artifactPath.matches(rule.getPattern()))
                {
                    return true;
                }
            }
        }

        return false;
    }

}
