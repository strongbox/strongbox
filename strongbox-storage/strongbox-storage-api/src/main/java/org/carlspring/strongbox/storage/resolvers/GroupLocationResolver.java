package org.carlspring.strongbox.storage.resolvers;

import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.services.BasicRepositoryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("groupLocationResolver")
public class GroupLocationResolver
        extends AbstractLocationResolver
{

    private static final Logger logger = LoggerFactory.getLogger(GroupLocationResolver.class);

    private String alias = "group";

    @Autowired
    private BasicRepositoryService basicRepositoryService;

    public GroupLocationResolver()
    {
    }

    @Override
    public ArtifactInputStream getInputStream(String storageId,
                                              String repositoryId,
                                              String artifactPath)
            throws IOException, NoSuchAlgorithmException
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
            throws IOException, NoSuchAlgorithmException
    {
        RuleSet globalAcceptRules = getRoutingRules().getWildcardAcceptedRules();
        if (globalAcceptRules != null && globalAcceptRules.getRoutingRules() != null &&
            !globalAcceptRules.getRoutingRules().isEmpty())
        {
            final List<RoutingRule> routingRules = globalAcceptRules.getRoutingRules();
            for (RoutingRule rule : routingRules)
            {
                if (artifactPath.matches(rule.getPattern()))
                {
                    for (String rId : rule.getRepositories())
                    {
                        String sId = getConfigurationManager().getStorageId(storage, rId);
                        rId = getConfigurationManager().getRepositoryId(rId);

                        Repository repository = getConfiguration().getStorage(sId).getRepository(rId);
                        if (repository.isInService() && basicRepositoryService.containsPath(repository, artifactPath))
                        {
                            return resolveArtifact(sId, repository.getId(), artifactPath);
                        }
                    }
                }
            }
        }

        return null;
    }

    private ArtifactInputStream getInputStreamFromRepositoryAcceptRules(String repositoryId,
                                                                        String artifactPath,
                                                                        Storage storage)
            throws IOException, NoSuchAlgorithmException
    {
        RuleSet acceptRules = getRoutingRules().getAcceptRules(repositoryId);
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
                        if (repository.isInService() && basicRepositoryService.containsPath(repository, artifactPath))
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
     * returns the product of calling GroupLocationResolver.getInputStream recursively otherwise
     * 
     * @param storageId    The storage id
     * @param repositoryId The repository
     * @param artifactPath The path to the artifact
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private ArtifactInputStream resolveArtifact(String storageId,
                                                String repositoryId,
                                                String artifactPath)
            throws NoSuchAlgorithmException,
                   IOException
    {
        ArtifactInputStream is;
        Repository repository = getStorage(storageId).getRepository(repositoryId);

        if (!RepositoryTypeEnum.GROUP.getType().equals(repository.getType()))
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
            is = this.getInputStream(storageId, repository.getId(), artifactPath);
            if (is != null)
            {
                logger.debug("Located artifact: [" + storageId + ":" + repository.getId() + "]");
                return is;
            }
        }

        return null;
    }

    private ArtifactInputStream getInputStream(Repository repository, String artifactPath)
            throws IOException, NoSuchAlgorithmException
    {
        final File repoPath = new File(repository.getBasedir());
        final File artifactFile = new File(repoPath, artifactPath).getCanonicalFile();

        logger.debug(" -> Checking for " + artifactFile.getCanonicalPath() + "...");

        if (artifactFile.exists())
        {
            logger.debug("Resolved " + artifactFile.getCanonicalPath() + "!");

            ArtifactInputStream ais = new ArtifactInputStream(new FileInputStream(artifactFile));
            ais.setLength(artifactFile.length());

            return ais;
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
    public boolean contains(String storageId, String repositoryId, String path)
            throws IOException
    {
        return false;
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
    public void undelete(String storageId, String repositoryId, String path)
            throws IOException
    {
        logger.debug("Failed to undelete '" + storageId + ":" + repositoryId + "/" + path + "'," +
                     " as group repositories cannot perform undelete operations.");
    }

    @Override
    public void undeleteTrash(String storageId, String repositoryId)
            throws IOException
    {
        logger.debug("Failed to undelete trash for " + storageId + ":" + repositoryId + "," +
                     " as group repositories cannot perform undelete operations.");
    }

    @Override
    public void undeleteTrash()
            throws IOException
    {
        logger.debug("Failed to undelete trash, as group repositories cannot perform undelete operations.");
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

    public RoutingRules getRoutingRules()
    {
        return getConfiguration().getRoutingRules();
    }

}
