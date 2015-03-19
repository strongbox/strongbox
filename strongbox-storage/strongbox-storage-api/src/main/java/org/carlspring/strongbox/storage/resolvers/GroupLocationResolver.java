package org.carlspring.strongbox.storage.resolvers;

import org.carlspring.strongbox.services.BasicRepositoryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRulesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Set;

/**
 * @author mtodorov
 */
@Component
public class GroupLocationResolver
        extends AbstractLocationResolver
{

    private static final Logger logger = LoggerFactory.getLogger(GroupLocationResolver.class);

    private String alias = "group";

    @Autowired
    private RoutingRulesManager routingRulesManager;

    @Autowired
    private BasicRepositoryService basicRepositoryService;


    public GroupLocationResolver()
    {
    }

    @Override
    public InputStream getInputStream(String storageId,
                                      String repositoryId,
                                      String artifactPath)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);

        logger.debug("Checking in " + storage.getId() + ":" + repositoryId + "...");

        Repository groupRepository = storage.getRepository(repositoryId);

        // Check the routing rules first.
        // Check the routing accept rules for the specified repository.
        final InputStream isRepositoryAccept = getInputStreamFromRepositoryAcceptRules(repositoryId,
                                                                                       artifactPath,
                                                                                       storage);
        if (isRepositoryAccept != null)
        {
            return isRepositoryAccept;
        }

        // Check the routing rules for wildcard accept rules
        final InputStream isWildcardRepositoryAccept = getInputStreamFromWildcardRepositoryAcceptRules(artifactPath,
                                                                                                       storage);
        if (isWildcardRepositoryAccept != null)
        {
            return isWildcardRepositoryAccept;
        }

        // Handle:
        // - Repository deny
        // - Repository wildcard repository deny
        final Set<RoutingRule> denyRules = routingRulesManager.getDenyRules(repositoryId);
        final Set<RoutingRule> wildcardDenyRules = getRoutingRulesManager().getWildcardDenyRules();

        // If there are no matches in the routing rules, then loop as usual:
        for (String storageAndRepositoryId : groupRepository.getGroupRepositories())
        {
            String sId = getConfigurationManager().getStorageId(storage, storageAndRepositoryId);
            String rId = getConfigurationManager().getRepositoryId(storageAndRepositoryId);

            Repository r = getConfiguration().getStorage(sId).getRepository(rId);

            if (!repositoryRejects(r.getId(), artifactPath, denyRules) &&
                !repositoryRejects(r.getId(), artifactPath, wildcardDenyRules))
            {
                final InputStream is = getInputStream(artifactPath, r);
                if (is != null)
                {
                    return is;
                }
            }
        }

        return null;
    }

    public boolean repositoryRejects(String repositoryId, String artifactPath, Set<RoutingRule> denyRules)
    {
        if (denyRules != null && !denyRules.isEmpty())
        {
            for (RoutingRule rule : denyRules)
            {
                if (rule.getRepositories().contains(repositoryId) && artifactPath.matches(rule.getPattern()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private InputStream getInputStreamFromWildcardRepositoryAcceptRules(String artifactPath, Storage storage)
            throws IOException
    {
        Set<RoutingRule> globalAcceptRules = routingRulesManager.getWildcardAcceptRules();
        if (globalAcceptRules != null && !globalAcceptRules.isEmpty())
        {
            int hops = 1;
            for (RoutingRule rule : globalAcceptRules)
            {
                if (artifactPath.matches(rule.getPattern()))
                {
                    for (String rId : rule.getRepositories())
                    {
                        String sId = getConfigurationManager().getStorageId(storage, rId);
                        rId = getConfigurationManager().getRepositoryId(rId);

                        Repository repository = getConfiguration().getStorage(sId).getRepository(rId);
                        if (basicRepositoryService.containsPath(repository, artifactPath))
                        {
                            final InputStream is = getInputStream(artifactPath, repository);
                            if (is != null)
                            {
                                logger.debug("Located artifact via wildcard routing rule [" + sId + ":" + rId + "]: [+]: " +
                                             rule.getPattern() + " after " + hops + " hops.");

                                return is;
                            }
                        }

                        hops++;
                    }

                    hops++;
                }
            }
        }

        return null;
    }

    private InputStream getInputStreamFromRepositoryAcceptRules(String repositoryId, String artifactPath, Storage storage)
            throws IOException
    {
        Set<RoutingRule> acceptRules = routingRulesManager.getAcceptRules(repositoryId);
        if (acceptRules != null && !acceptRules.isEmpty())
        {
            int hops = 1;
            for (RoutingRule rule : acceptRules)
            {
                if (artifactPath.matches(rule.getPattern()))
                {
                    for (String rId : rule.getRepositories())
                    {
                        String sId = getConfigurationManager().getStorageId(storage, rId);
                        rId = getConfigurationManager().getRepositoryId(rId);

                        Repository repository = getConfiguration().getStorage(sId).getRepository(rId);
                        if (basicRepositoryService.containsPath(repository, artifactPath))
                        {
                            final InputStream is = getInputStream(artifactPath, repository);
                            if (is != null)
                            {
                                logger.debug("Located artifact via routing rule [" + sId + ":" + rId + "]: [+]: " +
                                             rule.getPattern() + " after " + hops + " hops.");

                                return is;
                            }
                        }

                        hops++;
                    }

                    hops++;
                }
            }
        }

        return null;
    }

    private InputStream getInputStream(String artifactPath, Repository repository)
            throws IOException
    {
        final File repoPath = new File(repository.getBasedir());
        final File artifactFile = new File(repoPath, artifactPath).getCanonicalFile();

        logger.debug(" -> Checking for " + artifactFile.getCanonicalPath() + "...");

        if (artifactFile.exists())
        {
            logger.debug("Resolved " + artifactFile.getCanonicalPath() + "!");

            return new FileInputStream(artifactFile);
        }

        return null;
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

    @Override
    public void delete(String storageId,
                       String repositoryId,
                       String path,
                       boolean force)
            throws IOException
    {
        throw new IOException("Group repositories cannot perform delete operations.");
    }

    @Override
    public void deleteTrash(String storageId, String repositoryId)
            throws IOException
    {
        throw new IOException("Group repositories cannot perform delete operations.");
    }

    @Override
    public void deleteTrash()
            throws IOException
    {
        throw new IOException("Group repositories cannot perform delete operations.");
    }

    @Override
    public void initialize()
            throws IOException
    {
        logger.debug("Initialized GroupLocationResolver.");
    }

    @Override
    public String getAlias()
    {
        return alias;
    }

    @Override
    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    public RoutingRulesManager getRoutingRulesManager()
    {
        return routingRulesManager;
    }

    public void setRoutingRulesManager(RoutingRulesManager routingRulesManager)
    {
        this.routingRulesManager = routingRulesManager;
    }

}
