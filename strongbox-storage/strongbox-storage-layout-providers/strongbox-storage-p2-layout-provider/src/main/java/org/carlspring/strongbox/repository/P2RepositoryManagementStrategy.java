package org.carlspring.strongbox.repository;

import java.io.File;
import java.io.IOException;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * @author carlspring
 */
public class P2RepositoryManagementStrategy
        extends AbstractRepositoryManagementStrategy
{

    @Override
    protected void createRepositoryInternal(Storage storage,
                                            Repository repository)
        throws IOException,
        RepositoryManagementStrategyException
    {
        //Do nothing
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
