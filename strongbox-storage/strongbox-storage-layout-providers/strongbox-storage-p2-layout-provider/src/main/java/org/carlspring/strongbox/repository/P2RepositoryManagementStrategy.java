package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.storage.Storage;

import java.io.File;
import java.io.IOException;

/**
 * @author carlspring
 */
public class P2RepositoryManagementStrategy
        extends AbstractRepositoryManagementStrategy
{

    @Override
    public void createRepository(String storageId,
                                 String repositoryId)
            throws IOException
    {
        final Storage storage = getConfiguration().getStorage(storageId);
        final String storageBasedirPath = storage.getBasedir();

        createRepositoryStructure(storageBasedirPath, repositoryId);
    }

    @Override
    public void createRepositoryStructure(String storageBasedirPath,
                                          String repositoryId)
            throws IOException
    {
        final File storageBasedir = new File(storageBasedirPath);
        final File repositoryDir = new File(storageBasedir, repositoryId);

        if (!repositoryDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            repositoryDir.mkdirs();
            //noinspection ResultOfMethodCallIgnored
            new File(repositoryDir, ".trash").mkdirs();
        }
    }

    @Override
    public void initializeRepository(String storageId,
                                     String repositoryId)
            throws RepositoryInitializationException
    {

    }

}
