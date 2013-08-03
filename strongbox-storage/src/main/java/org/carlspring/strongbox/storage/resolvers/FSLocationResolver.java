package org.carlspring.strongbox.storage.resolvers;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class FSLocationResolver
        implements LocationResolver
{

    private static final Logger logger = LoggerFactory.getLogger(FSLocationResolver.class);

    private DataCenter dataCenter = new DataCenter();


    public FSLocationResolver()
    {
    }

    @Override
    public InputStream getInputStream(String repository,
                                      String artifactPath)
            throws IOException
    {
        for (Map.Entry entry : dataCenter.getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            if (storage.containsRepository(repository))
            {
                logger.debug("Checking in storage " + storage.getBasedir() + "...");

                final Map<String, Repository> repositories = storage.getRepositories();

                for (String key : repositories.keySet())
                {
                    Repository r = repositories.get(key);

                    logger.debug("Checking in repository " + r.getName() + "...");

                    final File repoPath = new File(storage.getBasedir(), r.getName());
                    final File artifactFile = new File(repoPath, artifactPath).getCanonicalFile();

                    logger.debug("Checking for " + artifactFile.getCanonicalPath() + "...");

                    if (artifactFile.exists())
                    {
                        logger.info("Resolved " + artifactFile.getCanonicalPath() + "!");

                        return new FileInputStream(artifactFile);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void initialize()
            throws IOException
    {
        final Configuration configuration = ConfigurationManager.getInstance().getConfiguration();

        final Map<String, Storage> storages = configuration.getStorages();

        dataCenter.setStorages(storages);

        logger.info("Initialized FSLocationResolver.");
    }

    public DataCenter getDataCenter()
    {
        return dataCenter;
    }

    public void setDataCenter(DataCenter dataCenter)
    {
        this.dataCenter = dataCenter;
    }

}
