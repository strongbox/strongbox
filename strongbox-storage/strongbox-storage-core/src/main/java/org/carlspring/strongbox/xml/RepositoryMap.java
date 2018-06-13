package org.carlspring.strongbox.xml;

import org.carlspring.strongbox.storage.repository.MutableRepository;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
public class RepositoryMap
{

    @XmlElement(name = "repository")
    private List<MutableRepository> entries = new ArrayList<>();


    public List<MutableRepository> getEntries()
    {
        return entries;
    }

    public void add(MutableRepository repository)
    {
        entries.add(repository);
    }

}

