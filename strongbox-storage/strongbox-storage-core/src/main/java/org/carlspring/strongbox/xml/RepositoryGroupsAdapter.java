package org.carlspring.strongbox.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Przemyslaw Fusik
 */
public class RepositoryGroupsAdapter
        extends XmlAdapter<RepositoryGroups, Map<String, String>>
{

    @Override
    public Map<String, String> unmarshal(final RepositoryGroups group)
            throws Exception
    {
        Map<String, String> result = new LinkedHashMap<>();
        group.getEntries().forEach(entry -> result.putIfAbsent(entry.getValue(), entry.getValue()));
        return result;
    }

    @Override
    public RepositoryGroups marshal(final Map<String, String> v)
            throws Exception
    {
        RepositoryGroups groups = new RepositoryGroups();
        v.keySet().forEach(key -> groups.add(new RepositoryGroup(key)));
        return groups;
    }
}
