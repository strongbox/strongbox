package org.carlspring.strongbox.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Przemyslaw Fusik
 */
public class ArtifactCoordinateValidatorsAdapter
        extends XmlAdapter<ArtifactCoordinateValidators, Map<String, String>>
{

    @Override
    public Map<String, String> unmarshal(final ArtifactCoordinateValidators group)
            throws Exception
    {
        Map<String, String> result = new LinkedHashMap<>();
        group.getEntries().forEach(entry -> result.putIfAbsent(entry.getValue(), entry.getValue()));
        return result;
    }

    @Override
    public ArtifactCoordinateValidators marshal(final Map<String, String> v)
            throws Exception
    {
        ArtifactCoordinateValidators validators = new ArtifactCoordinateValidators();
        v.keySet().forEach(key -> validators.add(new ArtifactCoordinateValidator(key)));
        return validators;
    }

}
