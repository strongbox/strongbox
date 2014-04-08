package org.carlspring.strongbox.storage.resolvers;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.*;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class FSLocationResolver
        implements LocationResolver
{

    private static final Logger logger = LoggerFactory.getLogger(FSLocationResolver.class);

    private String alias = "file-system";

    @Autowired
    private ConfigurationManager configurationManager;

    @Autowired
    private DataCenter dataCenter;


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

                for (Map.Entry<String, Repository> e : repositories.entrySet())
                {
                    Repository r = e.getValue();

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
    public OutputStream getOutputStream(String repository,
                                        String artifactPath)
            throws IOException
    {
        for (Map.Entry entry : dataCenter.getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            if (storage.containsRepository(repository))
            {
                final Map<String, Repository> repositories = storage.getRepositories();

                Repository r = repositories.get(repository);

                final File repoPath = new File(storage.getBasedir(), r.getName());
                final File artifactFile = new File(repoPath, artifactPath).getCanonicalFile();

                if (!artifactFile.getParentFile().exists())
                {
                    //noinspection ResultOfMethodCallIgnored
                    artifactFile.getParentFile().mkdirs();
                }

                return new FileOutputStream(artifactFile);
            }
        }

        return null;
    }

    @Override
    public void delete(String repository,
                       String path)
            throws IOException
    {
        for (Map.Entry entry : dataCenter.getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            if (storage.containsRepository(repository))
            {
                logger.debug("Checking in storage " + storage.getBasedir() + "...");

                final Map<String, Repository> repositories = storage.getRepositories();

                Repository r = repositories.get(repository);

                logger.debug("Checking in repository " + r.getName() + "...");

                final File repoPath = new File(storage.getBasedir(), r.getName());
                final File artifactFile = new File(repoPath, path).getCanonicalFile();

                logger.debug("Checking for " + artifactFile.getCanonicalPath() + "...");

                if (artifactFile.exists())
                {
                    if (!artifactFile.isDirectory())
                    {
                        //noinspection ResultOfMethodCallIgnored
                        artifactFile.delete();
                    }
                    else
                    {
                        FileUtils.deleteDirectory(artifactFile);
                    }

                    logger.debug("Removed /" + repository + path);
                }
            }
        }
    }

    @Override
    public void initialize()
            throws IOException
    {
        final Map<String, Storage> storages = configurationManager.getConfiguration().getStorages();

        dataCenter.setStorages(storages);

        logger.info("Initialized FSLocationResolver.");
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

    public DataCenter getDataCenter()
    {
        return dataCenter;
    }

    public void setDataCenter(DataCenter dataCenter)
    {
        this.dataCenter = dataCenter;
    }

    public ConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

}
