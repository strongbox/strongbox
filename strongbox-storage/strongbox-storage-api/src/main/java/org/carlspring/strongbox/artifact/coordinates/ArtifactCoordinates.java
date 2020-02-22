package org.carlspring.strongbox.artifact.coordinates;

import java.net.URI;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author carlspring
 */
@XmlRootElement(name = "artifactCoordinates")
public interface ArtifactCoordinates<C extends ArtifactCoordinates<C, V>, V extends Comparable<V>> extends Comparable<C>, GenericArtifactCoordinates
{

    String getId();
    
    V getNativeVersion();

    Map<String, String> dropVersion();
    
    String buildPath();
    
    URI buildResource();

}