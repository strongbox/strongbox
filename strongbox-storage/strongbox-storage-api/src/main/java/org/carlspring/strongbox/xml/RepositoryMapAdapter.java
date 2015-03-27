package org.carlspring.strongbox.xml;

import org.carlspring.strongbox.storage.repository.Repository;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mtodorov
 */
public class RepositoryMapAdapter
        extends XmlAdapter<RepositoryMap, Map<String, Repository>>
{

    @Override
    public RepositoryMap marshal(Map<String, Repository> map)
            throws Exception
    {
        RepositoryMap repositoryMap = new RepositoryMap();
        for (Map.Entry<String, Repository> entry : map.entrySet())
        {
            repositoryMap.getEntries().add(entry.getValue());
        }

        return repositoryMap;
    }

    @Override
    public Map<String, Repository> unmarshal(RepositoryMap repositoryMap)
            throws Exception
    {
        List<Repository> entries = repositoryMap.getEntries();

        Map<String, Repository> map = new LinkedHashMap<>(entries.size());
        for (Repository repository : entries)
        {
            map.put(repository.getId(), repository);
        }

        return map;
    }

}
