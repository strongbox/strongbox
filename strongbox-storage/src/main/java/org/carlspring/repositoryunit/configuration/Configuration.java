package org.carlspring.repositoryunit.configuration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.carlspring.repositoryunit.storage.Storage;
import org.carlspring.repositoryunit.storage.repository.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mtodorov
 */
public class Configuration
{

    @XStreamAlias(value = "storages")
    private Map<String, Storage> storages = new LinkedHashMap<String, Storage>();

    @XStreamAlias (value = "resolvers")
    private List<String> resolvers = new ArrayList<String>();

    @XStreamAlias(value = "port")
    private int port = 48080;

    @XStreamAlias(value = "version")
    private String version = "1.0";

    @XStreamOmitField
    private String filename;


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
        for (String resolver : resolvers)
        {
            System.out.println(" -> " + resolver);
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

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public List<String> getResolvers()
    {
        return resolvers;
    }

    public void setResolvers(List<String> resolvers)
    {
        this.resolvers = resolvers;
    }

    public void addResolver(String resolverAbsoluteClassName)
    {
        resolvers.add(resolverAbsoluteClassName);
    }

    public void removeResolver(String resolverAbsoluteClassName)
    {
        resolvers.remove(resolverAbsoluteClassName);
    }

}
