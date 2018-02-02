package org.carlspring.strongbox.repository.group.index;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.repository.group.BaseMavenGroupRepositoryComponentTest;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.util.IndexContextHelper;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class MavenIndexGroupRepositoryComponentTest
        extends BaseMavenGroupRepositoryComponentTest
{

    private static final String NEW_REPOSITORY_GROUP_X = "group-repo-x";

    @Inject
    private ArtifactIndexesService artifactIndexesService;

    @Override
    protected void postInitializeInternally()
            throws IOException
    {
        artifactIndexesService.rebuildIndexes();
    }

    @Test
    public void artifactDeletionShouldDeleteArtifactFromParentGroupRepositoryIndex()
            throws Exception
    {

        String artifactPath = "com/artifacts/to/delete/releases/delete-group/1.2.1/delete-group-1.2.1.jar";

        String contextId = IndexContextHelper.getContextId(STORAGE0,
                                                           REPOSITORY_GROUP_F,
                                                           IndexTypeEnum.LOCAL.getType());

        RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndexer(contextId);

        Repository repository = configurationManager.getConfiguration()
                                                    .getStorage(STORAGE0)
                                                    .getRepository(REPOSITORY_LEAF_L);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        RepositoryPath artifactFile = layoutProvider.resolve(repository).resolve(artifactPath);

        indexer.addArtifactToIndex(artifactFile);

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  REPOSITORY_GROUP_F,
                                                  "+g:com.artifacts.to.delete.releases +a:delete-group +v:1.2.1 +e:jar",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(artifactSearchService.contains(request));

        layoutProvider.delete(STORAGE0, REPOSITORY_LEAF_L, artifactPath, false);
        assertFalse("Failed to delete artifact file " + artifactFile.toAbsolutePath(), Files.exists(artifactFile));

        assertFalse(artifactSearchService.contains(request));
    }

    @Test
    public void whenCreatingNewGroupRepositoryItsIndexShouldContainChildrenArtifacts()
            throws Exception
    {
        createGroup(NEW_REPOSITORY_GROUP_X, REPOSITORY_GROUP_C, REPOSITORY_LEAF_D, REPOSITORY_LEAF_L);

        // recoded since we scheduled a cron job now
        artifactIndexesService.rebuildIndex(STORAGE0, NEW_REPOSITORY_GROUP_X, null);


        SearchRequest request = new SearchRequest(STORAGE0,
                                                  NEW_REPOSITORY_GROUP_X,
                                                  "+g:com.artifacts.to.delete.releases +a:delete-group +v:1.2.1 +e:jar",
                                                  MavenIndexerSearchProvider.ALIAS);
        assertThat(artifactSearchService.search(request).getResults().size(), Matchers.equalTo(1));

        request = new SearchRequest(STORAGE0,
                                    NEW_REPOSITORY_GROUP_X,
                                    "+g:com.artifacts.to.delete.releases +a:delete-group +v:1.2.1",
                                    MavenIndexerSearchProvider.ALIAS);
        assertThat(artifactSearchService.search(request).getResults().size(), Matchers.equalTo(2));

        request = new SearchRequest(STORAGE0,
                                    NEW_REPOSITORY_GROUP_X,
                                    "+g:com.artifacts.to.delete.releases +a:delete-group",
                                    MavenIndexerSearchProvider.ALIAS);
        assertThat(artifactSearchService.search(request).getResults().size(), Matchers.equalTo(4));

    }

    @Override
    protected void addRepositoriesToClean(final Set<Repository> repositories)
    {
        repositories.add(createRepositoryMock(STORAGE0, NEW_REPOSITORY_GROUP_X));
    }
}
