package org.carlspring.strongbox.xml;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
public class RepositoryGroups
        implements Serializable
{

    @XmlElement(name = "repository")
    private Set<RepositoryGroup> entries = new LinkedHashSet<>();

    public Set<RepositoryGroup> getEntries()
    {
        return entries;
    }

    public void add(RepositoryGroup group)
    {
        entries.add(group);
    }
}
