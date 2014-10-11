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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ArtifactResolutionServiceImpl.class);

    @Resource(name = "resolvers")
    private Map<String, LocationResolver> resolvers;

    @Autowired
    private DataCenter dataCenter;


    @Override
    @PostConstruct
    public void listResolvers()
    {
        logger.info("Loading resolvers...");

        for (String key : getResolvers().keySet())
        {
            LocationResolver resolver = getResolvers().get(key);
            logger.info(" -> " + resolver.getClass());
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
