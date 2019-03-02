package org.carlspring.strongbox.storage.routing;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "routing-rule")
@XmlAccessorType(XmlAccessType.FIELD)
public class MutableRoutingRule
        implements Serializable
{

    @XmlAttribute(name = "storage-id")
    private String storageId;

    @XmlAttribute(name = "repository-id")
    private String repositoryId;

    @XmlAttribute
    private String pattern;

    @XmlAttribute
    private String type;

    @XmlElement(name = "repository")
    @XmlElementWrapper(name = "repositories")
    private List<MutableRoutingRuleRepository> repositories = new ArrayList<>();

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public String getPattern()
    {
        return pattern;
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public List<MutableRoutingRuleRepository> getRepositories()
    {
        return repositories;
    }

    public void setRepositories(List<MutableRoutingRuleRepository> repositories)
    {
        this.repositories = repositories;
    }

    public static MutableRoutingRule create(String groupStorageId,
                                            String groupRepositoryId,
                                            List<MutableRoutingRuleRepository> repositories,
                                            String rulePattern,
                                            RoutingRuleTypeEnum type)
    {
        MutableRoutingRule routingRule = new MutableRoutingRule();
        routingRule.setPattern(rulePattern);
        routingRule.setStorageId(groupStorageId);
        routingRule.setRepositoryId(groupRepositoryId);
        routingRule.setType(type.getType());
        routingRule.setRepositories(repositories);
        return routingRule;
    }
}
