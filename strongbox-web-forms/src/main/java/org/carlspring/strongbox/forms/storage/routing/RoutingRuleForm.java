package org.carlspring.strongbox.forms.storage.routing;

import javax.validation.constraints.NotEmpty;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Pablo Tirado
 */
public class RoutingRuleForm
{
    @NotEmpty(message = "A pattern must be specified.")
    private String pattern;

    @NotEmpty(message = "A set of repositories must be specified.")
    private Set<String> repositories = new LinkedHashSet<>();

    public String getPattern()
    {
        return pattern;
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public Set<String> getRepositories()
    {
        return repositories;
    }

    public void setRepositories(Set<String> repositories)
    {
        this.repositories = repositories;
    }
}
