package org.carlspring.strongbox.xml;

import org.carlspring.strongbox.storage.repository.MutableRepository;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mtodorov
 */
public class RepositoryMapAdapter
        extends XmlAdapter<RepositoryMap, Map<String, MutableRepository>>
{

    @Override
    public RepositoryMap marshal(Map<String, MutableRepository> map)
            throws Exception
    {
        RepositoryMap repositoryMap = new RepositoryMap();
        if (map != null)
        {
            for (Map.Entry<String, MutableRepository> entry : map.entrySet())
            {
                repositoryMap.getEntries().add(entry.getValue());
            }
        }

        return repositoryMap;
    }

    @Override
    public Map<String, MutableRepository> unmarshal(RepositoryMap repositoryMap)
            throws Exception
    {
        Map<String, MutableRepository> map = new LinkedHashMap<>();

        if (repositoryMap != null && repositoryMap.getEntries() != null)
        {
            List<MutableRepository> entries = repositoryMap.getEntries();

            map = new LinkedHashMap<>(entries.size());
            for (MutableRepository repository : entries)
            {
                map.put(repository.getId(), repository);
            }
        }

        return map;
    }

}
