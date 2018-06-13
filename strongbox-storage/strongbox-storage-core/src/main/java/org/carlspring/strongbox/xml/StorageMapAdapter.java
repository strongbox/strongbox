package org.carlspring.strongbox.xml;

import org.carlspring.strongbox.storage.MutableStorage;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mtodorov
 */
public class StorageMapAdapter
        extends XmlAdapter<StorageMap, Map<String, MutableStorage>>
{

    @Override
    public StorageMap marshal(Map<String, MutableStorage> map)
            throws Exception
    {
        StorageMap storageMap = new StorageMap();
        for (Map.Entry<String, MutableStorage> entry : map.entrySet())
        {
            storageMap.getEntries().add(entry.getValue());
        }

        return storageMap;
    }

    @Override
    public Map<String, MutableStorage> unmarshal(StorageMap storageMap)
            throws Exception
    {
        List<MutableStorage> entries = storageMap.getEntries();

        Map<String, MutableStorage> map = new LinkedHashMap<>(entries.size());
        for (MutableStorage storage : entries)
        {
            map.put(storage.getId(), storage);
        }

        return map;
    }

}
