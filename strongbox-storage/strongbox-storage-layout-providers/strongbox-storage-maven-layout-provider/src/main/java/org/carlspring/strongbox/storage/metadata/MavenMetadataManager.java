package org.carlspring.strongbox.storage.metadata;

import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.storage.metadata.comparators.SnapshotVersionComparator;
import org.carlspring.strongbox.storage.metadata.comparators.VersionComparator;
import org.carlspring.strongbox.storage.metadata.versions.MetadataVersion;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.UnknownRepositoryTypeException;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.google.common.base.Throwables;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.providers.layout.LayoutProviderRegistry.getLayoutProvider;

/**
 * @author Martin Todorov
 * @author Steve Todorov
 * @author Sergey Bespalov
 * @author Ekaterina Novik
 */
@Component
public class MavenMetadataManager
{

    private static final Logger logger = LoggerFactory.getLogger(MavenMetadataManager.class);

    @Inject
    protected StorageProviderRegistry storageProviderRegistry;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    private ConcurrentHashMap<String, String> metadataSynchronizationContainer = new ConcurrentHashMap<>();

    public MavenMetadataManager()
    {
    }

    public Metadata readMetadata(Repository repository,
                                 Artifact artifact)
            throws IOException,
                   XmlPullParserException,
                   ProviderImplementationException
    {
        Metadata metadata;

        LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);
        ArtifactCoordinates coordinates = layoutProvider.getArtifactCoordinates(
                ArtifactUtils.convertArtifactToPath(artifact));

        if (layoutProvider.containsArtifact(repository, coordinates))
        {
            Path artifactPath = layoutProvider.resolve(repository, coordinates);
            Path artifactBasePath = artifactPath;
            if (artifact.getVersion() != null)
            {
                artifactBasePath = artifactPath.getParent().getParent();
            }

            logger.debug("Getting metadata for " + artifactBasePath);

            metadata = readMetadata(artifactBasePath);
        }
        else
        {
            throw new IOException("Artifact " + artifact.toString() + " does not exist in " +
                                  repository.getStorage().getBasedir() + "/" + repository.getBasedir() + " !");
        }

        return metadata;
    }

    public Metadata readMetadata(Path artifactBasePath)
            throws IOException, XmlPullParserException
    {
        Path metadataPath = MetadataHelper.getMetadataPath(artifactBasePath);
        Metadata metadata;

        try (InputStream is = Files.newInputStream(metadataPath))
        {
            metadata = readMetadata(is);
        }

        return metadata;
    }

    public Metadata readMetadata(InputStream is)
            throws IOException, XmlPullParserException
    {
        Metadata metadata;

        try
        {
            MetadataXpp3Reader reader = new MetadataXpp3Reader();

            metadata = reader.read(is);
        }
        finally
        {
            ResourceCloser.close(is, logger);
        }

        return metadata;
    }

    public void storeMetadata(final Path metadataBasePath,
                              final String version,
                              final Metadata metadata,
                              final MetadataType metadataType)
    {

        doInLock(metadataBasePath, path ->
                 {
                     try
                     {
                         Path metadataPath = MetadataHelper.getMetadataPath(metadataBasePath, version, metadataType);
                         Files.deleteIfExists(metadataPath);

                         try (OutputStream os = new MultipleDigestOutputStream(metadataPath,
                                                                               Files.newOutputStream(metadataPath));
                              Writer writer = WriterFactory.newXmlWriter(os))
                         {

                             MetadataXpp3Writer mappingWriter = new MetadataXpp3Writer();
                             mappingWriter.write(writer, metadata);

                             os.flush();
                         }
                     }
                     catch (Exception ex)
                     {
                         throw Throwables.propagate(ex);
                     }
                 }
        );
    }

    /**
     * Generate a metadata file for an artifact.
     */
    public void generateMetadata(Repository repository,
                                 String path,
                                 VersionCollectionRequest request)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException, ProviderImplementationException,
                   UnknownRepositoryTypeException
    {
        LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);
        if (layoutProvider.containsPath(repository, path))
        {
            logger.debug("Artifact metadata generation triggered for " + path +
                         " in '" + repository.getStorage().getId() + ":" + repository.getId() + "'" +
                         " [policy: " + repository.getPolicy() + "].");

            Artifact artifact = ArtifactUtils.convertPathToArtifact(path);

            Metadata metadata = new Metadata();
            metadata.setArtifactId(artifact.getArtifactId());
            metadata.setGroupId(artifact.getGroupId());

            List<MetadataVersion> baseVersioning = request.getMetadataVersions();
            Versioning versioning = request.getVersioning();

            // Set lastUpdated tag for main maven-metadata
            MetadataHelper.setLastUpdated(versioning);

            /**
             * In a release repository we only need to generate maven-metadata.xml in the artifactBasePath
             * (i.e. org/foo/bar/maven-metadata.xml)
             */
            if (repository.getPolicy().equals(RepositoryPolicyEnum.RELEASE.getPolicy()))
            {
                // Don't write empty <versioning/> tags when no versions are available.
                if (!versioning.getVersions().isEmpty())
                {
                    String latestVersion = baseVersioning.get(baseVersioning.size() - 1).getVersion();

                    metadata.setVersioning(request.getVersioning());
                    versioning.setRelease(latestVersion);

                    // Set <latest> by figuring out the most recent upload
                    Collections.sort(baseVersioning);
                    versioning.setLatest(latestVersion);
                }

                // Touch the lastUpdated field
                MetadataHelper.setLastUpdated(versioning);

                // Write basic metadata
                storeMetadata(request.getArtifactBasePath(), null, metadata, MetadataType.ARTIFACT_ROOT_LEVEL);

                logger.debug("Generated Maven metadata for " +
                             artifact.getGroupId() + ":" +
                             artifact.getArtifactId() + ".");
            }
            /**
             * In a snapshot repository we need to generate maven-metadata.xml in the artifactBasePath and
             * generate additional maven-metadata.xml files for each snapshot directory containing information about
             * all available artifacts.
             */
            else if (repository.getPolicy().equals(RepositoryPolicyEnum.SNAPSHOT.getPolicy()))
            {
                // Don't write empty <versioning/> tags when no versions are available.
                if (!versioning.getVersions().isEmpty())
                {
                    // Set <latest>
                    String latestVersion = versioning.getVersions().get(versioning.getVersions().size() - 1);
                    versioning.setLatest(latestVersion);

                    metadata.setVersioning(versioning);

                    // Generate and write additional snapshot metadata.
                    for (String version : metadata.getVersioning().getVersions())
                    {
                        Path snapshotBasePath = Paths.get(request.getArtifactBasePath().toAbsolutePath() + "/" +
                                                          ArtifactUtils.getSnapshotBaseVersion(version));

                        generateSnapshotVersioningMetadata(snapshotBasePath, artifact, version, true);
                    }
                }

                // Write artifact metadata
                storeMetadata(request.getArtifactBasePath(), null, metadata, MetadataType.ARTIFACT_ROOT_LEVEL);

                logger.debug("Generated Maven metadata for " + artifact.getGroupId() + ":" +
                             artifact.getArtifactId() + ".");
            }
            else if (repository.getPolicy().equals(RepositoryPolicyEnum.MIXED.getPolicy()))
            {
                // TODO: Implement merging.
            }
            else
            {
                throw new UnknownRepositoryTypeException("Repository policy type unknown: " + repository.getId());
            }

            // If this is a plugin, we need to add an additional metadata to the groupId.artifactId path.
            if (!request.getPlugins().isEmpty())
            {
                generateMavenPluginMetadata(request, artifact);
            }
        }
        else
        {
            logger.error("Artifact metadata generation failed: " + path + ").");
        }
    }

    private void generateMavenPluginMetadata(VersionCollectionRequest request,
                                             Artifact artifact)
            throws IOException, NoSuchAlgorithmException
    {
        Metadata pluginMetadata = new Metadata();
        pluginMetadata.setPlugins(request.getPlugins());

        Path pluginMetadataPath = request.getArtifactBasePath().getParent();

        storeMetadata(pluginMetadataPath, null, pluginMetadata, MetadataType.PLUGIN_GROUP_LEVEL);

        logger.debug("Generated Maven plugin metadata for " + artifact.getGroupId() + ":" +
                     artifact.getArtifactId() + ".");
    }

    public Metadata generateSnapshotVersioningMetadata(Path snapshotBasePath,
                                                       Artifact artifact,
                                                       String version,
                                                       boolean store)
            throws IOException, NoSuchAlgorithmException
    {
        VersionCollector versionCollector = new VersionCollector();
        List<SnapshotVersion> snapshotVersions = versionCollector.collectTimestampedSnapshotVersions(snapshotBasePath);

        Versioning snapshotVersioning = versionCollector.generateSnapshotVersions(snapshotVersions);

        MetadataHelper.setupSnapshotVersioning(snapshotVersioning);

        // Last updated should be present in both cases.
        MetadataHelper.setLastUpdated(snapshotVersioning);

        // Write snapshot metadata version information for each snapshot.
        Metadata snapshotMetadata = new Metadata();
        snapshotMetadata.setGroupId(artifact.getGroupId());
        snapshotMetadata.setArtifactId(artifact.getArtifactId());
        snapshotMetadata.setVersion(version);
        snapshotMetadata.setVersioning(snapshotVersioning);

        // Set the version that this metadata represents, if any. This is used for artifact snapshots only.
        // http://maven.apache.org/ref/3.3.3/maven-repository-metadata/repository-metadata.html
        snapshotMetadata.setVersion(version);

        if (store)
        {
            storeMetadata(snapshotBasePath.getParent(), version, snapshotMetadata, MetadataType.SNAPSHOT_VERSION_LEVEL);
        }

        return snapshotMetadata;
    }

    public void mergeAndStore(final Path metadataBasePath,
                              final Metadata mergeMetadata)
    {
        doInLock(metadataBasePath, path ->
        {
            if (Files.exists(metadataBasePath))
            {
                try
                {
                    final Metadata metadata = readMetadata(metadataBasePath);
                    mergeAndStore(metadataBasePath, metadata, mergeMetadata);
                    return;
                }
                catch (Exception e)
                {
                    logger.error("Unable to merge the metadata to " + metadataBasePath + " by source metadata" +
                                 ReflectionToStringBuilder.toString(mergeMetadata) +
                                 " Continuing with storing new metadata", e);
                }
            }

            try
            {
                Files.createDirectories(metadataBasePath);
            }
            catch (IOException e)
            {
                throw Throwables.propagate(e);
            }
            storeMetadata(metadataBasePath, null, mergeMetadata, MetadataType.ARTIFACT_ROOT_LEVEL);
        });
    }

    public void mergeAndStore(Repository repository,
                              Artifact artifact,
                              Metadata mergeMetadata)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException,
                   ProviderImplementationException
    {
        LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);
        ArtifactCoordinates coordinates = layoutProvider.getArtifactCoordinates(
                ArtifactUtils.convertArtifactToPath(artifact));

        if (layoutProvider.containsArtifact(repository, coordinates))
        {
            Path artifactBasePath;
            if (artifact.getFile() != null && !artifact.getFile().isDirectory())
            {
                artifactBasePath = artifact.getFile().toPath().getParent().getParent();
            }
            else
            {
                artifactBasePath = artifact.getFile().toPath();
            }

            logger.debug("Artifact merge metadata triggered for " + artifact.toString() +
                         "(" + artifactBasePath + "). " + repository.getType());

            try
            {
                Metadata metadata = readMetadata(repository, artifact);
                mergeAndStore(artifactBasePath, metadata, mergeMetadata);
            }
            catch (FileNotFoundException e)
            {
                logger.error(e.getMessage(), e);
                throw new IOException("Artifact " + artifact.toString() + " doesn't contain any metadata," +
                                      " therefore we can't merge the metadata!");
            }
        }
        else
        {
            throw new IOException("Artifact " + artifact.toString() + " does not exist in " +
                                  repository.getStorage().getBasedir() + "/" + repository.getBasedir() + " !");
        }
    }

    public void mergeAndStore(final Path metadataBasePath,
                              final Metadata metadata,
                              final Metadata mergeMetadata)
    {
        doInLock(metadataBasePath, path ->
        {
            metadata.merge(mergeMetadata);

            Versioning versioning = metadata.getVersioning();
            if (versioning.getVersions() != null)
            {
                Collections.sort(versioning.getVersions(), new VersionComparator());
            }
            if (versioning.getSnapshotVersions() != null)
            {
                Collections.sort(versioning.getSnapshotVersions(), new SnapshotVersionComparator());
            }

            storeMetadata(metadataBasePath, null, metadata, MetadataType.ARTIFACT_ROOT_LEVEL);
        });

    }

    private void doInLock(final Path metadataBasePath,
                          final Consumer<Path> operation)
    {
        synchronized (getMetadataSynchronizationLock(metadataBasePath))
        {
            operation.accept(metadataBasePath);
        }
    }

    private String getMetadataSynchronizationLock(final Path metadataBasePath)
    {
        final String artifactBasePathAsAbsolutePathString = metadataBasePath.toAbsolutePath().toString();
        final String normalizedPath = FilenameUtils.normalizeNoEndSeparator(artifactBasePathAsAbsolutePathString);
        metadataSynchronizationContainer.putIfAbsent(normalizedPath, normalizedPath);
        return metadataSynchronizationContainer.get(normalizedPath);
    }

}
