package org.carlspring.strongbox.storage.routing;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author mtodorov
 */
@Embeddable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "rule")
public class RoutingRule
        implements Serializable
{

    @XmlAttribute
    private String pattern;
    
    private volatile transient Pattern regex;

    @XmlElement(name = "repository")
    @XmlElementWrapper(name = "repositories")
    private Set<String> repositories = new LinkedHashSet<>();


    public RoutingRule()
    {
    }

    public RoutingRule(String pattern,
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
    public String toString() {
        return "RoutingRule{" +
                ", \n\tpattern='" + pattern + '\'' +
                ", \n\trepositories=" + repositories +
                '}';
    }
}
