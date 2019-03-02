package org.carlspring.strongbox.storage.routing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "routing-rules")
public class MutableRoutingRules
        implements Serializable
{

    @XmlElement(name = "routing-rule")
    private List<MutableRoutingRule> rules = new ArrayList<>();

    public List<MutableRoutingRule> getRules()
    {
        return rules;
    }

    public void setRules(List<MutableRoutingRule> rules)
    {
        this.rules = rules;
    }
}
