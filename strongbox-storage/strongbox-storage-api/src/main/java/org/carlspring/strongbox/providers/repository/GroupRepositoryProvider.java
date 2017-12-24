
package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.support.ArtifactRoutingRulesChecker;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

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
        Repository groupRepository = storage.getRepository(repositoryId);

        ArtifactInputStream is = resolveDirectlyFromGroupPathIfPossible(storageId, repositoryId, artifactPath);
        if (is != null)
        {
            return is;
        }

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

        return null;
    }

    private ArtifactInputStream resolveDirectlyFromGroupPathIfPossible(final String storageId,
                                                                       final String repositoryId,
                                                                       final String path)
            throws IOException
    {

        final Storage storage = getConfiguration().getStorage(storageId);
        final Repository repository = storage.getRepository(repositoryId);
        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final RepositoryPath artifactPath = layoutProvider.resolve(repository).resolve(path);
        final RepositoryFileAttributes artifactFileAttributes;
        try
        {
            artifactFileAttributes = Files.readAttributes(artifactPath, RepositoryFileAttributes.class);
        }
        catch (NoSuchFileException ex)
        {
            return null;
        }
        return artifactFileAttributes.isMetadata() ?
               hostedRepositoryProvider.getInputStream(storageId, repositoryId, path) : null;
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

    public RepositoryPath getPath(String storageId,
                                       String repositoryId,
                                       String path)
           throws IOException,
                  NoSuchAlgorithmException,
                  ArtifactTransportException,
                  ProviderImplementationException
   {
        final Storage storage = getConfiguration().getStorage(storageId);
        final Repository groupRepository = storage.getRepository(repositoryId);
        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(groupRepository.getLayout());
        RepositoryPath artifactPath = null;
        
        artifactPath = layoutProvider.resolve(groupRepository).resolve(path);
        
        if(Files.exists(artifactPath))
                return artifactPath;
        
        for (String storageAndRepositoryId : groupRepository.getGroupRepositories())
        {
            String sId = getConfigurationManager().getStorageId(storage, storageAndRepositoryId);
            String rId = getConfigurationManager().getRepositoryId(storageAndRepositoryId);

            Repository r = getConfiguration().getStorage(sId).getRepository(rId);

            if (!r.isInService())
            {
                continue;
            }
            if (artifactRoutingRulesChecker.isDenied(repositoryId, rId, path))
            {
                continue;
            }
            try
            {
                artifactPath = resolvePath(sId, r.getId(), path);
            }
            catch (IOException e)
            {
                continue;
            }
            if (artifactPath != null)
            {
                return artifactPath;
            }
        }
        
        
        return null;
   }
    
    public RepositoryPath resolvePath(String storageId,
                                      String repositoryId,
                                      String path)
           throws NoSuchAlgorithmException,
                  IOException,
                  ArtifactTransportException,
                  ProviderImplementationException
    {
        RepositoryPath artifactPath;

        Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);

        if (!getAlias().equals(repository.getType()))
        {
            artifactPath = getPath(repository, path);
            if (artifactPath != null)
            {
                logger.debug("Located artifact: [" + storageId + ":" + repository.getId() + "]");
                return artifactPath;
            }
        }
        else
        {
            artifactPath = getPath(storageId, repository.getId(), path);
            if (artifactPath != null)
            {
                logger.debug("Located artifact: [" + storageId + ":" + repository.getId() + "]");
                return artifactPath;
            }
        }

        return null;
    }
    
    private RepositoryPath getPath(Repository repository, String path)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        RepositoryProvider provider = getRepositoryProviderRegistry().getProvider(repository.getType());
       // RepositoryPath artifactPath = provider.getPath(repository.getStorage().getId(),
        //                                                 repository.getId(),
        //                                                 path);

        LayoutProvider<?> layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        
        RepositoryPath artifactPath = layoutProvider.resolve(repository).resolve(path);
        
        return artifactPath;
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

        Set<Repository> groupRepositorySet = collectGroupRepositorySet(groupRepository);

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

    private Set<Repository> collectGroupRepositorySet(Repository repository)
    {
        return collectGroupRepositorySet(repository, false);
    }

    private Set<Repository> collectGroupRepositorySet(Repository groupRepository,
                                                      boolean traverse)
    {
        Set<Repository> result = groupRepository.getGroupRepositories()
                                                .stream()
                                                .map(groupRepoId -> {
                                                    return getRepository(groupRepository.getStorage(), groupRepoId);
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
            traverseResult.addAll(collectGroupRepositorySet(r, true));
        }

        return traverseResult;
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

        Set<Pair<String, String>> storageRepositoryPairSet = collectGroupRepositorySet(groupRepository,
                                                                                       true).stream()
                                                                                            .map(r -> Pair.with(r.getStorage()
                                                                                                                 .getId(),
                                                                                                                r.getId()))
                                                                                            .collect(Collectors.toCollection(LinkedHashSet::new));

        return artifactEntryService.countCoordinates(storageRepositoryPairSet, searchRequest.getCoordinates(),
                                                     searchRequest.isStrict());
    }


}
