package org.carlspring.strongbox.storage.routing;

import org.carlspring.strongbox.xml.RuleSetMapAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mtodorov
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "routing-rules")
public class MutableRoutingRules
        implements Serializable
{

    public static final String WILDCARD = "*";

    /**
     * List of accepted patterns.
     * <p>
     * K: groupRepositoryId V: RuleSet-s for this pattern.
     */
    @XmlElement(name = "accepted")
    @XmlJavaTypeAdapter(RuleSetMapAdapter.class)
    private Map<String, MutableRuleSet> accepted = new LinkedHashMap<>();

    /**
     * List of denied patterns.
     * <p>
     * K: groupRepositoryId V: RuleSet-s for this pattern.
     */
    @XmlElement(name = "denied")
    @XmlJavaTypeAdapter(RuleSetMapAdapter.class)
    private Map<String, MutableRuleSet> denied = new LinkedHashMap<>();


    public MutableRoutingRules()
    {
    }

    public MutableRuleSet getWildcardAcceptedRules()
    {
        return accepted.get(WILDCARD);
    }

    public MutableRuleSet getWildcardDeniedRules()
    {
        return denied.get(WILDCARD);
    }

    public void addAcceptRule(String groupRepositoryId,
                              MutableRuleSet ruleSet)
    {
        accepted.put(groupRepositoryId, ruleSet);
    }

    public MutableRuleSet getAcceptRules(String groupRepositoryId)
    {
        return accepted.get(groupRepositoryId);
    }

    public void addDenyRule(String groupRepositoryId,
                            MutableRuleSet ruleSet)
    {
        denied.put(groupRepositoryId, ruleSet);
    }

    public MutableRuleSet getWildcardDenyRules()
    {
        return getDenyRules(WILDCARD);
    }

    public MutableRuleSet getDenyRules(String groupRepositoryId)
    {
        return denied.get(groupRepositoryId);
    }

    public Map<String, MutableRuleSet> getAccepted()
    {
        return accepted;
    }

    public void setAccepted(Map<String, MutableRuleSet> accepted)
    {
        this.accepted = accepted;
    }

    public Map<String, MutableRuleSet> getDenied()
    {
        return denied;
    }

    public void setDenied(Map<String, MutableRuleSet> denied)
    {
        this.denied = denied;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("RoutingRules{");
        sb.append(", \n\taccepted=").append(accepted);
        sb.append(", \n\tdenied=").append(denied);
        sb.append('}');
        return sb.toString();
    }
}
