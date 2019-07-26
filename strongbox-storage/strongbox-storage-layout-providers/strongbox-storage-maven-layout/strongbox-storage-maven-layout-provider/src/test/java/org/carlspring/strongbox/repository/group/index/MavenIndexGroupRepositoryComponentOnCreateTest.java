package org.carlspring.strongbox.repository.group.index;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group.Rule;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import static org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum.DENY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
@EnabledIf(expression = "#{containsObject('artifactIndexesService')}", loadContext = true)
public class MavenIndexGroupRepositoryComponentOnCreateTest
        extends BaseMavenIndexGroupRepositoryComponentTest
{

    private static final String REPOSITORY_GROUP_XA_2 = "migrc-group-xa_2";

    private static final String REPOSITORY_LEAF_XE_2 = "leaf-repo-xe_2";

    private static final String REPOSITORY_LEAF_XL_2 = "leaf-repo-xl_2";

    private static final String REPOSITORY_LEAF_XZ_2 = "leaf-repo-xz_2";

    private static final String REPOSITORY_LEAF_XD_2 = "leaf-repo-xd_2";

    private static final String REPOSITORY_LEAF_XG_2 = "leaf-repo-xg_2";

    private static final String REPOSITORY_LEAF_XK_2 = "leaf-repo-xk_2";

    private static final String REPOSITORY_GROUP_XO_2 = "group-repo-xo_2";

    private static final String REPOSITORY_GROUP_XB_2 = "group-repo-xb_2";

    private static final String REPOSITORY_GROUP_XC_2 = "group-repo-xc_2";

    private static final String REPOSITORY_GROUP_XF_2 = "group-repo-xf_2";

    private static final String REPOSITORY_GROUP_XH_2 = "group-repo-xh_2";


    protected Set<Repository> getRepositories(Repository... repositories)
    {
        return Stream.of(repositories).collect(Collectors.toSet());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void whenCreatingNewGroupRepositoryItsIndexShouldContainChildrenArtifacts(
            @MavenRepository(repositoryId = REPOSITORY_LEAF_XE_2, setup = MavenIndexedRepositorySetup.class) Repository repositoryLeafXe2,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_XL_2, setup = MavenIndexedRepositorySetup.class) Repository repositoryLeafXl2,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_XZ_2, setup = MavenIndexedRepositorySetup.class) Repository repositoryLeafXz2,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_XD_2, setup = MavenIndexedRepositorySetup.class) Repository repositoryLeafXd2,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_XG_2, setup = MavenIndexedRepositorySetup.class) Repository repositoryLeafXg2,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_XK_2, setup = MavenIndexedRepositorySetup.class) Repository repositoryLeafXk2,
            @Group({ REPOSITORY_LEAF_XE_2,
                     REPOSITORY_LEAF_XZ_2 })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_XC_2, setup = MavenIndexedRepositorySetup.class) Repository repositoryGroupXc2,
            @Group({ REPOSITORY_GROUP_XC_2,
                     REPOSITORY_LEAF_XD_2,
                     REPOSITORY_LEAF_XL_2 })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_XB_2, setup = MavenIndexedRepositorySetup.class) Repository repositoryGroupXb2,
            @Group({ REPOSITORY_LEAF_XG_2,
                     REPOSITORY_GROUP_XB_2 })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_XO_2, setup = MavenIndexedRepositorySetup.class) Repository repositoryGroupXo2,
            @Group({ REPOSITORY_GROUP_XC_2,
                     REPOSITORY_LEAF_XD_2,
                     REPOSITORY_LEAF_XL_2 })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_XF_2, setup = MavenIndexedRepositorySetup.class) Repository repositoryGroupXf2,
            @Group(repositories = { REPOSITORY_GROUP_XF_2,
                                    REPOSITORY_LEAF_XK_2 },
                    rules = { @Rule(
                            pattern = ".*(com|org)/artifacts/to/update/releases/update-group.*",
                            repositories = REPOSITORY_LEAF_XD_2,
                            type = DENY)
                    })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_XH_2, setup = MavenIndexedRepositorySetup.class) Repository repositoryGroupXh2,
            @Group(repositories = { REPOSITORY_GROUP_XC_2,
                                    REPOSITORY_LEAF_XD_2,
                                    REPOSITORY_LEAF_XL_2 })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_XA_2, setup = MavenIndexedRepositorySetup.class) Repository repositoryGroupXa2,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_XL_2, id = "com.artifacts.to.delete.releases:delete-group", versions = { "1.2.1",
                                                                                                                                       "1.2.2" })
                    Path artifactLeafXl2,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_XG_2, id = "com.artifacts.to.delete.releases:delete-group", versions = { "1.2.1",
                                                                                                                                       "1.2.2" })
                    Path artifactLeafXg2,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_XD_2, id = "com.artifacts.to.update.releases:update-group", versions = { "1.2.1",
                                                                                                                                       "1.2.2" })
                    Path artifactLeafXd2,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_XK_2, id = "com.artifacts.to.update.releases:update-group", versions = { "1.2.1" })
                    Path artifactLeafXk2)
            throws Exception
    {
        final String repositoryGroupXa2Id = repositoryGroupXa2.getId();

        generateMavenMetadata(STORAGE0, repositoryLeafXl2.getId());
        generateMavenMetadata(STORAGE0, repositoryLeafXg2.getId());
        generateMavenMetadata(STORAGE0, repositoryLeafXd2.getId());
        generateMavenMetadata(STORAGE0, repositoryLeafXk2.getId());

        Set<Repository> repositoriesSet = getRepositories(repositoryLeafXe2, repositoryLeafXl2, repositoryLeafXz2,
                                                          repositoryLeafXd2, repositoryLeafXg2, repositoryLeafXk2,
                                                          repositoryGroupXo2, repositoryGroupXb2, repositoryGroupXc2,
                                                          repositoryGroupXf2, repositoryGroupXh2, repositoryGroupXa2);
        rebuildIndexes(repositoriesSet);

        RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(repositoryGroupXa2);
        // recoded since we scheduled a cron job now
        artifactIndexesService.rebuildIndex(repositoryPath);


        SearchRequest request = new SearchRequest(STORAGE0,
                                                  repositoryGroupXa2Id,
                                                  "+g:com.artifacts.to.delete.releases +a:delete-group +v:1.2.1 +e:jar",
                                                  MavenIndexerSearchProvider.ALIAS);
        assertEquals(1, artifactSearchService.search(request).getResults().size());

        request = new SearchRequest(STORAGE0,
                                    repositoryGroupXa2Id,
                                    "+g:com.artifacts.to.delete.releases +a:delete-group +v:1.2.1",
                                    MavenIndexerSearchProvider.ALIAS);

        assertEquals(2, artifactSearchService.search(request).getResults().size());

        request = new SearchRequest(STORAGE0,
                                    repositoryGroupXa2Id,
                                    "+g:com.artifacts.to.delete.releases +a:delete-group",
                                    MavenIndexerSearchProvider.ALIAS);

        assertEquals(4, artifactSearchService.search(request).getResults().size());
    }

}
