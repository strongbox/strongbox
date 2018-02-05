package org.carlspring.strongbox.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.RepositoryInputStream;
import org.carlspring.strongbox.io.RepositoryOutputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;

/**
 * @author mtodorov
 */
public interface ArtifactResolutionService
{
    
    RepositoryInputStream getInputStream(String storageId,
                                         String repositoryId,
                                         String artifactPath)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException;

    RepositoryOutputStream getOutputStream(String storageId,
                                           String repositoryId,
                                           String artifactPath)
            throws IOException,
                   ProviderImplementationException,
                   NoSuchAlgorithmException;
    
    URL resolveResource(String storageId,
                        String repositoryId,
                        String path)
            throws MalformedURLException, 
                   IOException;
    
    RepositoryPath resolvePath(String storageId,
                               String repositoryId,
                               String path) 
            throws IOException;
}
