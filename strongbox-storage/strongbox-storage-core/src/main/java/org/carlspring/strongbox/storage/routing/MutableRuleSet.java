package org.carlspring.strongbox.storage.routing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "rule-set")
@XmlAccessorType(XmlAccessType.FIELD)
public class MutableRuleSet
        implements Serializable
{

    @XmlAttribute(name = "group-repository")
    private String groupRepository;

    @XmlElement(name = "rule")
    private List<MutableRoutingRule> routingRules = new ArrayList<>();


    public MutableRuleSet()
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

    public List<MutableRoutingRule> getRoutingRules()
    {
        return routingRules;
    }

    public void setRoutingRules(List<MutableRoutingRule> routingRules)
    {
        this.routingRules = routingRules;
    }

    @Override
    public String toString()
    {
        return "RuleSet{" + "groupRepository='" + groupRepository + '\'' + ", routingRules=" + routingRules + '}';
    }
}
