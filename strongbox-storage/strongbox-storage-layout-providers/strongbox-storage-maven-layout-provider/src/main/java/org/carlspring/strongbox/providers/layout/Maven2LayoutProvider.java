package org.carlspring.strongbox.providers.layout;

import org.carlspring.maven.commons.io.filters.PomFilenameFilter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.RepositoryPath;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.repository.MavenRepositoryManagementStrategy;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.storage.search.SearchResults;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.checksum.MavenChecksumManager;
import org.carlspring.strongbox.storage.indexing.*;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.UnknownRepositoryTypeException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.index.ArtifactInfo;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("maven2LayoutProvider")
public class Maven2LayoutProvider extends AbstractLayoutProvider<MavenArtifactCoordinates,
                                                                 MavenRepositoryFeatures,
                                                                 MavenRepositoryManagementStrategy>
{

    private static final Logger logger = LoggerFactory.getLogger(Maven2LayoutProvider.class);

    public static final String ALIAS = "Maven 2";

    @Inject
    private MavenMetadataManager mavenMetadataManager;

    @Inject
    private MavenChecksumManager mavenChecksumManager;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private ArtifactSearchService artifactSearchService;

    @Inject
    private MavenRepositoryFeatures mavenRepositoryFeatures;

    @Inject
    private MavenRepositoryManagementStrategy mavenRepositoryManagementStrategy;

    @Inject
    private RepositoryIndexManager repositoryIndexManager;


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
    public MavenArtifactCoordinates getArtifactCoordinates(String path)
    {
        MavenArtifactCoordinates coordinates;
        if (isMetadata(path))
        {
            Artifact artifact = ArtifactUtils.convertPathToArtifact(path);
            coordinates = new MavenArtifactCoordinates(artifact);
        }
        else
        {
            coordinates = new MavenArtifactCoordinates(path);
        }

        return coordinates;
    }

    protected boolean isMetadata(String path)
    {
        return ArtifactUtils.isMetadata(path);
    }

    @Override
    public void delete(String storageId,
                       String repositoryId,
                       String path,
                       boolean force)
            throws IOException, SearchException
    {
        logger.debug("Removing " + storageId + ":" + repositoryId + ":" + path + "...");

        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        RepositoryPath repositoryPath = resolve(repository, path);

        if (!Files.isDirectory(repositoryPath))
        {
            deleteFromIndex(storageId, repositoryId, path);
        }
        else
        {
            String[] artifactCoordinateElements = path.split("/");
            StringBuilder groupId = new StringBuilder();
            for (int i = 0; i < artifactCoordinateElements.length - 2; i++)
            {
                String element = artifactCoordinateElements[i];
                groupId.append((groupId.length() == 0) ? element : "." + element);
            }

            String artifactId = artifactCoordinateElements[artifactCoordinateElements.length - 2];
            String version = artifactCoordinateElements[artifactCoordinateElements.length - 1];

            String pomFilePath = path + "/" + artifactId + "-" + version + ".pom";

            // If there is a pom file, read it.
            if (Files.exists(resolve(repository, pomFilePath)))
            {
                // Run a search against the index and get a list of all the artifacts matching this exact GAV
                SearchRequest request = new SearchRequest(storageId,
                                                          repositoryId,
                                                          "+g:" + groupId + " " +
                                                          "+a:" + artifactId + " " +
                                                          "+v:" + version,
                                                          MavenIndexerSearchProvider.ALIAS);

                try
                {
                    SearchResults results = artifactSearchService.search(request);

                    for (SearchResult result : results.getResults())
                    {
                        String artifactPath = result.getArtifactCoordinates().toPath();

                        logger.debug("Removing " + artifactPath + " from index...");

                        deleteFromIndex(storageId, repositoryId, artifactPath);
                    }
                }
                catch (SearchException e)
                {
                    logger.error(e.getMessage(), e);
                }
            }
            // Otherwise, this is either not an artifact directory, or not a valid Maven artifact
        }

        super.delete(storageId, repositoryId, path, force);

        deleteMetadata(storageId, repositoryId, path);
    }

    @Override
    protected void doDeletePath(RepositoryPath repositoryPath,
                                boolean force,
                                boolean deleteChecksum)
            throws IOException
    {
        // Delete the path
        super.doDeletePath(repositoryPath, force, deleteChecksum);

        if (deleteChecksum && !ArtifactUtils.isChecksum(repositoryPath.getTarget().getFileName().toString()))
        {
            // Delete the checksums
            // TODO: Add check, if the paths exist

            RepositoryPath md5Path = repositoryPath.resolveSibling(repositoryPath.getFileName() + ".md5");
            if (Files.exists(md5Path))
            {
                super.doDeletePath(md5Path, force, deleteChecksum);
            }

            RepositoryPath sha1Path = repositoryPath.resolveSibling(repositoryPath.getFileName() + ".sha1");
            if (Files.exists(sha1Path))
            {
                super.doDeletePath(sha1Path, force, deleteChecksum);
            }
        }
    }

    public void deleteFromIndex(String storageId,
                                String repositoryId,
                                String path)
            throws IOException
    {
        if (isMetadata(path))
        {
            return;
        }


        final RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndexer(storageId + ":" +
                                                                                      repositoryId + ":" +
                                                                                      IndexTypeEnum.LOCAL.getType());
        if (indexer != null)
        {
            String extension = path.substring(path.lastIndexOf('.') + 1, path.length());

            final Artifact a = ArtifactUtils.convertPathToArtifact(path);

            indexer.delete(Collections.singletonList(new ArtifactInfo(repositoryId,
                                                                      a.getGroupId(),
                                                                      a.getArtifactId(),
                                                                      a.getVersion(),
                                                                      a.getClassifier(),
                                                                      extension)));
        }
    }

    @Override
    public void deleteMetadata(String storageId,
                               String repositoryId,
                               String path)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        try
        {
            RepositoryPath artifactVersionPath = resolve(repository, path);

            if (Files.exists(artifactVersionPath))
            {
                // This is at the version level
                Path pomPath = Files.list(artifactVersionPath.getTarget())
                                    .filter(p -> p.getFileName().endsWith(".pom"))
                                    .findFirst()
                                    .orElse(null);

                String version = ArtifactUtils.convertPathToArtifact(path).getVersion() != null ?
                                 ArtifactUtils.convertPathToArtifact(path).getVersion() :
                                 pomPath.getParent().getFileName().toString();

                deleteMetadataAtVersionLevel(artifactVersionPath, version);
            }
            else
            {
                // This is at the artifact level
                Path mavenMetadataPath = Files.list(artifactVersionPath.getTarget().getParent())
                                              .filter(p -> p.getFileName().endsWith("maven-metadata.xml"))
                                              .findFirst()
                                              .orElse(null);

                if (mavenMetadataPath != null)
                {
                    String version = path.substring(path.lastIndexOf('/') + 1, path.length());

                    deleteMetadataAtArtifactLevel(resolve(repository, mavenMetadataPath.getParent().toString()), version);
                }
            }
        }
        catch (IOException | NoSuchAlgorithmException | XmlPullParserException e)
        {
            // We won't do anything in this case because it doesn't have an impact to the deletion
            logger.error(e.getMessage(), e);
        }
    }

    public void deleteMetadataAtVersionLevel(RepositoryPath artifactVersionPath, String version)
            throws IOException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        if (ArtifactUtils.isSnapshot(version) && Files.exists(artifactVersionPath))
        {
            Metadata metadataVersionLevel = mavenMetadataManager.readMetadata(artifactVersionPath);
            if (metadataVersionLevel != null && metadataVersionLevel.getVersioning() != null &&
                metadataVersionLevel.getVersioning().getVersions().contains(version))
            {
                metadataVersionLevel.getVersioning().getVersions().remove(version);

                MetadataHelper.setLastUpdated(metadataVersionLevel.getVersioning());

                mavenMetadataManager.storeMetadata(artifactVersionPath,
                                                   null,
                                                   metadataVersionLevel,
                                                   MetadataType.SNAPSHOT_VERSION_LEVEL);
            }
        }
    }

    public void deleteMetadataAtArtifactLevel(RepositoryPath artifactPath, String version)
            throws IOException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        Metadata metadataVersionLevel = mavenMetadataManager.readMetadata(artifactPath);
        if (metadataVersionLevel != null && metadataVersionLevel.getVersioning() != null)
        {
            if (metadataVersionLevel.getVersioning().getVersions().contains(version))
            {
                metadataVersionLevel.getVersioning().getVersions().remove(version);
                MetadataHelper.setLastUpdated(metadataVersionLevel.getVersioning());
            }

            if (metadataVersionLevel.getVersioning().getLatest() != null &&
                metadataVersionLevel.getVersioning().getLatest().equals(version))
            {
                if (metadataVersionLevel.getVersioning().getVersions() != null &&
                    metadataVersionLevel.getVersioning().getVersions().isEmpty())
                {
                    metadataVersionLevel.getVersioning().setLatest(null);
                }
                else
                {
                    MetadataHelper.setLatest(metadataVersionLevel);
                }

                MetadataHelper.setLastUpdated(metadataVersionLevel.getVersioning());
            }

            mavenMetadataManager.storeMetadata(artifactPath,
                                               null,
                                               metadataVersionLevel,
                                               MetadataType.ARTIFACT_ROOT_LEVEL);
        }
    }

    @Override
    public void rebuildMetadata(String storageId,
                                String repositoryId,
                                String basePath)
            throws IOException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        artifactMetadataService.rebuildMetadata(storageId, repositoryId, basePath);
    }

    @Override
    public void rebuildIndexes(String storageId,
                               String repositoryId,
                               String basePath,
                               boolean forceRegeneration)
            throws IOException
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void regenerateChecksums(Repository repository,
                                    List<String> versionDirectories,
                                    boolean forceRegeneration)
            throws IOException,
                   NoSuchAlgorithmException,
                   ProviderImplementationException,
                   UnknownRepositoryTypeException,
                   ArtifactTransportException
    {
        /**
         * In the repository we need to generate checksum for files in the artifactBasePath and
         * for each version directory.
         */
        if (!versionDirectories.isEmpty())
            {
                RepositoryPath basePath = resolve(repository, versionDirectories.get(0)).getParent();

                logger.debug("Artifact checksum generation triggered for " + basePath + " in '" +
                             repository.getStorage().getId() + ":" + repository.getId() + "'" +
                             " [policy: " + repository.getPolicy() + "].");

                versionDirectories.forEach(path ->
                                           {
                                               try
                                               {
                                                   storeChecksum(repository,
                                                                 resolve(repository, path),
                                                                 forceRegeneration);
                                               }
                                               catch (IOException |
                                                      NoSuchAlgorithmException |
                                                      ArtifactTransportException |
                                                      ProviderImplementationException e)
                                               {
                                                   logger.error(e.getMessage(), e);
                                               }

                                               logger.debug("Generated Maven checksum for " + path + ".");
                                           });

                storeChecksum(repository, basePath, forceRegeneration);
        }
        else
        {
            logger.error("Artifact checksum generation failed.");
        }
    }

    @Override
    public FilenameFilter getMetadataFilenameFilter()
    {
        return new PomFilenameFilter();
    }

    @Override
    public MavenRepositoryFeatures getRepositoryFeatures()
    {
        return mavenRepositoryFeatures;
    }

    @Override
    public MavenRepositoryManagementStrategy getRepositoryManagementStrategy()
    {
        return mavenRepositoryManagementStrategy;
    }

}
