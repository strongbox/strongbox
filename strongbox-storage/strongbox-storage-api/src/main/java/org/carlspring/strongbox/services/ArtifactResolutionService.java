package org.carlspring.strongbox.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;

/**
 * @author mtodorov
 */
public interface ArtifactResolutionService
{
    
    ArtifactCoordinates getArtifactCoordinates(String storageId,
                                               String repositoryId,
                                               String artifactPath);

    InputStream getInputStream(String storageId,
                               String repositoryId,
                               String artifactPath)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException;

    ArtifactOutputStream getOutputStream(String storageId,
                                         String repositoryId,
                                         String artifactPath)
            throws IOException,
                   ProviderImplementationException,
                   NoSuchAlgorithmException;
    
    URL resolveArtifactResource(String storageId,
                                String repositoryId,
                                ArtifactCoordinates artifactCoordinates)
            throws MalformedURLException, 
                   IOException;

    RepositoryFileAttributes getAttributes(String storageId,
                                           String repositoryId,
                                           String path) 
                             throws IOException,
                                    NoSuchAlgorithmException,
                                    ArtifactTransportException,
                                    ProviderImplementationException;

}
