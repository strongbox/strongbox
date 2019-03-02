package org.carlspring.strongbox.forms.storage.routing;

import org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pablo Tirado
 * @author Przemyslaw Fusik
 */
public class RoutingRuleForm
{

    private String storageId;

    private String repositoryId;

    @NotEmpty(message = "A pattern must be specified.")
    private String pattern;

    @NotNull(message = "A type must be specified.")
    private RoutingRuleTypeEnum type;

    @NotEmpty(message = "A list of repositories must be specified.")
    private List<RoutingRuleRepositoryForm> repositories = new ArrayList<>();

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

    public RoutingRuleTypeEnum getType()
    {
        return type;
    }

    public void setType(RoutingRuleTypeEnum type)
    {
        this.type = type;
    }

    public List<RoutingRuleRepositoryForm> getRepositories()
    {
        return repositories;
    }

    public void setRepositories(List<RoutingRuleRepositoryForm> repositories)
    {
        this.repositories = repositories;
    }
}
