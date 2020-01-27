package org.carlspring.strongbox.artifact.coordinates;

import java.util.Map;

import org.carlspring.strongbox.data.domain.DomainObject;

/**
 * @author sbespalov
 *
 */
public interface GenericArtifactCoordinates extends DomainObject
{

    String getVersion();

    Map<String, String> getCoordinates();

    default String getPath()
    {
        return getUuid();
    }

}
