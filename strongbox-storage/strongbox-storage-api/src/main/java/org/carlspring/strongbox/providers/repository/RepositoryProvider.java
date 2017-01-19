package org.carlspring.strongbox.providers.repository;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

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

    ArtifactOutputStream getOutputStream(String storageId, String repositoryId, String path)
            throws IOException, NoSuchAlgorithmException;

}
