package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.providers.layout.LayoutProviderRegistry.getLayoutProvider;

/**
 * @author carlspring
 */
@Component
public class GroupRepositoryProvider extends AbstractRepositoryProvider
{

    private static final Logger logger = LoggerFactory.getLogger(GroupRepositoryProvider.class);

    private static final String ALIAS = "group";


    @PostConstruct
    @Override
    public void register()
    {
        getRepositoryProviderRegistry().addProvider(ALIAS, this);

        logger.info("Registered repository provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public ArtifactInputStream getInputStream(String storageId,
                                              String repositoryId,
                                              String artifactPath)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        Storage storage = getConfiguration().getStorage(storageId);

        logger.debug("Checking in " + storage.getId() + ":" + repositoryId + "...");

        Repository groupRepository = storage.getRepository(repositoryId);

        // Check the routing rules first.
        // Check the routing accept rules for the specified repository.
        final ArtifactInputStream isRepositoryAccept = getInputStreamFromRepositoryAcceptRules(repositoryId,
                                                                                               artifactPath,
                                                                                               storage);

        if (isRepositoryAccept != null)
        {
            return isRepositoryAccept;
        }

        // Check the routing rules for wildcard accept rules
        final ArtifactInputStream isWildcardRepositoryAccept = getInputStreamFromWildcardRepositoryAcceptRules(artifactPath, storage);
        if (isWildcardRepositoryAccept != null)
        {
            return isWildcardRepositoryAccept;
        }

        // Handle:
        // - Repository deny
        // - Repository wildcard repository deny
        final RuleSet denyRules = getRoutingRules().getDenyRules(repositoryId);
        final RuleSet wildcardDenyRules = getRoutingRules().getWildcardDeniedRules();

        // If there are no matches in the routing rules, then loop as usual:
        for (String storageAndRepositoryId : groupRepository.getGroupRepositories())
        {
            String sId = getConfigurationManager().getStorageId(storage, storageAndRepositoryId);
            String rId = getConfigurationManager().getRepositoryId(storageAndRepositoryId);

            Repository r = getConfiguration().getStorage(sId).getRepository(rId);

            if (r.isInService() &&
                !repositoryRejects(r.getId(), artifactPath, denyRules) &&
                !repositoryRejects(r.getId(), artifactPath, wildcardDenyRules))
            {
                final ArtifactInputStream is = resolveArtifact(sId, r.getId(), artifactPath);
                if (is != null)
                {
                    return is;
                }
            }
        }

        return null;
    }

    public boolean repositoryRejects(String repositoryId, String artifactPath, RuleSet denyRules)
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

    private ArtifactInputStream getInputStreamFromWildcardRepositoryAcceptRules(String artifactPath, Storage storage)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        RuleSet globalAcceptRules = getRoutingRules().getWildcardAcceptedRules();

        return getArtifactInputStreamViaAcceptedRules(artifactPath, storage, globalAcceptRules);
    }

    private ArtifactInputStream getInputStreamFromRepositoryAcceptRules(String repositoryId,
                                                                        String artifactPath,
                                                                        Storage storage)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        RuleSet acceptRules = getRoutingRules().getAcceptRules(repositoryId);

        return getArtifactInputStreamViaAcceptedRules(artifactPath, storage, acceptRules);
    }

    private ArtifactInputStream getArtifactInputStreamViaAcceptedRules(String artifactPath,
                                                                       Storage storage,
                                                                       RuleSet acceptRules)
            throws ProviderImplementationException,
                   NoSuchAlgorithmException,
                   IOException,
                   ArtifactTransportException
    {
        if (acceptRules != null && acceptRules.getRoutingRules() != null &&
            !acceptRules.getRoutingRules().isEmpty())
        {
            final List<RoutingRule> routingRules = acceptRules.getRoutingRules();
            for (RoutingRule rule : routingRules)
            {
                if (artifactPath.matches(rule.getPattern()))
                {
                    for (String rId : rule.getRepositories())
                    {
                        String sId = getConfigurationManager().getStorageId(storage, rId);
                        rId = getConfigurationManager().getRepositoryId(rId);

                        Repository repository = getConfiguration().getStorage(sId).getRepository(rId);
                        LayoutProvider layoutProvider = getLayoutProvider(repository, getLayoutProviderRegistry());

                        if (repository.isInService() && layoutProvider.containsPath(repository, artifactPath))
                        {
                            return resolveArtifact(sId, repository.getId(), artifactPath);
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns the artifact associated to artifactPath if repository type isn't GROUP or
     * returns the product of calling getInputStream recursively otherwise.
     *
     * @param storageId    The storage id
     * @param repositoryId The repository
     * @param artifactPath The path to the artifact
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws ArtifactTransportException
     */
    private ArtifactInputStream resolveArtifact(String storageId,
                                                String repositoryId,
                                                String artifactPath)
            throws NoSuchAlgorithmException,
                   IOException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        ArtifactInputStream is;
        Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);

        if (!getAlias().equals(repository.getType()))
        {
            is = getInputStream(repository, artifactPath);
            if (is != null)
            {
                logger.debug("Located artifact: [" + storageId + ":" + repository.getId() + "]");
                return is;
            }
        }
        else
        {
            is = getInputStream(storageId, repository.getId(), artifactPath);
            if (is != null)
            {
                logger.debug("Located artifact: [" + storageId + ":" + repository.getId() + "]");
                return is;
            }
        }

        return null;
    }

    private ArtifactInputStream getInputStream(Repository repository, String artifactPath)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        return getLayoutProviderRegistry().getProvider(repository.getLayout())
                                          .getInputStream(repository.getStorage().getId(),
                                                          repository.getId(),
                                                          artifactPath);
    }

    @Override
    public OutputStream getOutputStream(String storageId,
                                        String repositoryId,
                                        String artifactPath)
            throws IOException
    {
        // It should not be possible to write artifacts to a group repository.
        // A group repository should only serve artifacts that already exist
        // in the repositories within the group.

        return null;
    }

    public RoutingRules getRoutingRules()
    {
        return getConfiguration().getRoutingRules();
    }

}
