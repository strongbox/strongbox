package org.carlspring.strongbox.providers.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;

/**
 * @author carlspring
 */
public interface RepositoryProvider
{

    void register();

    String getAlias();

    ArtifactInputStream getInputStream(String storageId, String repositoryId, String path)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException;

    ArtifactOutputStream getOutputStream(String storageId,
                                         String repositoryId,
                                         String path)
            throws IOException, NoSuchAlgorithmException;
    
    List<Path> search(String storageId,
                      String repositoryId,
                      Map<String, String> coordinates,
                      int skip,
                      int limit,
                      String orderBy);

}
