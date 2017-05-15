package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.Storage;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("nugetArtifactManagementService")
public class NugetArtifactManagementService
        implements ArtifactManagementService
{

    @Inject
    private ConfigurationManager configurationManager;


    @Override
    public void store(String storageId,
                      String repositoryId,
                      String path,
                      InputStream is)
            throws IOException, ProviderImplementationException, NoSuchAlgorithmException
    {
        throw new UnsupportedOperationException("This operation is not yet implemented!");
    }

    @Override
    public void store(String storageId,
                      String repositoryId,
                      String path,
                      InputStream is,
                      OutputStream os)
            throws IOException, ProviderImplementationException, NoSuchAlgorithmException
    {
        throw new UnsupportedOperationException("This operation is not yet implemented!");
    }

    @Override
    public InputStream resolve(String storageId,
                               String repositoryId,
                               String path)
            throws IOException, ArtifactTransportException, ProviderImplementationException
    {
        throw new UnsupportedOperationException("This operation is not yet implemented!");
    }

    @Override
    public void delete(String storageId,
                       String repositoryId,
                       String artifactPath,
                       boolean force)
            throws IOException
    {
        throw new UnsupportedOperationException("This operation is not yet implemented!");
    }

    @Override
    public boolean contains(String storageId,
                            String repositoryId,
                            String artifactPath)
            throws IOException
    {
        throw new UnsupportedOperationException("This operation is not yet implemented!");
    }

    @Override
    public void copy(String srcStorageId,
                     String srcRepositoryId,
                     String path,
                     String destStorageId,
                     String destRepositoryId)
            throws IOException
    {
        throw new UnsupportedOperationException("This operation is not yet implemented!");
    }

    @Override
    public void removeTimestampedSnapshots(String storageId,
                                           String repositoryId,
                                           String artifactPath,
                                           int numberToKeep,
                                           int keepPeriod)
            throws IOException
    {
        throw new UnsupportedOperationException("This operation is not yet implemented!");
    }

    @Override
    public Storage getStorage(String storageId)
    {
        return getConfiguration().getStorages().get(storageId);
    }

    @Override
    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
