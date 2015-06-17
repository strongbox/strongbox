package org.carlspring.strongbox.storage.resolvers;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.io.ArtifactFile;
import org.carlspring.strongbox.io.ArtifactFileOutputStream;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;

/**
 * @author mtodorov
 */
@Component
public class FSLocationResolver
        extends AbstractLocationResolver
{

    private static final Logger logger = LoggerFactory.getLogger(FSLocationResolver.class);

    private String alias = "file-system";


    public FSLocationResolver()
    {
    }

    @Override
    public InputStream getInputStream(String storageId,
                                      String repositoryId,
                                      String artifactPath,
                                      long offset)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);

        logger.debug("Checking in " + storage.getId() + ":" + repositoryId + "...");

        final File repoPath = new File(storage.getRepository(repositoryId).getBasedir());
        final File artifactFile = new File(repoPath, artifactPath).getCanonicalFile();

        logger.debug(" -> Checking for " + artifactFile.getCanonicalPath() + "...");

        if (artifactFile.exists())
        {
            logger.debug("Resolved " + artifactFile.getCanonicalPath() + "!");

            FileInputStream fis = new FileInputStream(artifactFile);

            if (offset > 0)
            {
                long skip = fis.skip(offset);

                logger.debug("Skipped " + skip + " bytes!");
            }

            return fis;
        }

        return null;
    }

    @Override
    public OutputStream getOutputStream(String storageId,
                                        String repositoryId,
                                        String artifactPath)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        ArtifactFile artifactFile;
        if (!ArtifactUtils.isMetadata(artifactPath) && !ArtifactUtils.isChecksum(artifactPath))
        {
            Artifact artifact = ArtifactUtils.convertPathToArtifact(artifactPath);
            artifactFile = new ArtifactFile(repository, artifact, true);
        }
        else
        {
            final File repoPath = new File(storage.getRepository(repositoryId).getBasedir());
            artifactFile = new ArtifactFile(new File(repoPath, artifactPath).getCanonicalFile());
        }

        artifactFile.createParents();

        return new ArtifactFileOutputStream(artifactFile);
    }

    @Override
    public void delete(String storageId,
                       String repositoryId,
                       String path,
                       boolean force)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        final File repoPath = new File(repository.getBasedir());
        final File artifactFile = new File(repoPath, path).getCanonicalFile();
        final File basedirTrash = repository.getTrashDir();

        logger.debug("Checking in " + storage.getId() + ":" + repository.getId() + "(" + artifactFile.getCanonicalPath() + ")...");

        if (artifactFile.exists())
        {
            if (!artifactFile.isDirectory())
            {
                if ((repository.isTrashEnabled() && !force) || (force && !repository.allowsForceDeletion()))
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
                if ((repository.isTrashEnabled() && !force) || (force && !repository.allowsForceDeletion()))
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
    public void deleteTrash(String storageId, String repositoryId)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        logger.debug("Emptying trash for repositoryId " + repository.getId() + "...");

        final File basedirTrash = repository.getTrashDir();

        FileUtils.deleteDirectory(basedirTrash);

        //noinspection ResultOfMethodCallIgnored
        basedirTrash.mkdirs();
    }

    @Override
    public void deleteTrash()
            throws IOException
    {
        for (Map.Entry entry : getConfiguration().getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            final Map<String, Repository> repositories = storage.getRepositories();
            for (Repository repository : repositories.values())
            {
                if (repository.allowsDeletion())
                {
                    logger.debug("Emptying trash for repository " + repository.getId() + "...");

                    final File basedirTrash = repository.getTrashDir();

                    FileUtils.deleteDirectory(basedirTrash);

                    //noinspection ResultOfMethodCallIgnored
                    basedirTrash.mkdirs();
                }
                else
                {
                    logger.warn("Repository " + repository.getId() + " does not support removal of trash.");
                }
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

}
