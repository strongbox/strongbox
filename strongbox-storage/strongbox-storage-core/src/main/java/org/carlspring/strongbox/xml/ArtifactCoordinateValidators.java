package org.carlspring.strongbox.xml;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
public class ArtifactCoordinateValidators
        implements Serializable
{

    @XmlElement(name = "artifact-coordinate-validator")
    private Set<ArtifactCoordinateValidator> entries = new LinkedHashSet<>();

    public Set<ArtifactCoordinateValidator> getEntries()
    {
        return entries;
    }

    public void add(ArtifactCoordinateValidator validator)
    {
        entries.add(validator);
    }
}
