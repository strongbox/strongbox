package org.carlspring.strongbox.providers.repository;


import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.RepositoryInputStream;
import org.carlspring.strongbox.io.RepositoryOutputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.repository.group.GroupRepositorySetCollector;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.support.ArtifactRoutingRulesChecker;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import java.nio.file.Files;

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

    @Inject
    private ArtifactRoutingRulesChecker artifactRoutingRulesChecker;

    @Inject
    private HostedRepositoryProvider hostedRepositoryProvider;

    @Inject
    private GroupRepositorySetCollector groupRepositorySetCollector;

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
    public RepositoryInputStream getInputStream(String storageId,
                                                String repositoryId,
                                                String artifactPath)
        throws IOException,
        NoSuchAlgorithmException,
        ArtifactTransportException,
        ProviderImplementationException
    {
        return Optional.ofNullable(resolvePathDirectlyFromGroupPathIfPossible(storageId, repositoryId,
                                                                              artifactPath))
                       .map(p -> hostedRepositoryProvider.newInputStream(p))
                       .orElse(hostedRepositoryProvider.newInputStream(resolvePathTraversal(storageId, repositoryId,
                                                                                            artifactPath)));
    }

    protected RepositoryPath resolvePathTraversal(String storageId,
                                                  String repositoryId,
                                                  String artifactPath)
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository groupRepository = storage.getRepository(repositoryId);
        
        for (String storageAndRepositoryId : groupRepository.getGroupRepositories())
        {
            String sId = getConfigurationManager().getStorageId(storage, storageAndRepositoryId);
            String rId = getConfigurationManager().getRepositoryId(storageAndRepositoryId);

            Repository r = getConfiguration().getStorage(sId).getRepository(rId);

            if (!r.isInService())
            {
                continue;
            }
            if (artifactRoutingRulesChecker.isDenied(repositoryId, rId, artifactPath))
            {
                continue;
            }
            
            RepositoryPath result = resolvePathFromGroupMemberOrTraverse(sId, r.getId(), artifactPath);
            if (result == null)
            {
                continue;
            }
            
            logger.debug(String.format("Located artifact: [%s]", result));
            
            return result;
        }
        return null;
    }

    protected RepositoryPath resolvePathDirectlyFromGroupPathIfPossible(final String storageId,
                                                                        final String repositoryId,
                                                                        final String path)
        throws IOException
    {
        final Storage storage = getConfiguration().getStorage(storageId);
        final Repository repository = storage.getRepository(repositoryId);
        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final RepositoryPath artifactPath = layoutProvider.resolve(repository).resolve(path);
        if (Files.exists(artifactPath) && RepositoryFiles.isMetadata(artifactPath))
        {
            return artifactPath;
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
    protected RepositoryPath resolvePathFromGroupMemberOrTraverse(String storageId,
                                                                  String repositoryId,
                                                                  String artifactPath)
    {
        Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);
        if (getAlias().equals(repository.getType()))
        {
            return resolvePathTraversal(storageId, repository.getId(), artifactPath);
        }
        
        RepositoryProvider provider = getRepositoryProviderRegistry().getProvider(repository.getType());
        try
        {
            return (RepositoryPath) provider.resolvePath(storageId, repositoryId, artifactPath);
        }
        catch (IOException e)
        {
            logger.error(String.format("Failed to resolve path [%s]/[%s]/[%s]", storageId, repositoryId,
                                       artifactPath));
            return null;
        }
    }

    @Override
    public RepositoryOutputStream getOutputStream(String storageId,
                                                  String repositoryId,
                                                  String artifactPath)
            throws IOException
    {
        // It should not be possible to write artifacts to a group repository.
        // A group repository should only serve artifacts that already exist
        // in the repositories within the group.

        throw new UnsupportedOperationException();
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

        Set<Repository> groupRepositorySet = groupRepositorySetCollector.collect(groupRepository);

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
            RepositorySearchRequest searchRequestLocal = new RepositorySearchRequest(null, null);
            searchRequestLocal.setCoordinates(searchRequest.getCoordinates());
            searchRequestLocal.setStrict(searchRequest.isStrict());
            searchRequestLocal.setTagSet(searchRequest.getTagSet());

            RepositoryPageRequest pageRequestLocal = new RepositoryPageRequest();
            pageRequestLocal.setLimit(groupLimit);
            pageRequestLocal.setOrderBy(pageRequest.getOrderBy());
            pageRequestLocal.setSkip(groupSkip);

            groupLimit = 0;

            for (Iterator<Repository> i = groupRepositorySet.iterator(); i.hasNext();)
            {
                Repository r = i.next();
                searchRequestLocal.setStorageId(r.getStorage().getId());
                searchRequestLocal.setRepositoryId(r.getId());


                RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(r.getType());

                List<Path> repositoryResult = repositoryProvider.search(searchRequestLocal, pageRequestLocal);
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

    private ArtifactCoordinates getArtifactCoordinates(Path p)
    {
        try
        {
            return RepositoryFiles.readCoordinates((RepositoryPath) p);
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

        Set<Pair<String, String>> storageRepositoryPairSet = groupRepositorySetCollector.collect(groupRepository,
                                                                                                 true).stream()
                                                                                            .map(r -> Pair.with(r.getStorage()
                                                                                                                 .getId(),
                                                                                                                r.getId()))
                                                                                            .collect(Collectors.toCollection(LinkedHashSet::new));

        return artifactEntryService.countCoordinates(storageRepositoryPairSet, searchRequest.getCoordinates(),
                                                     searchRequest.isStrict());
    }
    
    @Override
    public RepositoryPath resolvePath(String storageId,
                                      String repositoryId,
                                      String path)
        throws IOException
    {
        return Optional.ofNullable(resolvePathDirectlyFromGroupPathIfPossible(storageId, repositoryId, path))
                       .orElse(resolvePathTraversal(storageId, repositoryId, path));
    }
    
}
