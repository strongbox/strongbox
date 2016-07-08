package org.carlspring.strongbox.storage.routing;

import org.carlspring.strongbox.xml.RuleSetMapAdapter;

import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author mtodorov
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "routing-rules")
public class RoutingRules implements Serializable
{

    public static final String WILDCARD = "*";

    /**
     * Added to avoid a runtime error whereby the detachAll property is checked for existence but not actually used.
     */
    @JsonIgnore
    protected String detachAll;

    @Version
    @JsonIgnore
    protected Long version;

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

    public String getDetachAll()
    {
        return detachAll;
    }

    public void setDetachAll(String detachAll)
    {
        this.detachAll = detachAll;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public Map<String, RuleSet> getAccepted()
    {
        return accepted;
    }

    public void setAccepted(Map<String, RuleSet> accepted)
    {
        this.accepted = accepted;
    }

    public Map<String, RuleSet> getDenied()
    {
        return denied;
    }

    public void setDenied(Map<String, RuleSet> denied)
    {
        this.denied = denied;
    }
}
