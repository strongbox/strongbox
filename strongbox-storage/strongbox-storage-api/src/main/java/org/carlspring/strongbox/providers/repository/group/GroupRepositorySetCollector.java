package org.carlspring.strongbox.providers.repository.group;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.configuration.ConfigurationUtils;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class GroupRepositorySetCollector
{

    private static final Logger logger = LoggerFactory.getLogger(GroupRepositorySetCollector.class);

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    private ConfigurationManager configurationManager;

    public Set<Repository> collect(Repository groupRepository)
    {
        return collect(groupRepository, false);
    }

    public Set<Repository> collect(Repository groupRepository,
                                   boolean traverse)
    {
        Set<Repository> result = groupRepository.getGroupRepositories()
                                                .stream()
                                                .map(groupRepoId -> getRepository(groupRepository,
                                                                                  groupRepoId))
                                                .filter(repository -> repository != null)
                                                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (!traverse)
        {
            return result;
        }

        Set<Repository> traverseResult = new LinkedHashSet<>();
        for (Iterator<Repository> i = result.iterator(); i.hasNext();)
        {
            Repository r = i.next();
            if (CollectionUtils.isEmpty(r.getGroupRepositories()))
            {
                traverseResult.add(r);
                continue;
            }

            i.remove();
            traverseResult.addAll(collect(r, true));
        }

        return traverseResult;
    }

    private Repository getRepository(Repository groupRepository,
                                     String id)
    {
        String sId = ConfigurationUtils.getStorageId(groupRepository.getStorage().getId(), id);
        String rId = ConfigurationUtils.getRepositoryId(id);

        try
        {
            Storage groupRepositoryStorage = configurationManager.getConfiguration().getStorage(sId);
            if (groupRepositoryStorage == null)
            {
                logger.warn("Storage [{}] not found for groupRepositoryId [{}]", sId, id);
                markRepositoryOutOfService(groupRepository);
                return null;
            }

            Repository repository = groupRepositoryStorage.getRepository(rId);
            if (repository == null)
            {
                logger.warn("Repository [{}] not found for groupRepositoryId [{}]", rId, id);
                markRepositoryOutOfService(groupRepository);
                return null;
            }

            return repository;
        }
        catch (IOException e)
        {
            logger.error("Something went wrong while marking Repository Out of Service..");
        }

        return null;
    }
    
    public void markRepositoryOutOfService(Repository groupRepository)
        throws IOException

    {
        logger.debug("Going to mark repository [{}] as Out of Service as it contains reference to non-existent Repository/storage.",
                     groupRepository.getId());
        repositoryManagementService.putOutOfService(groupRepository.getStorage().getId(), groupRepository.getId());
    }

}
