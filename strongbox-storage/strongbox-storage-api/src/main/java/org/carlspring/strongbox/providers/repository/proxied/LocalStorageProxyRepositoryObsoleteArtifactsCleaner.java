package org.carlspring.strongbox.providers.repository.proxied;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class LocalStorageProxyRepositoryObsoleteArtifactsCleaner
{

    @Transactional
    public void cleanup(final Integer uselessnessDays,
                        final Integer minSize)
    {
        // TODO :)
    }

}
