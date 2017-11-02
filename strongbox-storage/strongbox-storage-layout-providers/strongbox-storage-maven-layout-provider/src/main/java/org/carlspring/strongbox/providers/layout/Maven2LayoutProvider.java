package org.carlspring.strongbox.providers.layout;


import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathHandler;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.repository.MavenRepositoryManagementStrategy;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.services.impl.MavenArtifactManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.storage.search.SearchResults;
import org.carlspring.strongbox.util.IndexContextHelper;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
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
public class Maven2LayoutProvider
        extends AbstractLayoutProvider<MavenArtifactCoordinates,
                                              MavenRepositoryFeatures,
                                              MavenRepositoryManagementStrategy>
        implements RepositoryPathHandler
{

    public static final String ALIAS = "Maven 2";

    private static final Logger logger = LoggerFactory.getLogger(Maven2LayoutProvider.class);
    
    @Inject
    private MavenMetadataManager mavenMetadataManager;

    @Inject
    private MavenArtifactManagementService mavenArtifactManagementService;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private ArtifactSearchService artifactSearchService;

    @Inject
    private MavenRepositoryManagementStrategy mavenRepositoryManagementStrategy;

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    @Inject
    private ArtifactIndexesService artifactIndexesService;

    @Inject
    private MavenRepositoryFeatures repositoryFeatures;

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
        if (path == null || !ArtifactUtils.isArtifact(path))
        {
            return null;
        }

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

    @Override
    public boolean isMetadata(String path)
    {
        return path.endsWith(".pom") || path.endsWith(".xml");
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
        RepositoryPath repositoryPath = resolve(repository).resolve(path);

        if (!Files.isDirectory(repositoryPath))
        {
            deleteFromIndex(repositoryPath);
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
            if (Files.exists(resolve(repository).resolve(pomFilePath)))
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
                        deleteFromIndex(resolve(repository, result.getArtifactCoordinates()));
                    }
                }
                catch (SearchException e)
                {
                    logger.error(e.getMessage(), e);
                }
            }
            // Otherwise, this is either not an artifact directory, or not a valid Maven artifact
        }

        deleteMetadata(storageId, repositoryId, path);

        super.delete(storageId, repositoryId, path, force);
    }

    //TODO: move this method call into `RepositoryFileSystemProvider.delete(Path path)` 
    public void deleteFromIndex(RepositoryPath path)
            throws IOException
    {
        Repository repository = path.getFileSystem().getRepository();
        RepositoryFileAttributes a = (RepositoryFileAttributes) Files.readAttributes(path, BasicFileAttributes.class);

        if (!repositoryFeatures.isIndexingEnabled(repository) || a.isMetadata())
        {
            return;
        }

        final RepositoryIndexer indexer = getRepositoryIndexer(path);
        if (indexer != null)
        {
            String extension = path.getFileName().toString().substring(
                    path.getFileName().toString().lastIndexOf('.') + 1);

            MavenArtifactCoordinates coordinates = (MavenArtifactCoordinates) a.getCoordinates();

            indexer.delete(Collections.singletonList(new ArtifactInfo(repository.getId(),
                                                                      coordinates.getGroupId(),
                                                                      coordinates.getArtifactId(),
                                                                      coordinates.getVersion(),
                                                                      coordinates.getClassifier(),
                                                                      extension)));
        }
    }

    public void closeIndex(String storageId,
                           String repositoryId,
                           String path)
            throws IOException
    {
        logger.debug("Closing " + storageId + ":" + repositoryId + ":" + path + "...");

        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        RepositoryPath repositoryPath = resolve(repository).resolve(path);

        closeIndex(repositoryPath);
    }

    public void closeIndex(RepositoryPath path)
            throws IOException
    {
        final RepositoryIndexer indexer = getRepositoryIndexer(path);
        if (indexer != null)
        {
            logger.debug("Closing indexer of path " + path + "...");

            indexer.close();
        }
    }

    private RepositoryIndexer getRepositoryIndexer(RepositoryPath path)
    {
        Repository repository = path.getFileSystem().getRepository();

        if (!repositoryFeatures.isIndexingEnabled(repository))
        {
            return null;
        }

        return repositoryIndexManager.getRepositoryIndexer(repository.getStorage().getId() + ":" +
                                                           repository.getId() + ":" +
                                                           IndexTypeEnum.LOCAL.getType());
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
            RepositoryPath artifactPath = resolve(repository).resolve(path);
            RepositoryPath artifactBasePath = artifactPath;
            RepositoryPath artifactIdLevelPath = artifactBasePath.getParent();

            if (Files.exists(artifactPath))
            {

                RepositoryFileAttributes artifactFileAttributes = (RepositoryFileAttributes) Files.readAttributes(
                        artifactPath, BasicFileAttributes.class);

                if (!artifactFileAttributes.isDirectory())
                {
                    artifactBasePath = artifactBasePath.getParent();
                    artifactIdLevelPath = artifactIdLevelPath.getParent();

                    // This is at the version level
                    Path pomPath = Files.list(artifactBasePath)
                                        .filter(p -> p.getFileName().toString().endsWith(".pom"))
                                        .findFirst()
                                        .orElse(null);

                    String version = ArtifactUtils.convertPathToArtifact(path).getVersion() != null ?
                                     ArtifactUtils.convertPathToArtifact(path).getVersion() :
                                     pomPath.getParent().getFileName().toString();

                    deleteMetadataAtVersionLevel(artifactBasePath, version);
                }
            }
            else
            {
                artifactBasePath = artifactBasePath.getParent();
                artifactIdLevelPath = artifactIdLevelPath.getParent();
            }

            if (Files.exists(artifactIdLevelPath))
            {
                // This is at the artifact level
                try (Stream<Path> pathStream = Files.list(artifactIdLevelPath))
                {
                    Path mavenMetadataPath = pathStream.filter(p -> p.getFileName()
                                                                     .toString()
                                                                     .endsWith("maven-metadata.xml"))
                                                       .findFirst()
                                                       .orElse(null);

                    if (mavenMetadataPath != null)
                    {
                        String version = FilenameUtils.getName(artifactBasePath.toString());

                        deleteMetadataAtArtifactLevel((RepositoryPath) mavenMetadataPath.getParent(), version);
                    }
                }
            }
        }
        catch (IOException | NoSuchAlgorithmException | XmlPullParserException e)
        {
            // We won't do anything in this case because it doesn't have an impact to the deletion
            logger.error(e.getMessage(), e);
        }
    }

    public void deleteMetadataAtVersionLevel(RepositoryPath metadataBasePath,
                                             String version)
            throws IOException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        if (ArtifactUtils.isSnapshot(version) && Files.exists(metadataBasePath))
        {
            Metadata metadataVersionLevel = mavenMetadataManager.readMetadata(metadataBasePath);
            if (metadataVersionLevel != null && metadataVersionLevel.getVersioning() != null &&
                metadataVersionLevel.getVersioning().getVersions().contains(version))
            {
                metadataVersionLevel.getVersioning().getVersions().remove(version);

                MetadataHelper.setLastUpdated(metadataVersionLevel.getVersioning());

                mavenMetadataManager.storeMetadata(metadataBasePath,
                                                   null,
                                                   metadataVersionLevel,
                                                   MetadataType.SNAPSHOT_VERSION_LEVEL);
            }
        }
    }

    public void deleteMetadataAtArtifactLevel(RepositoryPath artifactPath,
                                              String version)
            throws IOException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        Metadata metadataVersionLevel = mavenMetadataManager.readMetadata(artifactPath);
        if (metadataVersionLevel != null && metadataVersionLevel.getVersioning() != null)
        {
            metadataVersionLevel.getVersioning().getVersions().remove(version);

            if (version.equals(metadataVersionLevel.getVersioning().getLatest()))
            {
                MetadataHelper.setLatest(metadataVersionLevel);
            }

            if (version.equals(metadataVersionLevel.getVersioning().getRelease()))
            {
                MetadataHelper.setRelease(metadataVersionLevel);
            }

            MetadataHelper.setLastUpdated(metadataVersionLevel.getVersioning());

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
    public void undelete(String storageId,
                         String repositoryId,
                         String path)
            throws IOException
    {
        super.undelete(storageId, repositoryId, path);

        artifactIndexesService.rebuildIndex(storageId, repositoryId, path);
    }

    @Override
    public void undeleteTrash(String storageId,
                              String repositoryId)
            throws IOException
    {
        super.undeleteTrash(storageId, repositoryId);

        artifactIndexesService.rebuildIndex(storageId, repositoryId, null);
    }

    @Override
    public void postProcess(RepositoryPath repositoryPath)
            throws IOException
    {
        Boolean artifactAttribute = (Boolean) Files.getAttribute(repositoryPath, RepositoryFileAttributes.ARTIFACT);
        if (!Boolean.TRUE.equals(artifactAttribute))
        {
            return;
        }

        Repository repository = repositoryPath.getFileSystem().getRepository();
        Storage storage = repository.getStorage();

        String contextId = IndexContextHelper.getContextId(storage.getId(),
                                                           repository.getId(),
                                                           IndexTypeEnum.LOCAL.getType());

        RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndexer(contextId);

        if (!repositoryFeatures.isIndexingEnabled(repository) || indexer == null)
        {
            return;
        }

        String repositoryRelativePath = repositoryPath.getResourceLocation();
        Artifact artifact = ArtifactUtils.convertPathToArtifact(repositoryRelativePath);

        File storageBasedir = new File(storage.getBasedir());
        File artifactFile = new File(new File(storageBasedir, repository.getId()),
                                     repositoryRelativePath).getCanonicalFile();

        indexer.addArtifactToIndex(repository.getId(), artifactFile, artifact);
    }

    @Override
    public MavenRepositoryManagementStrategy getRepositoryManagementStrategy()
    {
        return mavenRepositoryManagementStrategy;
    }

    @Override
    public ArtifactManagementService getArtifactManagementService()
    {
        return mavenArtifactManagementService;
    }

    protected RepositoryPathHandler getRepositoryPathHandler()
    {
        return this;
    }

}
