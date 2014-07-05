package org.carlspring.strongbox.storage.services.impl;

import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.LocationResolver;
import org.carlspring.strongbox.storage.services.ArtifactResolutionService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.util.RepositoryUtils.checkRepositoryExists;

/**
 * @author mtodorov
 */
@Component
public class ArtifactResolutionServiceImpl
        implements ArtifactResolutionService
{

    @Resource(name = "resolvers")
    private Map<String, LocationResolver> resolvers;

    @Autowired
    private DataCenter dataCenter;


    @Override
    @PostConstruct
    public void listResolvers()
    {
        System.out.println("Loading resolvers...");

        for (String key : getResolvers().keySet())
        {
            LocationResolver resolver = getResolvers().get(key);
            System.out.println(" -> " + resolver.getClass());
        }
    }

    @Override
    public InputStream getInputStream(String repositoryName,
                                      String artifactPath)
            throws ArtifactResolutionException, IOException
    {
        final Repository repository = dataCenter.getRepository(repositoryName);
        checkRepositoryExists(repositoryName, repository);

        LocationResolver resolver = getResolvers().get(repository.getImplementation());
        InputStream is = resolver.getInputStream(repositoryName, artifactPath);

        if (is == null)
        {
            throw new ArtifactResolutionException("Artifact " + artifactPath + " not found.");
        }

        return is;
    }

    @Override
    public OutputStream getOutputStream(String repositoryName,
                                        String artifactPath)
            throws ArtifactResolutionException, IOException
    {
        final Repository repository = dataCenter.getRepository(repositoryName);
        checkRepositoryExists(repositoryName, repository);

        LocationResolver resolver = getResolvers().get(repository.getImplementation());
        OutputStream os = resolver.getOutputStream(repositoryName, artifactPath);

        if (os == null)
        {
            throw new ArtifactResolutionException("Artifact " + artifactPath + " cannot be stored.");
        }

        return os;
    }

    @Override
    public Map<String, LocationResolver> getResolvers()
    {
        return resolvers;
    }

    public void setResolvers(Map<String, LocationResolver> resolvers)
    {
        this.resolvers = resolvers;
    }

    @Override
    public DataCenter getDataCenter()
    {
        return dataCenter;
    }

    public void setDataCenter(DataCenter dataCenter)
    {
        this.dataCenter = dataCenter;
    }

}
