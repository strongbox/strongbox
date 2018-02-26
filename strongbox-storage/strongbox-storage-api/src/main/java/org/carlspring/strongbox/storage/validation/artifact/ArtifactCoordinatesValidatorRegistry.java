package org.carlspring.strongbox.storage.validation.artifact;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.providers.AbstractMappedProviderRegistry;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.validation.ArtifactCoordinatesValidator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class ArtifactCoordinatesValidatorRegistry
        extends AbstractMappedProviderRegistry<ArtifactCoordinatesValidator>
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactCoordinatesValidatorRegistry.class);

    @Inject
    private ConfigurationManagementService configurationManagementService;


    public ArtifactCoordinatesValidatorRegistry()
    {
    }

    @Override
    @PostConstruct
    public void initialize()
    {
        logger.info("Initialized the artifact coordinates validator registry.");
    }

    public Configuration getConfiguration()
    {
        return configurationManagementService.getConfiguration();
    }

    public Storage getStorage(String storageId)
    {
        return configurationManagementService.getConfiguration().getStorage(storageId);
    }

}
