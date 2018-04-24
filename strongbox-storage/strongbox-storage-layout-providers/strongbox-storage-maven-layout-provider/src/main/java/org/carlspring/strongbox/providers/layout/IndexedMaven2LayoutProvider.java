package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.MavenIndexerEnabledCondition;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;

import org.apache.maven.index.ArtifactInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
@Conditional(MavenIndexerEnabledCondition.class)
public class IndexedMaven2LayoutProvider
        extends Maven2LayoutProvider
{

    private static final Logger logger = LoggerFactory.getLogger(IndexedMaven2LayoutProvider.class);

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    @Inject
    private ArtifactIndexesService artifactIndexesService;

    @Inject
    private IndexedMavenRepositoryFeatures mavenRepositoryFeatures;

    @Override
    protected void delete(final RepositoryPath directory)
            throws IOException
    {
        super.delete(directory);

        deleteFromIndex(directory);
    }

    //TODO: move this method call into `RepositoryFileSystemProvider.delete(Path path)`
    public void deleteFromIndex(RepositoryPath path)
            throws IOException
    {
        Repository repository = path.getFileSystem().getRepository();
        if (!mavenRepositoryFeatures.isIndexingEnabled(repository))
        {
            return;
        }

        final RepositoryIndexer indexer = getRepositoryIndexer(path);
        if (indexer != null)
        {
            MavenArtifact artifact = MavenArtifactUtils.convertPathToArtifact(RepositoryFiles.stringValue(path));
            MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates(artifact);

            indexer.delete(Collections.singletonList(new ArtifactInfo(repository.getId(),
                                                                      coordinates.getGroupId(),
                                                                      coordinates.getArtifactId(),
                                                                      coordinates.getVersion(),
                                                                      coordinates.getClassifier(),
                                                                      coordinates.getExtension())));
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

        if (!mavenRepositoryFeatures.isIndexingEnabled(repository))
        {
            return null;
        }

        return repositoryIndexManager.getRepositoryIndexer(repository.getStorage().getId() + ":" +
                                                           repository.getId() + ":" +
                                                           IndexTypeEnum.LOCAL.getType());
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
        artifactIndexesService.addArtifactToIndex(repositoryPath);
    }
}
