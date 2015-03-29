package org.carlspring.strongbox.storage.routing;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "rules")
@Deprecated
public class Rules
{

    @XmlElement(name = "rule")
    private List<RoutingRule> routingRules = new ArrayList<>();


    public Rules()
    {
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
