package org.carlspring.strongbox.xml;

import org.carlspring.strongbox.storage.Storage;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mtodorov
 */
public class StorageMapAdapter
        extends XmlAdapter<StorageMap, Map<String, Storage>>
{

    @Override
    public StorageMap marshal(Map<String, Storage> map)
            throws Exception
    {
        StorageMap storageMap = new StorageMap();
        for (Map.Entry<String, Storage> entry : map.entrySet())
        {
            storageMap.getEntries().add(entry.getValue());
        }

        return storageMap;
    }

    @Override
    public Map<String, Storage> unmarshal(StorageMap storageMap)
            throws Exception
    {
        List<Storage> entries = storageMap.getEntries();

        Map<String, Storage> map = new LinkedHashMap<String, Storage>(entries.size());
        for (Storage storage : entries)
        {
            map.put(storage.getId(), storage);
        }

        return map;
    }

}