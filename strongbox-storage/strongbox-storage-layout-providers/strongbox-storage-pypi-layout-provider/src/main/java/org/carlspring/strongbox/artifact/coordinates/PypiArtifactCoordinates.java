package org.carlspring.strongbox.artifact.coordinates;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

import org.semver.Version;

/**
 * This class is an {@link ArtifactCoordinates} implementation for PyPi Wheel
 * artifacts. <br>
 *
 * See <a href="https://www.python.org/dev/peps/pep-0427/#file-format">Official PyPi package
 * specification</a>.
 *
 * @author carlspring
 */
@Entity
@SuppressWarnings("serial")
@XmlRootElement(name = "PypiArtifactCoordinates")
@XmlAccessorType(XmlAccessType.NONE)
@ArtifactCoordinatesLayout(name = PypiArtifactCoordinates.LAYOUT_NAME, alias = PypiArtifactCoordinates.LAYOUT_ALIAS)
public class PypiArtifactCoordinates
        extends AbstractArtifactCoordinates<PypiArtifactCoordinates, Version>
{

    public static final String LAYOUT_NAME = "PyPi";

    public static final String LAYOUT_ALIAS = LAYOUT_NAME;


    // TODO: Fix all these with proper implementations
    // TODO: Adding this class was required in order for
    // TODO: the rest of the code to be able to compile

    public static PypiArtifactCoordinates parse(String path)
    {
        return null;
    }


    @Override
    public String getId()
    {
        return null;
    }

    @Override
    public void setId(String id)
    {

    }

    @Override
    public String getVersion()
    {
        return null;
    }

    @Override
    public void setVersion(String version)
    {

    }

    @Override
    public Version getNativeVersion()
    {
        return null;
    }

    @Override
    public Map<String, String> dropVersion()
    {
        return null;
    }

    @Override
    public String toPath()
    {
        return null;
    }

}
