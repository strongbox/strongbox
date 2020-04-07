package org.carlspring.strongbox.providers.repository.proxied;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.repositories.ArtifactRepository;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class LocalStorageProxyRepositoryExpiredArtifactsCleaner
{

    private final Logger logger = LoggerFactory.getLogger(LocalStorageProxyRepositoryExpiredArtifactsCleaner.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Inject
    private ArtifactRepository artifactEntityRepository;

    @Inject
    private RemoteRepositoryAlivenessService remoteRepositoryAlivenessCacheManager;

    @Inject
    private ArtifactManagementService artifactManagementService;

    @Transactional
    public void cleanup(final Integer lastAccessedTimeInDays,
                        final Long minSizeInBytes)
            throws IOException
    {
        final Page<Artifact> artifactEntries = artifactEntityRepository.findMatching(lastAccessedTimeInDays, minSizeInBytes,
                                                                                     PageRequest.of(0, Integer.MAX_VALUE));
        List<Artifact> artifactsToDelete = filterAccessibleProxiedArtifacts(artifactEntries.toList());
        if (artifactsToDelete.isEmpty())
        {
            return;
        }

        logger.debug("Cleaning artifacts {}", artifactsToDelete);
        deleteFromStorage(artifactsToDelete);
    }

    private List<Artifact> filterAccessibleProxiedArtifacts(final List<Artifact> artifactEntries)
    {
        if (CollectionUtils.isEmpty(artifactEntries))
        {
            return Collections.emptyList();
        }
        
        List<Artifact> result = new ArrayList<>();
        for (final Iterator<Artifact> it = artifactEntries.iterator(); it.hasNext(); )
        {
            final Artifact artifactEntry = it.next();
            final Storage storage = configurationManager.getConfiguration().getStorage(artifactEntry.getStorageId());
            final Repository repository = storage.getRepository(artifactEntry.getRepositoryId());
            if (!repository.isProxyRepository())
            {
                continue;
            }
            final RemoteRepository remoteRepository = repository.getRemoteRepository();
            if (remoteRepository == null)
            {
                logger.warn("Repository {} is not associated with remote repository", repository.getId());
                continue;
            }
            if (!remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository))
            {
                logger.warn("Remote repository {} is down. Artifacts won't be cleaned up.", remoteRepository.getUrl());
                continue;
            }
            
            result.add(artifactEntry);
        }
        
        return result;
    }

    private void deleteFromStorage(final List<Artifact> artifactEntries)
            throws IOException
    {
        for (final Artifact artifactEntry : artifactEntries)
        {
            final Storage storage = configurationManager.getConfiguration().getStorage(artifactEntry.getStorageId());
            final Repository repository = storage.getRepository(artifactEntry.getRepositoryId());
            
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository).resolve(artifactEntry);

            artifactManagementService.delete(repositoryPath, true);
        }
    }

}
