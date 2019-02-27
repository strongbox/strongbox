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

    @XmlElement(name = "accepted")
    private List<MutableRuleSet> accepted = new ArrayList<>();

    @XmlElement(name = "denied")
    private List<MutableRuleSet> denied = new ArrayList<>();

    public List<MutableRuleSet> getAccepted()
    {
        return accepted;
    }

    public void setAccepted(List<MutableRuleSet> accepted)
    {
        this.accepted = accepted;
    }

    public List<MutableRuleSet> getDenied()
    {
        return denied;
    }

    public void setDenied(List<MutableRuleSet> denied)
    {
        this.denied = denied;
    }
}
