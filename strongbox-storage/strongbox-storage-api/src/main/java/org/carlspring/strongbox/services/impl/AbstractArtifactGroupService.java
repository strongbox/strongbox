package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.data.service.CommonCrudService;
import org.carlspring.strongbox.domain.ArtifactGroup;
import org.carlspring.strongbox.services.ArtifactGroupService;

/**
 * @author sbespalov
 *
 * @param <T>
 */
public abstract class AbstractArtifactGroupService<T extends ArtifactGroup> extends CommonCrudService<T> implements ArtifactGroupService<T>
{


}
