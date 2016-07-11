package org.carlspring.strongbox.services;

import org.carlspring.strongbox.client.ArtifactTransportException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

/**
 * @author mtodorov
 */
public interface ArtifactResolutionService
{

    InputStream getInputStream(String storageId,
                               String repositoryId,
                               String artifactPath)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException;

    OutputStream getOutputStream(String storageId,
                                 String repositoryId,
                                 String artifactPath)
            throws IOException;

}
