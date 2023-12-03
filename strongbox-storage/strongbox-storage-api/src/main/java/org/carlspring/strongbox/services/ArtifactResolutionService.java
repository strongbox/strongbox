package org.carlspring.strongbox.services;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryStreamSupport.RepositoryInputStream;
import org.carlspring.strongbox.providers.io.RepositoryStreamSupport.RepositoryOutputStream;

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
