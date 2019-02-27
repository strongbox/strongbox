package org.carlspring.strongbox.storage.routing;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    @XmlElement(name = "repository")
    @XmlElementWrapper(name = "repositories")
    private List<MutableRoutingRuleRepository> repositories = new ArrayList<>();

    public String getPattern()
    {
        return pattern;
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public List<MutableRoutingRuleRepository> getRepositories()
    {
        return repositories;
    }

    public void setRepositories(List<MutableRoutingRuleRepository> repositories)
    {
        this.repositories = repositories;
    }
}
