package org.carlspring.strongbox.storage.routing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
@XmlRootElement (name = "rule-set")
@XmlAccessorType(XmlAccessType.FIELD)
public class RuleSet
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

}
