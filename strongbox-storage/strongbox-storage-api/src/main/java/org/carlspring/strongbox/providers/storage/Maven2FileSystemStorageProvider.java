package org.carlspring.strongbox.providers.storage;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.carlspring.commons.io.filters.DirectoryFilter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.util.DirUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.spi.FileSystemProvider;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author carlspring
 */
@Component
public class Maven2FileSystemStorageProvider extends AbstractStorageProvider
{

    private static final Logger logger = LoggerFactory.getLogger(FileSystemProvider.class);

    private static final String ALIAS = "file-system";

    @Autowired
    private StorageProviderRegistry storageProviderRegistry;

    // TODO: Uncomment
    /*
    @Autowired
    private MetadataManager metadataManager;
    */


    @PostConstruct
    @Override
    public void register()
    {
        storageProviderRegistry.addProviderImplementation(getAlias(), Maven2LayoutProvider.ALIAS, this);

        logger.info("Registered storage provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public String getImplementation()
    {
        return Maven2LayoutProvider.ALIAS;
    }

    @Override
    public ArtifactInputStream getInputStream(String storageId, String repositoryId, String path)
            throws IOException, NoSuchAlgorithmException, ArtifactTransportException
    {
        return null;
    }

    @Override
    public OutputStream getOutputStream(String storageId,
                                        String repositoryId,
                                        String path)
            throws IOException
    {
        return null;
    }

    @Override
    public void copy(String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId,
                     String path)
            throws IOException
    {

    }

    @Override
    public void move(String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId,
                     String path)
            throws IOException
    {

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
                    // FileUtils.moveDirectory(artifactFile, trashFile);
                    org.carlspring.commons.io.FileUtils.moveDirectory(artifactFile.toPath(), trashFile.toPath());

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

            logger.debug(
                    "Moved /" + repositoryId + "/" + path + ".sha1" + " to trash (" + sha1TrashFile.getAbsolutePath() +
                    ").");
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

    private void restoreChecksumsFromTrash(String storageId,
                                           String repositoryId,
                                           String path,
                                           File artifactFile)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        File md5ChecksumFile = new File(artifactFile.getAbsolutePath() + ".md5");
        if (md5ChecksumFile.exists())
        {
            File md5RestoredFile = new File(repository.getBasedir(), path + ".md5").getCanonicalFile();
            FileUtils.moveFile(md5ChecksumFile, md5RestoredFile);

            logger.debug("Restored /" + repositoryId + "/" + path + ".md5" + " from trash (" + md5ChecksumFile.getAbsolutePath() + ").");
        }

        File sha1ChecksumFile = new File(artifactFile.getAbsolutePath() + ".sha1");
        if (sha1ChecksumFile.exists())
        {
            File sha1RestoredFile = new File(repository.getBasedir(), path + ".sha1").getCanonicalFile();
            FileUtils.moveFile(sha1ChecksumFile, sha1RestoredFile);

            logger.debug("Restored /" + repositoryId + "/" + path + ".sha1" + " from trash (" + sha1ChecksumFile.getAbsolutePath() + ").");
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
    public void undelete(String storageId, String repositoryId, String path)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        final File repoPath = new File(repository.getBasedir());
        final File artifactFile = new File(repoPath, path).getCanonicalFile();
        final File artifactFileTrash = new File(repository.getTrashDir(), path);

        logger.debug("Attempting to restore " + artifactFileTrash.getCanonicalPath() + " (from " + storage.getId() + ":" + repository.getId() + ")...");

        if (artifactFileTrash.exists())
        {
            if (!artifactFileTrash.isDirectory())
            {
                if (repository.isTrashEnabled())
                {
                    // File trashFile = new File(basedirTrash, path).getCanonicalFile();
                    FileUtils.moveFile(artifactFileTrash, artifactFile);

                    logger.debug("Restored /" + storageId + "/" + repositoryId + "/" + path +
                                 " from trash (" + artifactFileTrash.getAbsolutePath() + ").");

                    // Move the checksums to the trash as well
                    restoreChecksumsFromTrash(storageId, repositoryId, path, artifactFileTrash);

                    DirUtils.removeEmptyAncestors(artifactFileTrash.getParentFile().getAbsolutePath(), ".trash");
                }
                else
                {
                    // TODO: Display a message that undeleting is not supported for this repository.
                }
            }
            else
            {
                if (repository.isTrashEnabled())
                {
                    // File trashFile = new File(basedirTrash, path).getCanonicalFile();
                    FileUtils.moveDirectory(artifactFileTrash, artifactFile);
                    DirUtils.removeEmptyAncestors(artifactFileTrash.getAbsolutePath(), ".trash");

                    logger.debug("Moved /" + repositoryId + "/" + path + " to trash (" + artifactFileTrash.getAbsolutePath() + ").");
                }
                else
                {
                    // TODO: Display a message that undeleting is not supported for this repository.
                }
            }

            logger.debug("Restored /" + repositoryId + "/" + path);
        }
    }

    @Override
    public void undeleteTrash(String storageId, String repositoryId)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        logger.debug("Restoring all artifacts from the trash of " + storageId + ":" + repository.getId() + "...");

        if (repository.isTrashEnabled())
        {
            final File basedirTrash = repository.getTrashDir();
            final File basedirRepository = new File(repository.getBasedir());

            for (File dir : basedirTrash.listFiles(new DirectoryFilter()))
            {
                logger.debug("Restoring " + dir.getAbsolutePath() + " to " + basedirRepository);

                File srcDir = new File(dir.getAbsolutePath());

                // Because moving files has to be something so fucking stupidly implemented in Java.
                FileUtils.copyDirectoryToDirectory(srcDir, basedirRepository);
                FileUtils.deleteDirectory(srcDir);
            }
        }
        else
        {
            logger.warn("Repository " + repository.getId() + " does not support removal of trash.");
        }
    }

    @Override
    public void deleteMetadata(String storageId,
                               String repositoryId,
                               String metadataPath)
            throws IOException
    {
        // TODO: Further untangle the relationships of this so that the code below can be uncommented:

        /*
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        final File repoPath = new File(repository.getBasedir());

        try
        {
            File artifactFile = new File(repoPath, metadataPath).getCanonicalFile();
            if (!artifactFile.isFile())
            {
                String version = artifactFile.getPath().substring(artifactFile.getPath().lastIndexOf(File.separatorChar) + 1);
                java.nio.file.Path path = Paths.get(artifactFile.getPath().substring(0, artifactFile.getPath().lastIndexOf(File.separatorChar)));

                Metadata metadata = getMetadataManager().readMetadata(path);
                if (metadata != null && metadata.getVersioning() != null
                    && metadata.getVersioning().getVersions().contains(version))
                {
                    metadata.getVersioning().getVersions().remove(version);
                    getMetadataManager().storeMetadata(path, null, metadata, MetadataType.ARTIFACT_ROOT_LEVEL);
                }
            }
        }
        catch (IOException | XmlPullParserException | NoSuchAlgorithmException e)
        {
            // We won't do anything in this case because it doesn't have an impact to the deletion
        }
        */
    }

    @Override
    public boolean containsArtifact(Repository repository, Artifact artifact)
    {
        if (!repository.getType().equals(RepositoryTypeEnum.GROUP.getType()))
        {
            final String artifactPath = ArtifactUtils.convertArtifactToPath(artifact);

            final File repositoryBasedir = new File(repository.getStorage().getBasedir(), repository.getId());
            final File artifactFile = new File(repositoryBasedir, artifactPath).getAbsoluteFile();

            return artifactFile.exists();
        }
        else if (repository.getType().equals(RepositoryTypeEnum.GROUP.getType()))
        {
            for (String storageAndRepositoryId : repository.getGroupRepositories())
            {
                String[] storageAndRepositoryIdTokens = storageAndRepositoryId.split(":");
                String storageId = storageAndRepositoryIdTokens.length == 2 ?
                                   storageAndRepositoryIdTokens[0] :
                                   repository.getStorage().getId();
                String repositoryId = storageAndRepositoryIdTokens[storageAndRepositoryIdTokens.length < 2 ? 0 : 1];

                Repository r = getConfiguration().getStorage(storageId).getRepository(repositoryId);

                if (containsArtifact(r, artifact))
                {
                    return true;
                }
            }

            return false;
        }

        return false;
    }

    @Override
    public boolean containsPath(Repository repository, String path)
    {
        if (!repository.getType().equals(RepositoryTypeEnum.GROUP.getType()))
        {
            final File repositoryBasedir = new File(repository.getStorage().getBasedir(), repository.getId());
            final File artifactFile = new File(repositoryBasedir, path).getAbsoluteFile();

            return artifactFile.exists();
        }
        else if (repository.getType().equals(RepositoryTypeEnum.GROUP.getType()))
        {
            for (String storageAndRepositoryId : repository.getGroupRepositories())
            {
                String[] storageAndRepositoryIdTokens = storageAndRepositoryId.split(":");
                String storageId = storageAndRepositoryIdTokens.length == 2 ?
                                   storageAndRepositoryIdTokens[0] :
                                   repository.getStorage().getId();
                String repositoryId = storageAndRepositoryIdTokens[storageAndRepositoryIdTokens.length < 2 ? 0 : 1];

                Repository r = getConfiguration().getStorage(storageId).getRepository(repositoryId);

                if (containsPath(r, path))
                {
                    return true;
                }
            }

            return false;
        }

        return false;
    }

    @Override
    public String getPathToArtifact(Repository repository, Artifact artifact)
    {
        final String artifactPath = ArtifactUtils.convertArtifactToPath(artifact);

        final File repositoryBasedir = new File(repository.getStorage().getBasedir(), repository.getId());
        final File artifactFile = new File(repositoryBasedir, artifactPath);

        return artifactFile.getAbsolutePath();
    }

}
