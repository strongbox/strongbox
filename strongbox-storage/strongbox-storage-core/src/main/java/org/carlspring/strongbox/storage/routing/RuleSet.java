package org.carlspring.strongbox.storage.routing;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
@Embeddable
@XmlRootElement(name = "rule-set")
@XmlAccessorType(XmlAccessType.FIELD)
public class RuleSet
        implements Serializable
{

    @XmlAttribute(name = "group-repository")
    private String groupRepository;

    @XmlElement(name = "rule")
    private List<RoutingRule> routingRules = new ArrayList<>();


    public RuleSet()
    {
    }

    public String getGroupRepository()
    {
        return groupRepository;
    }

    public void setGroupRepository(String groupRepository)
    {
        this.groupRepository = groupRepository;
    }

    public List<RoutingRule> getRoutingRules()
    {
        return routingRules;
    }

    public void setRoutingRules(List<RoutingRule> routingRules)
    {
        this.routingRules = routingRules;
    }

    @Override
    public String toString()
    {
        return "RuleSet{" + "groupRepository='" + groupRepository + '\'' + ", routingRules=" + routingRules + '}';
    }
}
