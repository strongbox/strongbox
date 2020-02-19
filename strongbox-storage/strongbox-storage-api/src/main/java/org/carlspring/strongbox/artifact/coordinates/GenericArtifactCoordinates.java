package org.carlspring.strongbox.artifact.coordinates;

import java.io.Serializable;
import java.util.Map;

/**
 * @author sbespalov
 *
 */
public interface GenericArtifactCoordinates extends Serializable
{

    String getVersion();

    Map<String, String> getCoordinates();

}
