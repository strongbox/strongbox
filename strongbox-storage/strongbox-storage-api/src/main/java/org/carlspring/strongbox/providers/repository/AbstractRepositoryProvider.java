package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author carlspring
 */
public abstract class AbstractRepositoryProvider implements RepositoryProvider
{

    @Autowired
    private LayoutProviderRegistry layoutProviderRegistry;


    public LayoutProviderRegistry getLayoutProviderRegistry()
    {
        return layoutProviderRegistry;
    }

    public void setLayoutProviderRegistry(LayoutProviderRegistry layoutProviderRegistry)
    {
        this.layoutProviderRegistry = layoutProviderRegistry;
    }

}
