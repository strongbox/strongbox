package org.carlspring.strongbox.storage.resolvers;

import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class ArtifactResolutionService
{

    @Resource(name = "resolvers")
    private Map<String, LocationResolver> resolvers;

    @Autowired
    private DataCenter dataCenter;


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

    public InputStream getInputStream(String repositoryName, String artifactPath)
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

    public OutputStream getOutputStream(String repositoryName, String artifactPath)
            throws ArtifactResolutionException, IOException
    {
        final Repository repository = dataCenter.getRepository(repositoryName);
        checkRepositoryExists(repositoryName, repository);

        LocationResolver resolver = getResolvers().get(repository.getImplementation());
        OutputStream os = resolver.getOutputStream(repositoryName, artifactPath);

        if (os == null)
        {
            throw new ArtifactResolutionException("Artifact " + artifactPath + " could not be stored.");
        }

        return os;
    }

    public void delete(String repositoryName, String artifactPath)
            throws ArtifactResolutionException, IOException
    {
        final Repository repository = dataCenter.getRepository(repositoryName);
        checkRepositoryExists(repositoryName, repository);

        LocationResolver resolver = getResolvers().get(repository.getImplementation());

        resolver.delete(repositoryName, artifactPath);
    }

    // TODO: This should have restricted access.
    public void deleteTrash(String repositoryName)
            throws ArtifactResolutionException, IOException
    {
        final Repository repository = dataCenter.getRepository(repositoryName);
        checkRepositoryExists(repositoryName, repository);

        LocationResolver resolver = getResolvers().get(repository.getImplementation());
        resolver.deleteTrash(repositoryName);
    }

    // TODO: This should have restricted access.
    public void deleteTrash()
            throws ArtifactResolutionException, IOException
    {
        for (LocationResolver resolver : getResolvers().values())
        {
            resolver.deleteTrash();
        }
    }

    private void checkRepositoryExists(String repositoryName,
                                       Repository repository)
            throws ArtifactResolutionException
    {
        if (repository == null)
        {
            throw new ArtifactResolutionException("Repository " + repositoryName + " does not exist.");
        }
    }

    public Map<String, LocationResolver> getResolvers()
    {
        return resolvers;
    }

    public void setResolvers(Map<String, LocationResolver> resolvers)
    {
        this.resolvers = resolvers;
    }

    public DataCenter getDataCenter()
    {
        return dataCenter;
    }

    public void setDataCenter(DataCenter dataCenter)
    {
        this.dataCenter = dataCenter;
    }

}
