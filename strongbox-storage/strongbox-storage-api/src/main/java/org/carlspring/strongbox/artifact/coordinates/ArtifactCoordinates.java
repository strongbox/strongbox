package org.carlspring.strongbox.artifact.coordinates;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author carlspring
 */
@XmlRootElement(name = "artifactCoordinates")
public interface ArtifactCoordinates<T extends ArtifactCoordinates<T, V>, V extends Comparable<V>> extends Comparable<T>, Serializable
{

    String getId();

    void setId(String id);

    String getVersion();

    void setVersion(String version);
    
    V getNativeVersion();

    Map<String, String> getCoordinates();
    
    Map<String, String> dropVersion();
    
    String toPath();
    
    URI toResource();

}
