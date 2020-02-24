package org.carlspring.strongbox.providers.repository.proxied;

import static org.carlspring.strongbox.services.support.ArtifactEntrySearchCriteria.Builder.anArtifactEntrySearchCriteria;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.data.service.support.search.PagingCriteria;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.repositories.ArtifactEntityRepository;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.support.ArtifactEntrySearchCriteria;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private ArtifactEntityRepository artifactEntityRepository;

    @Inject
    private RemoteRepositoryAlivenessService remoteRepositoryAlivenessCacheManager;

    @Inject
    private ArtifactManagementService artifactManagementService;

    @Transactional(rollbackFor = Exception.class)
    public void cleanup(final Integer lastAccessedTimeInDays,
                        final Long minSizeInBytes)
            throws IOException
    {
        final ArtifactEntrySearchCriteria searchCriteria = anArtifactEntrySearchCriteria()
                                                                   .withLastAccessedTimeInDays(lastAccessedTimeInDays)
                                                                   .withMinSizeInBytes(minSizeInBytes)
                                                                   .build();

        final List<Artifact> artifactEntries = artifactEntityRepository.findMatching(searchCriteria,
                                                                                           PagingCriteria.ALL);
        filterAccessibleProxiedArtifacts(artifactEntries);

        if (CollectionUtils.isEmpty(artifactEntries))
        {
            return;
        }

        logger.debug("Cleaning artifacts {}", artifactEntries);
        deleteFromStorage(artifactEntries);
    }

    private void filterAccessibleProxiedArtifacts(final List<Artifact> artifactEntries)
    {
        if (CollectionUtils.isEmpty(artifactEntries))
        {
            return;
        }
        for (final Iterator<Artifact> it = artifactEntries.iterator(); it.hasNext(); )
        {
            final Artifact artifactEntry = it.next();
            final Storage storage = configurationManager.getConfiguration().getStorage(artifactEntry.getStorageId());
            final Repository repository = storage.getRepository(artifactEntry.getRepositoryId());
            if (!repository.isProxyRepository())
            {
                it.remove();
                continue;
            }
            final RemoteRepository remoteRepository = repository.getRemoteRepository();
            if (remoteRepository == null)
            {
                logger.warn("Repository {} is not associated with remote repository", repository.getId());
                it.remove();
                continue;
            }
            if (!remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository))
            {
                logger.warn("Remote repository {} is down. Artifacts won't be cleaned up.", remoteRepository.getUrl());
                it.remove();
                continue;
            }
        }

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
