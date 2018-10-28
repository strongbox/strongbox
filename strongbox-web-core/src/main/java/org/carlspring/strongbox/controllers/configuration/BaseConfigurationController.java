package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.services.ConfigurationManagementService;

/**
 * @author Pablo Tirado
 */
public abstract class BaseConfigurationController
        extends BaseController
{

    protected final ConfigurationManagementService configurationManagementService;


    protected BaseConfigurationController(ConfigurationManagementService configurationManagementService)
    {
        this.configurationManagementService = configurationManagementService;
    }

}
