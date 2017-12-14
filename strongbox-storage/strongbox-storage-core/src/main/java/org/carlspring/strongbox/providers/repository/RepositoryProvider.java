package org.carlspring.strongbox.providers.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.RepositoryInputStream;
import org.carlspring.strongbox.io.RepositoryOutputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;

/**
 * @author carlspring
 */
public interface RepositoryProvider
{

    void register();

    String getAlias();

    RepositoryInputStream getInputStream(String storageId, String repositoryId, String path)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException;

    RepositoryOutputStream getOutputStream(String storageId,
                                         String repositoryId,
                                         String path)
            throws IOException, NoSuchAlgorithmException;
    
    List<Path> search(RepositorySearchRequest searchRequest, RepositoryPageRequest pageRequest);
    
    Long count(RepositorySearchRequest searchRequest);

}
