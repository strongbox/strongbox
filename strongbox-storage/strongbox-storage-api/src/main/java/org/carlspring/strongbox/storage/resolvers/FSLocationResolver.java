package org.carlspring.strongbox.storage.resolvers;

import org.apache.commons.io.FileUtils;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.DataCenter;
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
    public LocationOutput getOutputStream(String repository,
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

                return new LocationOutput(artifactFile, new FileOutputStream(artifactFile));
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
                final File basedirTrash = new File(repoPath, ".trash");

                logger.debug("Checking for " + artifactFile.getCanonicalPath() + "...");

                if (artifactFile.exists())
                {
                    if (!artifactFile.isDirectory())
                    {
                        if (r.isTrashEnabled())
                        {
                            File trashFile = new File(basedirTrash, path).getCanonicalFile();
                            FileUtils.moveFile(artifactFile, trashFile);

                            logger.debug("Moved /" + repository + "/" + path + " to trash (" + trashFile.getAbsolutePath() + ").");

                            // Move the checksums to the trash as well
                            moveChecksumsToTrash(repository, path, artifactFile, basedirTrash);
                        }
                        else
                        {
                            //noinspection ResultOfMethodCallIgnored
                            artifactFile.delete();
                        }
                    }
                    else
                    {
                        if (r.isTrashEnabled())
                        {
                            File trashFile = new File(basedirTrash, path).getCanonicalFile();
                            FileUtils.moveDirectory(artifactFile, trashFile);

                            logger.debug("Moved /" + repository + "/" + path + " to trash (" + trashFile.getAbsolutePath() + ").");
                        }
                        else
                        {
                            FileUtils.deleteDirectory(artifactFile);
                        }
                    }

                    logger.debug("Removed /" + repository + "/" + path);
                }
            }
        }
    }

    private void moveChecksumsToTrash(String repository,
                                      String path,
                                      File artifactFile,
                                      File basedirTrash)
            throws IOException
    {
        File md5ChecksumFile = new File(artifactFile.getAbsolutePath() + ".md5");
        if (md5ChecksumFile.exists())
        {
            File md5TrashFile = new File(basedirTrash, path + ".md5").getCanonicalFile();
            FileUtils.moveFile(md5ChecksumFile, md5TrashFile);

            logger.debug("Moved /" + repository + "/" + path + ".md5" + " to trash (" + md5TrashFile.getAbsolutePath() + ").");
        }

        File sha1ChecksumFile = new File(artifactFile.getAbsolutePath() + ".sha1");
        if (sha1ChecksumFile.exists())
        {
            File sha1TrashFile = new File(basedirTrash, path + ".sha1").getCanonicalFile();
            FileUtils.moveFile(sha1ChecksumFile, sha1TrashFile);

            logger.debug("Moved /" + repository + "/" + path + ".sha1" + " to trash (" + sha1TrashFile.getAbsolutePath() + ").");
        }
    }

    @Override
    public void deleteTrash(String repository)
            throws IOException
    {
        for (Map.Entry entry : dataCenter.getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            if (storage.containsRepository(repository))
            {
                final Map<String, Repository> repositories = storage.getRepositories();

                Repository r = repositories.get(repository);

                logger.debug("Emptying trash for repository " + r.getName() + "...");

                final File repoPath = new File(storage.getBasedir(), r.getName());
                final File basedirTrash = new File(repoPath, ".trash");

                FileUtils.deleteDirectory(basedirTrash);

                //noinspection ResultOfMethodCallIgnored
                basedirTrash.mkdirs();
            }
        }
    }

    @Override
    public void deleteTrash()
            throws IOException
    {
        for (Map.Entry entry : dataCenter.getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            final Map<String, Repository> repositories = storage.getRepositories();
            for (Repository repository : repositories.values())
            {
                logger.debug("Emptying trash for repository " + repository.getName() + "...");

                final File repoPath = new File(storage.getBasedir(), repository.getName());
                final File basedirTrash = new File(repoPath, ".trash");

                FileUtils.deleteDirectory(basedirTrash);

                //noinspection ResultOfMethodCallIgnored
                basedirTrash.mkdirs();
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
