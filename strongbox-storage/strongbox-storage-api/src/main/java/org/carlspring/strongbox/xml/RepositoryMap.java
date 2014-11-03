package org.carlspring.strongbox.xml;

import org.carlspring.strongbox.storage.repository.Repository;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
public class RepositoryMap
{

    @XmlElement(name = "repository")
    private List<Repository> entries = new ArrayList<Repository>();


    public List<Repository> getEntries()
    {
        return entries;
    }

    public void add(Repository repository)
    {
        entries.add(repository);
    }

}

