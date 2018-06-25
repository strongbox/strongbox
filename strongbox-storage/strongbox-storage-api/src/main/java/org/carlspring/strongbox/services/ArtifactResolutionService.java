package org.carlspring.strongbox.services;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import org.carlspring.strongbox.io.RepositoryInputStream;
import org.carlspring.strongbox.io.RepositoryOutputStream;
import org.carlspring.strongbox.providers.io.RepositoryPath;

/**
 * @author mtodorov
 */
public interface ArtifactResolutionService
{
    
    RepositoryInputStream getInputStream(RepositoryPath path)
            throws IOException;

    RepositoryOutputStream getOutputStream(RepositoryPath repositoryPath)
            throws IOException,
                   NoSuchAlgorithmException;
    
    RepositoryPath resolvePath(String storageId,
                               String repositoryId,
                               String path) 
            throws IOException;
}
