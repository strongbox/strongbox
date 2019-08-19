package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.locator.handlers.GenerateMavenMetadataOperation;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.Indexer;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexCreator;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexingContextFactory;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.MutableRoutingRule;
import org.carlspring.strongbox.storage.routing.MutableRoutingRuleRepository;
import org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum;

import javax.inject.Inject;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.io.ByteStreams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.maven.index.FlatSearchRequest;
import org.apache.maven.index.FlatSearchResponse;
import org.apache.maven.index.context.IndexingContext;
import org.assertj.core.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
public abstract class TestCaseWithMavenArtifactGenerationAndIndexing
        extends MavenTestCaseWithArtifactGeneration
{
    protected static final String STORAGE0 = "storage0";
    
    @Inject
    private PropertiesBooter propertiesBooter;

    @Inject
    protected RepositoryManagementService repositoryManagementService;

    @Inject
    protected MavenMetadataManager mavenMetadataManager;

    @Inject
    protected ArtifactResolutionService artifactResolutionService;

    @Inject
    protected ArtifactEventListenerRegistry artifactEventListenerRegistry;

    protected static final org.apache.maven.index.Indexer indexer = Indexer.INSTANCE;

    public void createAndAddRoutingRule(String groupStorageId,
                                        String groupRepositoryId,
                                        List<MutableRoutingRuleRepository> repositories,
                                        String rulePattern,
                                        RoutingRuleTypeEnum type) throws IOException
    {
        MutableRoutingRule routingRule = MutableRoutingRule.create(groupStorageId, groupRepositoryId,
                                                                   repositories, rulePattern, type);
        configurationManagementService.addRoutingRule(routingRule);
    }

    protected void generateMavenMetadata(String storageId,
                                         String repositoryId)
            throws IOException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

        ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
        locator.setBasedir(repositoryPath);
        locator.setOperation(new GenerateMavenMetadataOperation(mavenMetadataManager, artifactEventListenerRegistry));
        locator.locateArtifactDirectories();
    }

    protected Configuration getConfiguration()
    {
        return configurationManagementService.getConfiguration();
    }

    protected Path getVaultDirectoryPath()
    {
        String base = FilenameUtils.normalize(propertiesBooter.getVaultDirectory());
        if (StringUtils.isBlank(base))
        {
            throw new IllegalStateException("propertiesBooter.getVaultDirectory() resolves to '" + base +
                                            "' which is illegal base path here.");
        }
        return Paths.get(base);
    }

    protected void deleteDirectoryRelativeToVaultDirectory(String dirPathToDelete)
            throws Exception
    {
        Path basePath = getVaultDirectoryPath();
        Path fullDirPathToDelete = basePath.resolve(dirPathToDelete);
        FileUtils.deleteDirectory(fullDirPathToDelete.toFile());
    }

    protected void assertStreamNotNull(final String storageId,
                                       final String repositoryId,
                                       final String path)
            throws Exception
    {
        RepositoryPath repositoryPath = artifactResolutionService.resolvePath(storageId,
                                                                              repositoryId,
                                                                              path);

        try (final InputStream is = artifactResolutionService.getInputStream(repositoryPath))
        {
            assertNotNull(is, "Failed to resolve " + path + "!");

            if (RepositoryFiles.isMetadata(repositoryPath))
            {
                System.out.println(ByteStreams.toByteArray(is));
            }
            else
            {
                while (is.read(new byte[1024]) != -1);
            }
        }
    }


    public RepositoryManagementService getRepositoryManagementService()
    {
        return repositoryManagementService;
    }

    protected static class RepositoryIndexingContextAssert
            implements Closeable
    {

        protected final IndexingContext indexingContext;

        public RepositoryIndexingContextAssert(Repository repository,
                                               RepositoryIndexCreator repositoryIndexCreator,
                                               RepositoryIndexingContextFactory indexingContextFactory)
                throws IOException
        {
            RepositoryPath indexPath = repositoryIndexCreator.apply(repository);
            indexingContext = indexingContextFactory.create(repository);
            indexingContext.merge(new SimpleFSDirectory(indexPath));
        }

        public QueriedIndexerAssert onSearchQuery(Query query)
        {
            return new QueriedIndexerAssert(query, indexingContext);
        }

        public void close()
                throws IOException
        {
            indexingContext.close(true);
        }

        public class QueriedIndexerAssert
        {

            private final Query query;
            private final IndexingContext indexingContext;

            public QueriedIndexerAssert(Query query,
                                        IndexingContext indexingContext)
            {
                this.query = query;
                this.indexingContext = indexingContext;
            }

            public void hitTotalTimes(int expectedHitsCount)
                    throws IOException
            {
                FlatSearchResponse response = indexer.searchFlat(
                        new FlatSearchRequest(query, indexingContext));
                Assertions.assertThat(response.getTotalHitsCount()).isEqualTo(expectedHitsCount);
            }
        }
    }
    
}
