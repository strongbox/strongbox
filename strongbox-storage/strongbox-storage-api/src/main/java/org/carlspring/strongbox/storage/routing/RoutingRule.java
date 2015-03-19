package org.carlspring.strongbox.storage.routing;

import javax.xml.bind.annotation.*;
import java.util.Set;

/**
 * @author mtodorov
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "rule")
public class RoutingRule
{

    @XmlAttribute
    private String type;

    @XmlAttribute (name = "group-repository")
    private String groupRepository;

    @XmlElement (name = "repository")
    @XmlElementWrapper(name = "repositories")
    private Set<String> repositories;

    @XmlElement (name = "pattern")
    private String pattern;


    public RoutingRule()
    {
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getGroupRepository()
    {
        return groupRepository;
    }

    public void setGroupRepository(String groupRepository)
    {
        this.groupRepository = groupRepository;
    }

    public Set<String> getRepositories()
    {
        return repositories;
    }

    public void setRepositories(Set<String> repositories)
    {
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

}
