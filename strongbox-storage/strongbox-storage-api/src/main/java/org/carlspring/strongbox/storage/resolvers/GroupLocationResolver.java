package org.carlspring.strongbox.storage.resolvers;

import org.apache.commons.io.FileUtils;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;

/**
 * @author mtodorov
 */
@Component
public class GroupLocationResolver
        extends FSLocationResolver
{

    private static final Logger logger = LoggerFactory.getLogger(GroupLocationResolver.class);

    private String alias = "group";

    @Autowired
    private ConfigurationManager configurationManager;


    public GroupLocationResolver()
    {
    }

    @Override
    public InputStream getInputStream(String repositoryId,
                                      String artifactPath)
            throws IOException
    {
        for (Map.Entry entry : configurationManager.getConfiguration().getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            if (storage.containsRepository(repositoryId))
            {
                logger.debug("Checking in " + storage.getId() + ":" + repositoryId + "...");

                Repository groupRepository = storage.getRepository(repositoryId);

                for (String storageAndRepositoryId : groupRepository.getGroupRepositories())
                {
                    String[] storageAndRepositoryIdTokens = storageAndRepositoryId.split(":");
                    String sId = storageAndRepositoryIdTokens.length == 2 ? storageAndRepositoryIdTokens[0] : storage.getId();
                    String rId = storageAndRepositoryIdTokens[storageAndRepositoryIdTokens.length < 2 ? 0 : 1];

                    Repository r = configurationManager.getConfiguration().getStorage(sId).getRepository(rId);

                    final File repoPath = new File(r.getBasedir());
                    final File artifactFile = new File(repoPath, artifactPath).getCanonicalFile();

                    logger.debug(" -> Checking for " + artifactFile.getCanonicalPath() + "...");

                    if (artifactFile.exists())
                    {
                        logger.debug("Resolved " + artifactFile.getCanonicalPath() + "!");

                        return new FileInputStream(artifactFile);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public OutputStream getOutputStream(String repositoryId,
                                        String artifactPath)
            throws IOException
    {
        // It should not be possible to write artifacts to a group repository.
        // A group repository should only serve artifacts that already exist
        // in the repositories within the group.

        return null;
    }

    @Override
    public void delete(String repositoryId,
                       String path,
                       boolean force)
            throws IOException
    {
        throw new IOException("Group repositories cannot perform delete operations.");
    }

    @Override
    public void deleteTrash(String repository)
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

}
