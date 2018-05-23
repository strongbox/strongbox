package org.carlspring.strongbox.storage.routing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author mtodorov
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "rule")
public class MutableRoutingRule
        implements Serializable
{

    @XmlAttribute
    private String pattern;
    
    private volatile transient Pattern regex;

    @XmlElement(name = "repository")
    @XmlElementWrapper(name = "repositories")
    private Set<String> repositories = new LinkedHashSet<>();


    public MutableRoutingRule()
    {
    }

    public MutableRoutingRule(String pattern,
                              Set<String> repositories)
    {
        setPattern(pattern);
        this.repositories = repositories;
    }

    public String getPattern()
    {
        return pattern;
    }

    public void setPattern(String pattern)
    {
        this.regex = Pattern.compile(pattern);
        this.pattern = pattern;
    }

    public Pattern getRegex()
    {
        if (regex == null)
        {
            regex = Pattern.compile(pattern);
        }
        
        return regex;
    }

    public Set<String> getRepositories()
    {
        return repositories;
    }

    public void setRepositories(Set<String> repositories)
    {
        this.repositories = repositories;
    }

    @Override
    public String toString()
    {
        return "RoutingRule{" +
                ", \n\tpattern='" + pattern + '\'' +
                ", \n\trepositories=" + repositories +
                '}';
    }

}
