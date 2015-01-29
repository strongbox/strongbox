package org.carlspring.strongbox.storage.resolvers;

import org.carlspring.strongbox.configuration.ConfigurationManager;
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


    public FSLocationResolver()
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

                final File repoPath = new File(storage.getRepository(repositoryId).getBasedir());
                final File artifactFile = new File(repoPath, artifactPath).getCanonicalFile();

                logger.debug(" -> Checking for " + artifactFile.getCanonicalPath() + "...");

                if (artifactFile.exists())
                {
                    logger.debug("Resolved " + artifactFile.getCanonicalPath() + "!");

                    return new FileInputStream(artifactFile);
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
        for (Map.Entry entry : configurationManager.getConfiguration().getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            if (storage.containsRepository(repositoryId))
            {
                final File repoPath = new File(storage.getRepository(repositoryId).getBasedir());
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
    public void delete(String repositoryId,
                       String path,
                       boolean force)
            throws IOException
    {
        for (Map.Entry entry : configurationManager.getConfiguration().getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            if (storage.containsRepository(repositoryId))
            {
                final Map<String, Repository> repositories = storage.getRepositories();

                Repository r = repositories.get(repositoryId);

                final File repoPath = new File(r.getBasedir());
                final File artifactFile = new File(repoPath, path).getCanonicalFile();
                final File basedirTrash = r.getTrashDir();

                logger.debug("Checking in " + storage.getId() + ":" + r.getId() + "(" + artifactFile.getCanonicalPath() + ")...");

                if (artifactFile.exists())
                {
                    if (!artifactFile.isDirectory())
                    {
                        if ((r.isTrashEnabled() && !force) || (force && !r.allowsForceDeletion()))
                        {
                            File trashFile = new File(basedirTrash, path).getCanonicalFile();
                            FileUtils.moveFile(artifactFile, trashFile);

                            logger.debug("Moved /" + repositoryId + "/" + path + " to trash (" + trashFile.getAbsolutePath() + ").");

                            // Move the checksums to the trash as well
                            moveChecksumsToTrash(repositoryId, path, artifactFile, basedirTrash);
                        }
                        else
                        {
                            //noinspection ResultOfMethodCallIgnored
                            artifactFile.delete();
                            deleteChecksums(repositoryId, path, artifactFile);
                        }
                    }
                    else
                    {
                        if ((r.isTrashEnabled() && !force) || (force && !r.allowsForceDeletion()))
                        {
                            File trashFile = new File(basedirTrash, path).getCanonicalFile();
                            FileUtils.moveDirectory(artifactFile, trashFile);

                            logger.debug("Moved /" + repositoryId + "/" + path + " to trash (" + trashFile.getAbsolutePath() + ").");
                        }
                        else
                        {
                            FileUtils.deleteDirectory(artifactFile);
                        }
                    }

                    logger.debug("Removed /" + repositoryId + "/" + path);
                }
            }
        }
    }

    private void moveChecksumsToTrash(String repositoryId,
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

            logger.debug("Moved /" + repositoryId + "/" + path + ".md5" + " to trash (" + md5TrashFile.getAbsolutePath() + ").");
        }

        File sha1ChecksumFile = new File(artifactFile.getAbsolutePath() + ".sha1");
        if (sha1ChecksumFile.exists())
        {
            File sha1TrashFile = new File(basedirTrash, path + ".sha1").getCanonicalFile();
            FileUtils.moveFile(sha1ChecksumFile, sha1TrashFile);

            logger.debug("Moved /" + repositoryId + "/" + path + ".sha1" + " to trash (" + sha1TrashFile.getAbsolutePath() + ").");
        }
    }

    private void deleteChecksums(String repositoryId,
                                 String path,
                                 File artifactFile)
            throws IOException
    {
        File md5ChecksumFile = new File(artifactFile.getAbsolutePath() + ".md5");
        if (md5ChecksumFile.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            md5ChecksumFile.delete();

            logger.debug("Deleted /" + repositoryId + "/" + path + ".md5.");
        }

        File sha1ChecksumFile = new File(artifactFile.getAbsolutePath() + ".sha1");
        if (sha1ChecksumFile.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            sha1ChecksumFile.delete();

            logger.debug("Deleted /" + repositoryId + "/" + path + ".sha1.");
        }
    }

    @Override
    public void deleteTrash(String repository)
            throws IOException
    {
        for (Map.Entry entry : configurationManager.getConfiguration().getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            if (storage.containsRepository(repository))
            {
                final Map<String, Repository> repositories = storage.getRepositories();

                Repository r = repositories.get(repository);

                logger.debug("Emptying trash for repository " + r.getId() + "...");

                final File basedirTrash = r.getTrashDir();

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
        for (Map.Entry entry : configurationManager.getConfiguration().getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            final Map<String, Repository> repositories = storage.getRepositories();
            for (Repository repository : repositories.values())
            {
                logger.debug("Emptying trash for repository " + repository.getId() + "...");

                final File basedirTrash = repository.getTrashDir();

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
        logger.debug("Initialized FSLocationResolver.");
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

    public ConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

}
