package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.AbstractMappedProviderRegistry;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class LayoutProviderRegistry
        extends AbstractMappedProviderRegistry<LayoutProvider>
{

    private static final Logger logger = LoggerFactory.getLogger(LayoutProviderRegistry.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private Optional<List<LayoutProvider>> layoutProviders;

    public static LayoutProvider getLayoutProvider(Repository repository,
                                                   LayoutProviderRegistry layoutProviderRegistry)
            throws ProviderImplementationException
    {
        return layoutProviderRegistry.getProvider(repository.getLayout());
    }

    @Override
    @PostConstruct
    public void initialize()
    {
        layoutProviders.ifPresent(providers -> providers.stream().forEach(lp -> addProvider(lp.getAlias(), lp)));
        logger.info("Initialized the layout provider registry.");
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    public Storage getStorage(String storageId)
    {
        return configurationManager.getConfiguration().getStorage(storageId);
    }

}
