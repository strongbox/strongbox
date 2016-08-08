package org.carlspring.strongbox.providers.layout;

import org.carlspring.commons.io.filters.DirectoryFilter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.io.ArtifactFile;
import org.carlspring.strongbox.io.ArtifactFileOutputStream;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.providers.storage.StorageProvider;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.util.DirUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static org.carlspring.commons.io.FileUtils.moveDirectory;

/**
 * @author carlspring
 */
@Component("maven2LayoutProvider")
public class Maven2LayoutProvider extends AbstractLayoutProvider
{

    private static final Logger logger = LoggerFactory.getLogger(Maven2LayoutProvider.class);

    public static final String ALIAS = "Maven 2";

    @Autowired
    private LayoutProviderRegistry layoutProviderRegistry;

    @Autowired
    private StorageProviderRegistry storageProviderRegistry;

    /*
    @Autowired
    private MetadataManager metadataManager;
    */


    @PostConstruct
    @Override
    public void register()
    {
        layoutProviderRegistry.addProvider(ALIAS, this);

        logger.info("Registered layout provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public ArtifactInputStream getInputStream(String storageId,
                                              String repositoryId,
                                              String path)
            throws IOException, NoSuchAlgorithmException
    {
        Storage storage = getConfiguration().getStorage(storageId);

        logger.debug("Checking in " + storage.getId() + ":" + repositoryId + "...");

        Repository repository = storage.getRepository(repositoryId);
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

        final File repoPath = storageProvider.getFileImplementation(storage.getRepository(repositoryId).getBasedir());
        final File artifactFile = storageProvider.getFileImplementation(repoPath.getPath(), path).getCanonicalFile();

        logger.debug(" -> Checking for " + artifactFile.getCanonicalPath() + "...");

        if (artifactFile.exists())
        {
            logger.debug("Resolved " + artifactFile.getCanonicalPath() + "!");

            ArtifactInputStream ais = storageProvider.getInputStreamImplementation(artifactFile.getAbsolutePath());
            ais.setLength(artifactFile.length());

            return ais;
        }

        return null;
    }

    @Override
    public OutputStream getOutputStream(String storageId,
                                        String repositoryId,
                                        String path)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

        ArtifactFile artifactFile;
        if (!ArtifactUtils.isMetadata(path) && !ArtifactUtils.isChecksum(path))
        {
            Artifact artifact = ArtifactUtils.convertPathToArtifact(path);
            artifactFile = new ArtifactFile(repository, artifact, true);
        }
        else
        {
            final File repoPath = storageProvider.getFileImplementation(storage.getRepository(repositoryId).getBasedir());
            artifactFile = new ArtifactFile(storageProvider.getFileImplementation(repoPath.getPath(), path).getCanonicalFile());
        }

        artifactFile.createParents();

        return new ArtifactFileOutputStream(artifactFile);
    }

    @Override
    public void copy(String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId,
                     String path)
            throws IOException
    {
        // TODO: Implement
    }

    @Override
    public void move(String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId,
                     String path)
            throws IOException
    {
        // TODO: Implement
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
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

        final File repoPath = storageProvider.getFileImplementation(storage.getRepository(repositoryId).getBasedir());
        final File artifactFile = storageProvider.getFileImplementation(repoPath.getPath(), path).getCanonicalFile();
        final File basedirTrash = repository.getTrashDir();

        logger.debug("Checking in " + storage.getId() + ":" + repository.getId() + "(" + artifactFile.getCanonicalPath() + ")...");

        if (artifactFile.exists())
        {
            if (!artifactFile.isDirectory())
            {
                if ((repository.isTrashEnabled() && !force) || (force && !repository.allowsForceDeletion()))
                {
                    File trashFile = storageProvider.getFileImplementation(basedirTrash.getPath(), path).getCanonicalFile();
                    FileUtils.moveFile(artifactFile, trashFile);

                    logger.debug("Moved /" + repositoryId + "/" + path + " to trash (" + trashFile.getAbsolutePath() + ").");

                    // Move the checksums to the trash as well
                    moveChecksumsToTrash(repository, path, artifactFile, basedirTrash);
                }
                else
                {
                    //noinspection ResultOfMethodCallIgnored
                    artifactFile.delete();
                    deleteChecksums(repository, path, artifactFile);
                }
            }
            else
            {
                if ((repository.isTrashEnabled() && !force) || (force && !repository.allowsForceDeletion()))
                {
                    File trashFile = storageProvider.getFileImplementation(basedirTrash.getPath(), path).getCanonicalFile();

                    moveDirectory(artifactFile.toPath(), trashFile.toPath());

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

    private void moveChecksumsToTrash(Repository repository,
                                      String path,
                                      File artifactFile,
                                      File basedirTrash)
            throws IOException
    {
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

        File md5ChecksumFile = storageProvider.getFileImplementation(artifactFile.getAbsolutePath() + ".md5");
        if (md5ChecksumFile.exists())
        {
            File md5TrashFile = storageProvider.getFileImplementation(basedirTrash.getPath(), path + ".md5").getCanonicalFile();
            FileUtils.moveFile(md5ChecksumFile, md5TrashFile);

            logger.debug("Moved /" + repository.getId() + "/" + path + ".md5" + " to trash (" + md5TrashFile.getAbsolutePath() + ").");
        }

        File sha1ChecksumFile = storageProvider.getFileImplementation(artifactFile.getAbsolutePath() + ".sha1");
        if (sha1ChecksumFile.exists())
        {
            File sha1TrashFile = storageProvider.getFileImplementation(basedirTrash.getPath(), path + ".sha1").getCanonicalFile();
            FileUtils.moveFile(sha1ChecksumFile, sha1TrashFile);

            logger.debug("Moved /" + repository.getId() + "/" + path + ".sha1" + " to trash (" + sha1TrashFile.getAbsolutePath() + ").");
        }
    }

    private void deleteChecksums(Repository repository,
                                 String path,
                                 File artifactFile)
            throws IOException
    {
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

        File md5ChecksumFile = storageProvider.getFileImplementation(artifactFile.getAbsolutePath() + ".md5");
        if (md5ChecksumFile.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            md5ChecksumFile.delete();

            logger.debug("Deleted /" + repository.getId() + "/" + path + ".md5.");
        }

        File sha1ChecksumFile = storageProvider.getFileImplementation(artifactFile.getAbsolutePath() + ".sha1");
        if (sha1ChecksumFile.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            sha1ChecksumFile.delete();

            logger.debug("Deleted /" + repository.getId() + "/" + path + ".sha1.");
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
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

        File md5ChecksumFile = storageProvider.getFileImplementation(artifactFile.getAbsolutePath() + ".md5");
        if (md5ChecksumFile.exists())
        {
            File md5RestoredFile = storageProvider.getFileImplementation(repository.getBasedir(), path + ".md5").getCanonicalFile();
            FileUtils.moveFile(md5ChecksumFile, md5RestoredFile);

            logger.debug("Restored /" + repositoryId + "/" + path + ".md5" + " from trash (" + md5ChecksumFile.getAbsolutePath() + ").");
        }

        File sha1ChecksumFile = storageProvider.getFileImplementation(artifactFile.getAbsolutePath() + ".sha1");
        if (sha1ChecksumFile.exists())
        {
            File sha1RestoredFile = storageProvider.getFileImplementation(repository.getBasedir(), path + ".sha1").getCanonicalFile();
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
    public void undelete(String storageId, String repositoryId, String path)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

        final File repoPath = storageProvider.getFileImplementation(repository.getBasedir());
        final File artifactFile = storageProvider.getFileImplementation(repoPath.getPath(), path).getCanonicalFile();
        final File artifactFileTrash = storageProvider.getFileImplementation(repository.getTrashDir().getPath(), path);

        logger.debug("Attempting to restore " + artifactFileTrash.getCanonicalPath() +
                     " (from " + storage.getId() + ":" + repository.getId() + ")...");

        if (artifactFileTrash.exists())
        {
            if (!artifactFileTrash.isDirectory())
            {
                if (repository.isTrashEnabled())
                {
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
                    // File trashFile = storageProvider.getFileImplementation(basedirTrash, path).getCanonicalFile();
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
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

        logger.debug("Restoring all artifacts from the trash of " + storageId + ":" + repository.getId() + "...");

        if (repository.isTrashEnabled())
        {
            final File basedirTrash = repository.getTrashDir();
            final File basedirRepository = storageProvider.getFileImplementation(repository.getBasedir());

            for (File dir : basedirTrash.listFiles(new DirectoryFilter()))
            {
                logger.debug("Restoring " + dir.getAbsolutePath() + " to " + basedirRepository);

                File srcDir = storageProvider.getFileImplementation(dir.getAbsolutePath());

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
    public void undeleteTrash()
            throws IOException
    {
        for (Map.Entry entry : getConfiguration().getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            final Map<String, Repository> repositories = storage.getRepositories();
            for (Repository repository : repositories.values())
            {
                undeleteTrash(storage.getId(), repository.getId());
            }
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
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

        final File repoPath = storageProvider.getFileImplementation(repository.getBasedir());

        try
        {
            File artifactFile = storageProvider.getFileImplementation(repoPath.getPath(), metadataPath).getCanonicalFile();
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
    public boolean contains(String storageId, String repositoryId, String path)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

        final File repoPath = storageProvider.getFileImplementation(storage.getRepository(repositoryId).getBasedir());
        final File artifactFile = storageProvider.getFileImplementation(repoPath.getPath(), path).getCanonicalFile();

        return artifactFile.exists();
    }

    @Override
    public boolean containsArtifact(Repository repository, Artifact artifact)
            throws IOException
    {
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

        final String artifactPath = ArtifactUtils.convertArtifactToPath(artifact);

        final File repositoryBasedir = storageProvider.getFileImplementation(repository.getStorage().getBasedir(), repository.getId());
        final File artifactFile = storageProvider.getFileImplementation(repositoryBasedir.getPath(), artifactPath).getAbsoluteFile();

        return artifactFile.exists();
    }

    @Override
    public boolean containsPath(Repository repository, String path)
            throws IOException
    {
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

        final File repositoryBasedir = storageProvider.getFileImplementation(repository.getStorage().getBasedir(), repository.getId());
        final File artifactFile = storageProvider.getFileImplementation(repositoryBasedir.getPath(), path).getAbsoluteFile();

        return artifactFile.exists();
    }

    @Override
    public String getPathToArtifact(Repository repository, Artifact artifact)
            throws IOException
    {
        StorageProvider storageProvider = storageProviderRegistry.getProvider(repository.getImplementation());

        final String artifactPath = ArtifactUtils.convertArtifactToPath(artifact);

        final File repositoryBasedir = storageProvider.getFileImplementation(repository.getStorage().getBasedir(), repository.getId());
        final File artifactFile = storageProvider.getFileImplementation(repositoryBasedir.getPath(), artifactPath);

        return artifactFile.getAbsolutePath();
    }

    /*
    public MetadataManager getMetadataManager()
    {
        return metadataManager;
    }
    */

}
