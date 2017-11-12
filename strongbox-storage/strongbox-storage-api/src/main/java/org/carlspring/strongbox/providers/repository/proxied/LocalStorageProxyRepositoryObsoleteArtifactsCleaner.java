package org.carlspring.strongbox.providers.repository.proxied;

import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.support.ArtifactEntrySearchCriteria;
import org.carlspring.strongbox.services.support.search.PagingCriteria;

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
public class LocalStorageProxyRepositoryObsoleteArtifactsCleaner
{

    private final Logger logger = LoggerFactory.getLogger(LocalStorageProxyRepositoryObsoleteArtifactsCleaner.class);

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private ArtifactManagementService artifactManagementService;

    @Transactional(rollbackFor = Exception.class)
    public void cleanup(final Integer uselessnessDays,
                        final Long minSizeInBytes)
            throws IOException, SearchException
    {
        final ArtifactEntrySearchCriteria searchCriteria = anArtifactEntrySearchCriteria()
                                                                   .withUselessnessDays(uselessnessDays)
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

    private void deleteFromStorage(List<ArtifactEntry> artifactEntries)
            throws IOException
    {
        for (final ArtifactEntry artifactEntry : artifactEntries)
        {
            artifactManagementService.delete(artifactEntry.getStorageId(), artifactEntry.getRepositoryId(),
                                             artifactEntry.getArtifactPath(), true);
        }
    }

    private void deleteFromDatabase(List<ArtifactEntry> artifactEntries)
    {
        int deletedCount = artifactEntryService.delete(artifactEntries);
        if (deletedCount != artifactEntries.size())
        {
            logger.warn(
                    "Unexpected differences on deleting artifact entries. Deleted count {}, artifact entries size {}",
                    deletedCount, artifactEntries.size());
        }
    }

}
