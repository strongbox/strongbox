package org.carlspring.strongbox.providers.repository.proxied;

import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.support.ArtifactEntrySearchCriteria;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import static org.carlspring.strongbox.services.support.ArtifactEntrySearchCriteria.Builder.anArtifactEntrySearchCriteria;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class LocalStorageProxyRepositoryObsoleteArtifactsCleaner
{

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Transactional
    public void cleanup(final Integer uselessnessDays,
                        final Integer minSizeInBytes)
    {
        final ArtifactEntrySearchCriteria searchCriteria = anArtifactEntrySearchCriteria()
                                                                   .withUselessnessDays(uselessnessDays)
                                                                   .withMinSizeInBytes(minSizeInBytes)
                                                                   .build();
    }

}
