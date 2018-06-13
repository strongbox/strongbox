package org.carlspring.strongbox.xml;

import org.carlspring.strongbox.storage.MutableStorage;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
public class StorageMap
{

    @XmlElement(name = "storage")
    private List<MutableStorage> entries = new ArrayList<>();


    public List<MutableStorage> getEntries()
    {
        return entries;
    }

    public void add(MutableStorage storage)
    {
        entries.add(storage);
    }

}

