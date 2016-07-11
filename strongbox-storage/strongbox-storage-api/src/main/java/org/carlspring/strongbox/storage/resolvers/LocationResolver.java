package org.carlspring.strongbox.storage.resolvers;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactInputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

/**
 * @author mtodorov
 */
public interface LocationResolver
{

    void register();

    LocationResolverRegistry getLocationResolverRegistry();

    ArtifactInputStream getInputStream(String storageId,
                                       String repositoryId,
                                       String path)
            throws IOException, NoSuchAlgorithmException, ArtifactTransportException;

    OutputStream getOutputStream(String storageId,
                                 String repositoryId,
                                 String path)
            throws IOException;

    boolean contains(String storageId,
                     String repositoryId,
                     String path)
            throws IOException;

    void delete(String storageId,
                String repositoryId,
                String path,
                boolean force)
            throws IOException;

    void deleteTrash(String storageId,
                     String repositoryId)
            throws IOException;

    void deleteTrash()
            throws IOException;

    void undelete(String storageId,
                  String repositoryId,
                  String path)
            throws IOException;

    void undeleteTrash(String storageId,
                       String repositoryId)
            throws IOException;

    void undeleteTrash()
            throws IOException;

    void initialize()
            throws IOException;

    String getAlias();

}
