package org.carlspring.strongbox.storage.resolvers;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * @author mtodorov
 */
@Component
public class GroupLocationResolver
        extends AbstractLocationResolver
{

    private static final Logger logger = LoggerFactory.getLogger(GroupLocationResolver.class);

    private String alias = "group";


    public GroupLocationResolver()
    {
    }

    @Override
    public InputStream getInputStream(String storageId,
                                      String repositoryId,
                                      String artifactPath)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);

        logger.debug("Checking in " + storage.getId() + ":" + repositoryId + "...");

        Repository groupRepository = storage.getRepository(repositoryId);

        for (String storageAndRepositoryId : groupRepository.getGroupRepositories())
        {
            String[] storageAndRepositoryIdTokens = storageAndRepositoryId.split(":");
            String sId = storageAndRepositoryIdTokens.length == 2 ? storageAndRepositoryIdTokens[0] : storage.getId();
            String rId = storageAndRepositoryIdTokens[storageAndRepositoryIdTokens.length < 2 ? 0 : 1];

            Repository r = getConfiguration().getStorage(sId).getRepository(rId);

            final File repoPath = new File(r.getBasedir());
            final File artifactFile = new File(repoPath, artifactPath).getCanonicalFile();

            logger.debug(" -> Checking for " + artifactFile.getCanonicalPath() + "...");

            if (artifactFile.exists())
            {
                logger.debug("Resolved " + artifactFile.getCanonicalPath() + "!");

                return new FileInputStream(artifactFile);
            }
        }

        return null;
    }

    @Override
    public OutputStream getOutputStream(String storageId,
                                        String repositoryId,
                                        String artifactPath)
            throws IOException
    {
        // It should not be possible to write artifacts to a group repository.
        // A group repository should only serve artifacts that already exist
        // in the repositories within the group.

        return null;
    }

    @Override
    public void delete(String storageId,
                       String repositoryId,
                       String path,
                       boolean force)
            throws IOException
    {
        throw new IOException("Group repositories cannot perform delete operations.");
    }

    @Override
    public void deleteTrash(String storageId, String repositoryId)
            throws IOException
    {
        throw new IOException("Group repositories cannot perform delete operations.");
    }

    @Override
    public void deleteTrash()
            throws IOException
    {
        throw new IOException("Group repositories cannot perform delete operations.");
    }

    @Override
    public void initialize()
            throws IOException
    {
        logger.debug("Initialized GroupLocationResolver.");
    }

    @Override
    public String getAlias()
    {
        return alias;
    }

    @Override
    public void setAlias(String alias)
    {
        this.alias = alias;
    }

}
