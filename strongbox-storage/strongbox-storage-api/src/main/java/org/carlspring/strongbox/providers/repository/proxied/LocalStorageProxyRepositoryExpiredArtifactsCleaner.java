package org.carlspring.strongbox.providers.repository.proxied;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.support.ArtifactEntrySearchCriteria;
import org.carlspring.strongbox.services.support.search.PagingCriteria;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import static org.carlspring.strongbox.services.support.ArtifactEntrySearchCriteria.Builder.anArtifactEntrySearchCriteria;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class LocalStorageProxyRepositoryExpiredArtifactsCleaner
{

    private final Logger logger = LoggerFactory.getLogger(LocalStorageProxyRepositoryExpiredArtifactsCleaner.class);

    @Inject
    protected ConfigurationManager configurationManager;

    @Inject
    protected LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Transactional(rollbackFor = Exception.class)
    public void cleanup(final Integer lastAccessedTimeInDays,
                        final Long minSizeInBytes)
            throws IOException, SearchException
    {
        final ArtifactEntrySearchCriteria searchCriteria = anArtifactEntrySearchCriteria()
                                                                   .withLastAccessedTimeInDays(lastAccessedTimeInDays)
                                                                   .withMinSizeInBytes(minSizeInBytes)
                                                                   .build();

        final List<ArtifactEntry> artifactEntries = artifactEntryService.findMatching(searchCriteria,
                                                                                      PagingCriteria.ALL);

        if (CollectionUtils.isEmpty(artifactEntries))
        {
            return;
        }

        logger.debug("Cleaning artifacts {}", artifactEntries);

        deleteFromDatabase(artifactEntries);
        deleteFromStorage(artifactEntries);
    }

    private void deleteFromStorage(final List<ArtifactEntry> artifactEntries)
            throws IOException
    {
        for (final ArtifactEntry artifactEntry : artifactEntries)
        {
            final Storage storage = configurationManager.getConfiguration().getStorage(artifactEntry.getStorageId());
            final Repository repository = storage.getRepository(artifactEntry.getRepositoryId());
            final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

            layoutProvider.getArtifactManagementService().delete(artifactEntry.getStorageId(),
                                                                 artifactEntry.getRepositoryId(),
                                                                 artifactEntry.getArtifactPath(), true);
        }
    }

    private void deleteFromDatabase(final List<ArtifactEntry> artifactEntries)
    {
        final int deletedCount = artifactEntryService.delete(artifactEntries);
        if (deletedCount != artifactEntries.size())
        {
            logger.warn(
                    "Unexpected differences on deleting artifact entries. Deleted count {}, artifact entries size {}",
                    deletedCount, artifactEntries.size());
        }
    }

}
