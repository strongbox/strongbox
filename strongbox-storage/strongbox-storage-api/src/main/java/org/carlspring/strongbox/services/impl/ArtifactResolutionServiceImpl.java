package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.ArtifactStorageException;
import org.carlspring.strongbox.storage.resolvers.LocationResolver;
import org.carlspring.strongbox.storage.validation.resource.ArtifactOperationsValidator;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private ConfigurationManager configurationManager;

    @Autowired
    private ArtifactOperationsValidator artifactOperationsValidator;


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
    public ArtifactInputStream getInputStream(String storageId,
                                              String repositoryId,
                                              String artifactPath)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException
    {
        artifactOperationsValidator.validate(storageId, repositoryId, artifactPath);

        final Repository repository = getStorage(storageId).getRepository(repositoryId);

        LocationResolver resolver = getResolvers().get(repository.getImplementation());
        ArtifactInputStream is = resolver.getInputStream(storageId, repositoryId, artifactPath);

        if (is == null)
        {
            throw new ArtifactResolutionException("Artifact " + artifactPath + " not found.");
        }

        return is;
    }

    @Override
    public OutputStream getOutputStream(String storageId,
                                        String repositoryId,
                                        String artifactPath)
            throws IOException
    {
        artifactOperationsValidator.validate(storageId, repositoryId, artifactPath);

        final Repository repository = getStorage(storageId).getRepository(repositoryId);

        LocationResolver resolver = getResolvers().get(repository.getImplementation());
        OutputStream os = resolver.getOutputStream(storageId, repositoryId, artifactPath);

        if (os == null)
        {
            throw new ArtifactStorageException("Artifact " + artifactPath + " cannot be stored.");
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

    public Storage getStorage(String storageId)
    {
        return configurationManager.getConfiguration().getStorage(storageId);
    }

}
