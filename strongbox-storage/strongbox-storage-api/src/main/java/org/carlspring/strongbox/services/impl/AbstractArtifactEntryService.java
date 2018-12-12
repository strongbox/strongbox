package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.data.service.SynchronizedCommonCrudService;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.services.ArtifactEntryService;

public abstract class AbstractArtifactEntryService
        extends SynchronizedCommonCrudService<ArtifactEntry>
        implements ArtifactEntryService
{

}
