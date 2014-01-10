package org.carlspring.strongbox.configuration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.springframework.core.io.Resource;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionService;
import org.carlspring.strongbox.storage.resolvers.LocationResolutionManager;
import org.carlspring.strongbox.storage.resolvers.LocationResolver;

import java.util.*;

/**
 * @author mtodorov
 */
public class Configuration
{

    @XStreamAlias(value = "storages")
    private Map<String, Storage> storages = new LinkedHashMap<String, Storage>();

    @XStreamAlias(value = "port")
    private int port = 48080;

    @XStreamAlias(value = "version")
    private String version = "1.0";

    @XStreamOmitField
    private Resource resource;


    public Configuration()
    {
    }

    public void dump()
    {
        System.out.println("Configuration version: " + version);
        System.out.println("Listening on port: " + port);

        System.out.println("Loading storages...");
        for (String storageKey : storages.keySet())
        {
            System.out.println(" -> Storage: " + storageKey);
            Storage storage = storages.get(storageKey);
            for (String repositoryKey : storage.getRepositories().keySet())
            {
                System.out.println("    -> Repository: " + repositoryKey);
            }
        }

        System.out.println("Loading resolvers...");
        for (LocationResolver resolver : ArtifactResolutionService.getResolvers())
        {
            System.out.println(" -> " + resolver.getClass());
        }
    }

    public Map<String, Storage> getStorages()
    {
        return storages;
    }

    public void setStorages(Map<String, Storage> storages)
    {
        this.storages = storages;
    }

    public void addStorage(Storage storage)
    {
        storages.put(storage.getBasedir(), storage);
    }

    public void removeStorage(Storage storage)
    {
        storages.remove(storage.getBasedir());
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public Resource getResource()
    {
        return resource;
    }

    public void setResource(Resource resource)
    {
        this.resource = resource;
    }

}
