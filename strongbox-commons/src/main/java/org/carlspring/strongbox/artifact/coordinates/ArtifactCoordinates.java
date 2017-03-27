package org.carlspring.strongbox.artifact.coordinates;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author carlspring
 */
@XmlRootElement(name = "artifactCoordinates")
public interface ArtifactCoordinates
        extends Serializable
{

    String getId();

    void setId(String id);

    String getVersion();

    void setVersion(String version);

    String toPath();

    void defineCoordinates(String... coordinates);

    void defineCoordinate(String coordinate);

    String getCoordinate(String coordinate);

    String setCoordinate(String coordinate, String value);

    Map<String, String> getCoordinates();

    void setCoordinates(Map<String, String> coordinates);

    void dump();

}
