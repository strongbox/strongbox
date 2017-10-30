package org.carlspring.strongbox.providers.repository;

import static org.carlspring.strongbox.providers.layout.LayoutProviderRegistry.getLayoutProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class GroupRepositoryProvider extends AbstractRepositoryProvider
{

    private static final Logger logger = LoggerFactory.getLogger(GroupRepositoryProvider.class);

    private static final String ALIAS = "group";

    @Inject
    private ArtifactEntryService artifactEntryService;

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
                ArtifactInputStream is;
                try
                {
                    is = resolveArtifact(sId, r.getId(), artifactPath);
                }
                catch (IOException e)
                {
                    continue;
                }
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
        RepositoryProvider provider = getRepositoryProviderRegistry().getProvider(repository.getType());
        ArtifactInputStream is = provider.getInputStream(repository.getStorage().getId(),
                                                         repository.getId(),
                                                         artifactPath);

        return is;
    }

    @Override
    public ArtifactOutputStream getOutputStream(String storageId,
                                                String repositoryId,
                                                String artifactPath)
            throws IOException
    {
        // It should not be possible to write artifacts to a group repository.
        // A group repository should only serve artifacts that already exist
        // in the repositories within the group.

        throw new UnsupportedOperationException();
    }

    public RoutingRules getRoutingRules()
    {
        return getConfiguration().getRoutingRules();
    }

    @Override
    public List<Path> search(RepositorySearchRequest searchRequest,
                             RepositoryPageRequest pageRequest)
    {
        Map<ArtifactCoordinates, Path> resultMap = new LinkedHashMap<>();

        String storageId = searchRequest.getStorageId();
        Storage storage = getConfiguration().getStorage(storageId);

        String repositoryId = searchRequest.getRepositoryId();
        logger.debug("Search in " + storage.getId() + ":" + repositoryId + "...");

        Repository groupRepository = storage.getRepository(repositoryId);

        Set<Repository> groupRepositorySet = collectGroupRepositorySet(storage, groupRepository);
        
        if (groupRepositorySet.isEmpty())
        {
            return new LinkedList<>();
        }
        
        int skip = pageRequest.getSkip();
        int limit = pageRequest.getLimit();

        int groupSize = groupRepositorySet.size();
        int groupSkip = (skip / (limit * groupSize)) * limit;
        int groupLimit = limit;

        skip = skip - groupSkip;
        
        outer: do
        {
            RepositoryPageRequest pageRequestLocal = new RepositoryPageRequest();
            pageRequestLocal.setLimit(groupLimit);
            pageRequestLocal.setOrderBy(pageRequest.getOrderBy());
            pageRequestLocal.setSkip(groupSkip);

            groupLimit = 0;

            for (Iterator<Repository> i = groupRepositorySet.iterator(); i.hasNext();)
            {
                Repository r = i.next();
                searchRequest.setStorageId(r.getStorage().getId());
                searchRequest.setRepositoryId(r.getId());

                RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(r.getType());

                List<Path> repositoryResult = repositoryProvider.search(searchRequest, pageRequestLocal);
                if (repositoryResult.isEmpty())
                {
                    i.remove();
                    continue;
                }

                // count coordinates intersection
                groupLimit += repositoryResult.stream()
                                              .map((p) -> resultMap.put(getArtifactCoordinates(p),
                                                                        p))
                                              .filter(p ->  p != null)
                                              .collect(Collectors.toList())
                                              .size();

                //Break search iterations if we have reached enough list size.
                if (resultMap.size() >= limit + skip)
                {
                    break outer;
                }
            }
            groupSkip += limit;

            // Will iterate until there is no more coordinates intersection and
            // there is more search results within group repositories
        } while (groupLimit > 0 && !groupRepositorySet.isEmpty());

        LinkedList<Path> resultList = new LinkedList<>();
        if (skip >= resultMap.size())
        {
            return resultList;
        }
        resultList.addAll(resultMap.values());

        int toIndex = resultList.size() - skip > limit ? limit + skip : resultList.size();
        return resultList.subList(skip, toIndex);
    }

    private Set<Repository> collectGroupRepositorySet(Storage storage, Repository repository)
    {
        return collectGroupRepositorySet(storage, repository, false);
    }

    private Set<Repository> collectGroupRepositorySet(Storage storage,
                                                      Repository groupRepository,
                                                      boolean traverse)
    {
        Set<Repository> result = groupRepository.getGroupRepositories()
                                                            .stream()
                                                            .map(groupRepoId -> {
                                                                return getRepository(storage, groupRepoId);
                                                            })
                                                            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (!traverse)
        {
            return result;
        }

        Set<Repository> traverseResult = new LinkedHashSet<>();
        for (Iterator<Repository> i = result.iterator(); i.hasNext();)
        {
            Repository r = i.next();
            if (r.getGroupRepositories().isEmpty()) {
                traverseResult.add(r);
                continue;
            }
            
            i.remove();
            traverseResult.addAll(collectGroupRepositorySet(storage, r, true));
        }
        
        return result;
    }

    private Repository getRepository(Storage storage, String id)
    {
        String sId = getConfigurationManager().getStorageId(storage, id);
        String rId = getConfigurationManager().getRepositoryId(id);

        return getConfiguration().getStorage(sId).getRepository(rId);
    }

    private ArtifactCoordinates getArtifactCoordinates(Path p)
    {
        try
        {
            return (ArtifactCoordinates) Files.getAttribute(p, RepositoryFileAttributes.COORDINATES);
        }
        catch (IOException e)
        {
            throw new RuntimeException(String.format("Failed to resolve ArtifactCoordinates for [%s]", p), e);
        }
    }
    
    @Override
    public Long count(RepositorySearchRequest searchRequest)
    {
        String storageId = searchRequest.getStorageId();
        Storage storage = getConfiguration().getStorage(storageId);

        String repositoryId = searchRequest.getRepositoryId();
        logger.debug("Count in " + storage.getId() + ":" + repositoryId + "...");

        Repository groupRepository = storage.getRepository(repositoryId);

        Set<Pair<String, String>> storageRepositoryPairSet = collectGroupRepositorySet(storage, groupRepository,
                                                                                   true).stream()
                                                                                        .map(r -> Pair.with(r.getStorage()
                                                                                                             .getId(),
                                                                                                            r.getId()))
                                                                                        .collect(Collectors.toCollection(LinkedHashSet::new));

        return artifactEntryService.countByCoordinates(storageRepositoryPairSet, searchRequest.getCoordinates(),
                                                       searchRequest.isStrict());
    }
}
