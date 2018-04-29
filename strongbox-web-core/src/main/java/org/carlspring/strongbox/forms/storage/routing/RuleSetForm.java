package org.carlspring.strongbox.forms.storage.routing;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pablo Tirado
 */
public class RuleSetForm
{
    @NotEmpty(message = "A group repository must be specified.")
    private String groupRepository;

    @Valid
    @NotEmpty(message = "A list of routing rules must be specified.")
    private List<RoutingRuleForm> routingRules = new ArrayList<>();

    public String getGroupRepository()
    {
        return groupRepository;
    }

    public void setGroupRepository(String groupRepository)
    {
        this.groupRepository = groupRepository;
    }

    public List<RoutingRuleForm> getRoutingRules()
    {
        return routingRules;
    }

    public void setRoutingRules(List<RoutingRuleForm> routingRules)
    {
        this.routingRules = routingRules;
    }
}
