package org.carlspring.strongbox.xml.parsers;

import javax.xml.bind.annotation.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author mtodorov
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Dummy
{

    @XmlElement
    private String name;

    @XmlElement(name = "alias")
    @XmlElementWrapper(name = "aliases")
    private Set<String> aliases = new LinkedHashSet<String>();


    public Dummy()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void addAlias(String alias)
    {
        aliases.add(alias);
    }

    public Set<String> getAliases()
    {
        return aliases;
    }

    public void setAliases(Set<String> aliases)
    {
        this.aliases = aliases;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Dummy)) return false;

        Dummy dummy = (Dummy) o;

        if (!aliases.equals(dummy.aliases)) return false;
        if (!name.equals(dummy.name)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + aliases.hashCode();
        return result;
    }

}

