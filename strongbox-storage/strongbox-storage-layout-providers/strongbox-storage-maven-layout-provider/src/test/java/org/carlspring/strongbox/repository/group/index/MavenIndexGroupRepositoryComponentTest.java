package org.carlspring.strongbox.repository.group.index;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.repository.group.BaseMavenGroupRepositoryComponentTest;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.util.IndexContextHelper;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class MavenIndexGroupRepositoryComponentTest
        extends BaseMavenGroupRepositoryComponentTest
{
    private static final String REPOSITORY_GROUP = "migrc-group";

    @Inject
    private Optional<ArtifactIndexesService> artifactIndexesService;

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;

    @Override
    protected void postInitializeInternally()
            throws IOException
    {
        Assume.assumeTrue(artifactIndexesService.isPresent());

        artifactIndexesService.get().rebuildIndexes();
    }

    @Test
    public void artifactDeletionShouldDeleteArtifactFromParentGroupRepositoryIndex()
            throws Exception
    {

        String artifactPath = "com/artifacts/to/delete/releases/delete-group/1.2.1/delete-group-1.2.1.jar";

        String contextId = IndexContextHelper.getContextId(STORAGE0,
                                                           REPOSITORY_GROUP_F,
                                                           IndexTypeEnum.LOCAL.getType());

        RepositoryIndexer indexer = repositoryIndexManager.get().getRepositoryIndexer(contextId);

        MutableRepository repository = mavenRepositoryFactory.createRepository(REPOSITORY_LEAF_L);
        repository.setStorage(configurationManagementService.getMutableConfigurationClone().getStorage(STORAGE0));

        RepositoryPath artifactFile = repositoryPathResolver.resolve(new Repository(repository), artifactPath);

        indexer.addArtifactToIndex(artifactFile);

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  REPOSITORY_GROUP_F,
                                                  "+g:com.artifacts.to.delete.releases +a:delete-group +v:1.2.1 +e:jar",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(artifactSearchService.contains(request));

        RepositoryFiles.delete(artifactFile, false);

        assertFalse("Failed to delete artifact file " + artifactFile.toAbsolutePath(), Files.exists(artifactFile));

        assertFalse(artifactSearchService.contains(request));
    }

    @Test
    public void whenCreatingNewGroupRepositoryItsIndexShouldContainChildrenArtifacts()
            throws Exception
    {
        MutableRepository repository = createGroup(REPOSITORY_GROUP, STORAGE0, REPOSITORY_GROUP_C, REPOSITORY_LEAF_D, REPOSITORY_LEAF_L);

        RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(new Repository(repository));
        // recoded since we scheduled a cron job now
        artifactIndexesService.get().rebuildIndex(repositoryPath);


        SearchRequest request = new SearchRequest(STORAGE0, REPOSITORY_GROUP,
                                                  "+g:com.artifacts.to.delete.releases +a:delete-group +v:1.2.1 +e:jar",
                                                  MavenIndexerSearchProvider.ALIAS);
        assertThat(artifactSearchService.search(request).getResults().size(), Matchers.equalTo(1));

        request = new SearchRequest(STORAGE0, REPOSITORY_GROUP,
                                    "+g:com.artifacts.to.delete.releases +a:delete-group +v:1.2.1",
                                    MavenIndexerSearchProvider.ALIAS);
        assertThat(artifactSearchService.search(request).getResults().size(), Matchers.equalTo(2));

        request = new SearchRequest(STORAGE0, REPOSITORY_GROUP,
                                    "+g:com.artifacts.to.delete.releases +a:delete-group",
                                    MavenIndexerSearchProvider.ALIAS);
        assertThat(artifactSearchService.search(request).getResults().size(), Matchers.equalTo(4));

    }

    @Override
    protected void addRepositoriesToClean(final Set<MutableRepository> repositories)
    {
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP, Maven2LayoutProvider.ALIAS));
    }

}
