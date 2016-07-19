package org.carlspring.strongbox.storage.routing;

import org.carlspring.strongbox.xml.RuleSetMapAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mtodorov
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "routing-rules")
public class RoutingRules
{

    public static final String WILDCARD = "*";

    /**
     * List of accepted patterns.
     *
     * K: groupRepositoryId
     * V: RuleSet-s for this pattern.
     */
    @XmlElement(name = "accepted")
    @XmlJavaTypeAdapter(RuleSetMapAdapter.class)
    private Map<String, RuleSet> accepted = new LinkedHashMap<>();

    /**
     * List of denied patterns.
     *
     * K: groupRepositoryId
     * V: RuleSet-s for this pattern.
     */
    @XmlElement(name = "denied")
    @XmlJavaTypeAdapter(RuleSetMapAdapter.class)
    private Map<String, RuleSet> denied = new LinkedHashMap<>();
    
    
    public RoutingRules()
    {
    }

    public RuleSet getWildcardAcceptedRules()
    {
        return accepted.get(WILDCARD);
    }

    public RuleSet getWildcardDeniedRules()
    {
        return denied.get(WILDCARD);
    }

    public void addAcceptRule(String groupRepositoryId, RuleSet ruleSet)
    {
        accepted.put(groupRepositoryId, ruleSet);
    }

    public RuleSet getAcceptRules(String groupRepositoryId)
    {
        return accepted.get(groupRepositoryId);
    }

    public void addDenyRule(String groupRepositoryId, RuleSet ruleSet)
    {
        denied.put(groupRepositoryId, ruleSet);
    }

    public RuleSet getWildcardDenyRules()
    {
        return getDenyRules(WILDCARD);
    }

    public RuleSet getDenyRules(String groupRepositoryId)
    {
        return denied.get(groupRepositoryId);
    }

}
