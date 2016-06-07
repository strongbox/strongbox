package org.carlspring.strongbox.storage.resolvers;

import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.commons.io.filters.DirectoryFilter;
import org.carlspring.commons.io.resource.ResourceCloser;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.client.MavenArtifactClient;
import org.carlspring.strongbox.io.ArtifactFile;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.RemoteRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.util.DirUtils;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("proxyLocationResolver")
public class ProxyLocationResolver
        extends AbstractLocationResolver
{

    private static final Logger logger = LoggerFactory.getLogger(ProxyLocationResolver.class);

    private String alias = "proxy";


    public ProxyLocationResolver()
    {
    }

    @Override
    public ArtifactInputStream getInputStream(String storageId,
                                              String repositoryId,
                                              String artifactPath)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException
    {
        Storage storage = getConfiguration().getStorage(storageId);

        logger.debug("Checking in " + storage.getId() + ":" + repositoryId + "...");

        Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);

        Artifact artifact = ArtifactUtils.convertPathToArtifact(artifactPath);
        ArtifactFile artifactFile = new ArtifactFile(repository, artifact, true);

        logger.debug(" -> Checking for " + artifactFile.getCanonicalPath() + "...");

        if (artifactFile.exists())
        {
            logger.debug("The artifact was found in the local cache.");
            logger.debug("Resolved " + artifactFile.getCanonicalPath() + "!");

            ArtifactInputStream ais = new ArtifactInputStream(new FileInputStream(artifactFile));
            ais.setLength(artifactFile.length());

            return ais;
        }
        else
        {
            logger.debug("The artifact was not found in the local cache.");

            RemoteRepository remoteRepository = repository.getRemoteRepository();

            MavenArtifactClient client = new MavenArtifactClient();
            client.setRepositoryBaseUrl(remoteRepository.getUrl());
            client.setUsername(remoteRepository.getUsername());
            client.setPassword(remoteRepository.getPassword());

            Response response = client.getResourceWithResponse(artifactPath);
            if (response.getStatus() != 200 || response.getEntity() == null)
            {
                return null;
            }

            System.out.println("Creating " + artifactFile.getTemporaryFile().getParentFile().getAbsolutePath());

            artifactFile.createParents();

            InputStream remoteIs = response.readEntity(InputStream.class);
            FileOutputStream fos = new FileOutputStream(artifactFile.getTemporaryFile());
            MultipleDigestOutputStream mdos = new MultipleDigestOutputStream(fos);

            // 1) Attempt to resolve it from the remote host
            if (remoteIs == null)
            {
                // 1 a) If the artifact does not exist, return null
                // The remote failed to resolve the artifact as well.
                return null;
            }

            int len;
            final int size = 1024;
            byte[] bytes = new byte[size];

            while ((len = remoteIs.read(bytes, 0, size)) != -1)
            {
                mdos.write(bytes, 0, len);
            }

            fos.flush();

            ResourceCloser.close(fos, logger);
            ResourceCloser.close(remoteIs, logger);
            ResourceCloser.close(client, logger);

            artifactFile.moveTempFileToOriginalDestination();

            // TODO: Add a policy for validating the checksums of downloaded artifacts
            // TODO: Validate the local checksum against the remote's checksums

            // 1 b) If it exists on the remote, serve the downloaded artifact

            ArtifactInputStream ais = new ArtifactInputStream(new FileInputStream(artifactFile));
            ais.setLength(artifactFile.length());

            return ais;
        }
    }

    @Override
    public OutputStream getOutputStream(String storageId,
                                        String repositoryId,
                                        String artifactPath)
            throws IOException
    {
        // It should not be possible to write artifacts to a proxy repository.
        // A proxy repository should only serve artifacts that already exist
        // in the cache, or the remote host.

        return null;
    }

    @Override
    public boolean contains(String storageId, String repositoryId, String path)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);

        final File repoPath = new File(storage.getRepository(repositoryId).getBasedir());
        final File artifactFile = new File(repoPath, path).getCanonicalFile();

        return artifactFile.exists();
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
    public void initialize()
            throws IOException
    {
        logger.debug("Initialized ProxyLocationResolver.");
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
