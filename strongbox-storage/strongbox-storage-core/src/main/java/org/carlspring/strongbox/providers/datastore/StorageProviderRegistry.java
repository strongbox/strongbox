package org.carlspring.strongbox.providers.datastore;

import org.carlspring.strongbox.providers.AbstractMappedProviderRegistry;
import org.carlspring.strongbox.services.ConfigurationManagementService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("storageProviderRegistry")
public class StorageProviderRegistry extends AbstractMappedProviderRegistry<StorageProvider>
{

    private static final Logger logger = LoggerFactory.getLogger(StorageProviderRegistry.class);

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private List<StorageProvider> storageProviders;

    @Override
    @PostConstruct
    public void initialize()
    {
        storageProviders.stream().forEach(sp -> addProvider(sp.getAlias(), sp));
        logger.info("Initialized the storage provider registry.");
    }


}
