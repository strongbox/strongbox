package org.carlspring.strongbox.xml;

import org.carlspring.strongbox.storage.Storage;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
public class StorageMap
{

    @XmlElement(name = "storage")
    private List<Storage> entries = new ArrayList<Storage>();


    public List<Storage> getEntries()
    {
        return entries;
    }

    public void add(Storage storage)
    {
        entries.add(storage);
    }

}

