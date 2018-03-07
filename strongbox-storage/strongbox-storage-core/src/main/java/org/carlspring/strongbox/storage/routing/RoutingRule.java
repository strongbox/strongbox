package org.carlspring.strongbox.storage.routing;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author mtodorov
 */
@Entity
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "rule")
public class RoutingRule
        implements Serializable
{

    @XmlAttribute
    private String pattern;

    @XmlElement(name = "repository")
    @XmlElementWrapper(name = "repositories")
    private Set<String> repositories = new LinkedHashSet<>();


    public RoutingRule()
    {
    }

    public RoutingRule(String pattern,
                       Set<String> repositories)
    {
        this.pattern = pattern;
        this.repositories = repositories;
    }

    public String getPattern()
    {
        return pattern;
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
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
