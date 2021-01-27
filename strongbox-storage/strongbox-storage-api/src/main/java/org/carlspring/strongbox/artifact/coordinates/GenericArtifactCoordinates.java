package org.carlspring.strongbox.artifact.coordinates;

import java.util.Map;

import org.carlspring.strongbox.data.domain.DomainObject;
import org.carlspring.strongbox.data.domain.EntityHierarchyNode;

/**
 * @author sbespalov
 *
 */
public interface GenericArtifactCoordinates extends DomainObject, EntityHierarchyNode<GenericArtifactCoordinates>
{

    String getVersion();

    Map<String, String> getCoordinates();

    default String getPath()
    {
        return getUuid();
    }

}
