package org.carlspring.strongbox.rest.serialization.search;

import org.carlspring.strongbox.configuration.ConfigurationManager;

/**
 * @author mtodorov
 */
public abstract class AbstractArtifactSearchSerializer implements ArtifactSearchSerializer
{

    private ConfigurationManager configurationManager;


    public AbstractArtifactSearchSerializer(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public String getURLFor(String storage,
                            String repository,
                            String pathToArtifactFile)
    {
        String baseUrl = configurationManager.getConfiguration().getBaseUrl();
        baseUrl = (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/");

        return baseUrl + "storages/" + storage + "/" + repository + "/" + pathToArtifactFile;
    }

    public ConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

}
