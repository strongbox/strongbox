package org.carlspring.strongbox.storage.resolvers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class ArtifactResolutionService
{

    public static final String RESOLVER_INMEMORY = "org.carlspring.strongbox.storage.resolvers.InMemoryLocationResolver";

    private boolean inMemoryModeOnly = true; //false;

    private boolean allowInMemory = false;

    private Set<LocationResolver> resolvers = new LinkedHashSet<LocationResolver>();


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
    {
        ServiceLoader<LocationResolver> resolversFromServiceLoader = ServiceLoader.load(LocationResolver.class);

        for (LocationResolver resolver : resolversFromServiceLoader)
        {
            resolvers.add(resolver);
        }
    }

    public Set<LocationResolver> getResolvers()
    {
        if (resolvers == null)
        {
            initializeResolvers();
        }

        return resolvers;
    }

    public InputStream getInputStream(String repository, String artifactPath)
            throws ArtifactResolutionException, IOException
    {
        InputStream is = null;

        for (LocationResolver resolver : getResolvers())
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

    public OutputStream getOutputStream(String repository, String artifactPath)
            throws ArtifactResolutionException, IOException
    {
        OutputStream os = null;

        for (LocationResolver resolver : getResolvers())
        {
            os = resolver.getOutputStream(repository, artifactPath);
            if (os != null)
            {
                break;
            }
        }

        if (os == null)
        {
            throw new ArtifactResolutionException("Artifact " + artifactPath + " could not be stored.");
        }

        return os;
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

}
