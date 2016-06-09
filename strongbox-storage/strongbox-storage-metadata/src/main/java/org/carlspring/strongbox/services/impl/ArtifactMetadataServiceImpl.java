package org.carlspring.strongbox.services.impl;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.artifact.locator.handlers.ArtifactLocationGenerateMetadataOperation;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataManager;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author stodorov
 * @author mtodorov
 */
@Component
public class ArtifactMetadataServiceImpl
        implements ArtifactMetadataService
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactMetadataServiceImpl.class);

    @Autowired
    private ConfigurationManager configurationManager;

    @Autowired
    private MetadataManager metadataManager;


    public ArtifactMetadataServiceImpl()
    {
    }

    @Override
    public Metadata getMetadata(String storageId,
                                String repositoryId,
                                String artifactPath)
            throws IOException,
                   XmlPullParserException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        Path artifactBasePath = Paths.get(repository.getBasedir(), artifactPath);

        return metadataManager.readMetadata(artifactBasePath);
    }

    @Override
    public Metadata getMetadata(String artifactBasePath)
            throws IOException,
                   XmlPullParserException
    {
        return metadataManager.readMetadata(Paths.get(artifactBasePath));
    }

    @Override
    public Metadata getMetadata(InputStream is)
            throws IOException, XmlPullParserException
    {
        return metadataManager.readMetadata(is);
    }

    public void rebuildMetadata(String storageId, String repositoryId, String basePath)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        ArtifactLocationGenerateMetadataOperation operation = new ArtifactLocationGenerateMetadataOperation(metadataManager);
        operation.setStorage(storage);
        operation.setRepository(repository);
        operation.setBasePath(basePath);

        ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
        locator.setOperation(operation);
        locator.locateArtifactDirectories();
    }

    @Override
    public void mergeMetadata(String storageId,
                              String repositoryId,
                              Artifact artifact,
                              Metadata mergeMetadata)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);

        metadataManager.mergeMetadata(repository, artifact, mergeMetadata);
    }

    @Override
    public void addVersion(String storageId,
                           String repositoryId,
                           String artifactPath,
                           String version,
                           MetadataType metadataType)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        Path artifactBasePath = Paths.get(repository.getBasedir(), artifactPath);

        Metadata metadata = getMetadata(artifactBasePath.toAbsolutePath().toString());

        addVersion(metadata, version);

        metadataManager.storeMetadata(artifactBasePath, version, metadata, metadataType);
    }

    @Override
    public void addVersion(Metadata metadata, String version)
    {
        if (!metadata.getVersioning().getVersions().contains(version))
        {
            metadata.getVersioning().getVersions().add(version);

            // Update the latest field
            MetadataHelper.setLatest(metadata, version);
            // Update the release field
            MetadataHelper.setRelease(metadata, version);
            // Update the lastUpdated field
            MetadataHelper.setLastUpdated(metadata.getVersioning());
        }
        else
        {
            // No need to throw an exception here.
            // Logging the error should suffice.
            logger.error("Version " + version + " already exists in the metadata file.");
        }
    }

    @Override
    public void addTimestampedSnapshotVersion(String storageId,
                                              String repositoryId,
                                              String artifactPath,
                                              String version,
                                              String classifier,
                                              String extension)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        String snapshot = ArtifactUtils.getSnapshotBaseVersion(version);

        Path artifactBasePath = Paths.get(repository.getBasedir(), artifactPath);

        Artifact artifact = ArtifactUtils.convertPathToArtifact(artifactPath);

        Metadata snapshotMetadata = metadataManager.generateSnapshotVersioningMetadata(artifactBasePath,
                                                                                       artifact,
                                                                                       snapshot,
                                                                                       false);

        addTimestampedSnapshotVersion(snapshotMetadata, version, classifier, extension);

        metadataManager.storeMetadata(artifactBasePath,
                                      snapshot,
                                      snapshotMetadata,
                                      MetadataType.SNAPSHOT_VERSION_LEVEL);
    }

    @Override
    public void addTimestampedSnapshotVersion(Metadata metadata,
                                              String version,
                                              String classifier,
                                              String extension)
    {
        List<SnapshotVersion> snapshotVersions = metadata.getVersioning().getSnapshotVersions();

        SnapshotVersion snapshotVersion = MetadataHelper.createSnapshotVersion(metadata.getGroupId(),
                                                                               metadata.getArtifactId(),
                                                                               version,
                                                                               classifier,
                                                                               extension);

        snapshotVersions.add(snapshotVersion);

        // Set the snapshot mapping fields (timestamp + buildNumber)
        MetadataHelper.setupSnapshotVersioning(metadata.getVersioning());

        // Update the lastUpdated field
        MetadataHelper.setLastUpdated(metadata.getVersioning());
    }

    @Override
    public void removeVersion(String storageId,
                              String repositoryId,
                              String artifactPath,
                              String version,
                              MetadataType metadataType)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        Path artifactBasePath = Paths.get(repository.getBasedir(), artifactPath);

        Metadata metadata = getMetadata(artifactBasePath.toAbsolutePath().toString());
        Versioning versioning = metadata.getVersioning();

        if (ArtifactUtils.isSnapshot(version))
        {
            Path snapshotBasePath = Paths.get(artifactBasePath + "/" + ArtifactUtils.getSnapshotBaseVersion(version));

            Artifact artifact = ArtifactUtils.convertPathToArtifact(artifactPath);

            metadataManager.generateSnapshotVersioningMetadata(snapshotBasePath, artifact, version, true);
        }

        // Update the latest field
        MetadataHelper.setLatest(metadata, version);
        // Update the release field
        MetadataHelper.setRelease(metadata, version);
        // Update the lastUpdated field
        MetadataHelper.setLastUpdated(metadata.getVersioning());

        // Remove the version
        versioning.removeVersion(version);

        metadataManager.storeMetadata(artifactBasePath, version, metadata, metadataType);
    }

    @Override
    public void removeTimestampedSnapshotVersion(String storageId,
                                                 String repositoryId,
                                                 String artifactPath,
                                                 String version,
                                                 String classifier)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        String snapshot = ArtifactUtils.getSnapshotBaseVersion(version);

        Path artifactBasePath = Paths.get(repository.getBasedir(), artifactPath);

        Artifact artifact = ArtifactUtils.convertPathToArtifact(artifactPath);

        Metadata snapshotMetadata = metadataManager.generateSnapshotVersioningMetadata(artifactBasePath,
                                                                                       artifact,
                                                                                       snapshot,
                                                                                       false);

        List<SnapshotVersion> snapshotVersions = snapshotMetadata.getVersioning().getSnapshotVersions();
        for (Iterator<SnapshotVersion> iterator = snapshotVersions.iterator(); iterator.hasNext();)
        {
            SnapshotVersion snapshotVersion = iterator.next();
            if (snapshotVersion.getVersion().equals(version) &&
                (classifier == null || snapshotVersion.getClassifier().equals(classifier)))
            {
                iterator.remove();

                logger.debug("Removed timestamped SNAPSHOT (" + version +
                             (classifier != null ? ":" + classifier :
                             (snapshotVersion.getClassifier() != null && !snapshotVersion.getClassifier().equals("") ?
                              ":" + snapshotVersion.getClassifier() + ":" : ":") +
                             snapshotVersion.getExtension()) + ") from metadata.");
            }
        }

        // Set the snapshot mapping fields (timestamp + buildNumber)
        MetadataHelper.setupSnapshotVersioning(snapshotMetadata.getVersioning());

        // Update the lastUpdated field
        MetadataHelper.setLastUpdated(snapshotMetadata.getVersioning());

        metadataManager.storeMetadata(artifactBasePath,
                                      snapshot,
                                      snapshotMetadata,
                                      MetadataType.SNAPSHOT_VERSION_LEVEL);
    }

    @Override
    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
