package org.carlspring.repositoryunit.storage.resolvers;

import org.carlspring.repositoryunit.configuration.Configuration;
import org.carlspring.repositoryunit.configuration.ConfigurationManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author mtodorov
 */
public class ArtifactResolutionService
{

    public static final String RESOLVER_INMEMORY = "org.carlspring.repositoryunit.storage.resolvers.InMemoryLocationResolver";

    private boolean inMemoryModeOnly = true; //false;

    private boolean allowInMemory = false;

    private Set<LocationResolver> resolvers = new LinkedHashSet<LocationResolver>();

    private static ArtifactResolutionService instance = new ArtifactResolutionService();


    public static ArtifactResolutionService getInstance()
            throws ClassNotFoundException,
                   IOException,
                   InstantiationException,
                   IllegalAccessException
    {
        if (instance == null)
        {
            instance = new ArtifactResolutionService();
            instance.initialize();
        }

        return instance;
    }

    public void initialize()
            throws ClassNotFoundException,
                   InstantiationException,
                   IllegalAccessException,
                   IOException
    {
        initializeResolvers(); // Forward the resolver handling to a separate method,
                               // as other stuff might need to initialized here later on as well.
    }

    private void initializeResolvers()
            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        final Configuration configuration = ConfigurationManager.getInstance().getConfiguration();

        if (configuration != null)
        {
            for (String resolverName : configuration.getResolvers())
            {
                Class<?> clazz = Class.forName(resolverName);
                LocationResolver resolver = (LocationResolver) clazz.newInstance();
                resolver.initialize();

                resolvers.add(resolver);
            }

            if (configuration.getResolvers().contains(RESOLVER_INMEMORY))
            {
                allowInMemory = true;

                if (configuration.getResolvers().size() == 1)
                {
                    if (configuration.getResolvers().get(0).equals(RESOLVER_INMEMORY))
                    {
                        inMemoryModeOnly = true;
                    }
                }
            }
        }
        else
        {
            resolvers.add(new InMemoryLocationResolver());
            allowInMemory = true;
            inMemoryModeOnly = true;
        }
    }

    public InputStream getInputStream(String repository, String artifactPath)
            throws ArtifactResolutionException, IOException
    {
        InputStream is = null;

        for (LocationResolver resolver : resolvers)
        {
            is = resolver.getInputStream(repository, artifactPath);
            if (is != null)
            {
                break;
            }
        }

        if (is == null)
        {
            throw new ArtifactResolutionException("Artifact " + artifactPath + " not found.");
        }

        return is;
    }

    public boolean isInMemoryModeOnly()
    {
        return inMemoryModeOnly;
    }

    public void setInMemoryModeOnly(boolean inMemoryModeOnly)
    {
        this.inMemoryModeOnly = inMemoryModeOnly;
    }

    public boolean allowsInMemory()
    {
        return allowInMemory;
    }

    public void setAllowInMemory(boolean allowInMemory)
    {
        this.allowInMemory = allowInMemory;
    }

    public Set<LocationResolver> getResolvers()
    {
        return resolvers;
    }

    public void setResolvers(Set<LocationResolver> resolvers)
    {
        this.resolvers = resolvers;
    }

}
