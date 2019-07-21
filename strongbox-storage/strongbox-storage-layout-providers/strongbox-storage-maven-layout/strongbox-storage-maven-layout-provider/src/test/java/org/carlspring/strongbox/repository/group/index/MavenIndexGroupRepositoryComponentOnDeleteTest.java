package org.carlspring.strongbox.repository.group.index;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group.Rule;
import org.carlspring.strongbox.util.IndexContextHelper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import static org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum.DENY;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
public class MavenIndexGroupRepositoryComponentOnDeleteTest
        extends BaseMavenIndexGroupRepositoryComponentTest
{

    private static final String REPOSITORY_GROUP_XA = "migrc-group-xa";

    private static final String REPOSITORY_LEAF_XE = "leaf-repo-xe";

    private static final String REPOSITORY_LEAF_XL = "leaf-repo-xl";

    private static final String REPOSITORY_LEAF_XZ = "leaf-repo-xz";

    private static final String REPOSITORY_LEAF_XD = "leaf-repo-xd";

    private static final String REPOSITORY_LEAF_XG = "leaf-repo-xg";

    private static final String REPOSITORY_LEAF_XK = "leaf-repo-xk";

    private static final String REPOSITORY_GROUP_XO = "group-repo-xo";

    private static final String REPOSITORY_GROUP_XB = "group-repo-xb";

    private static final String REPOSITORY_GROUP_XC = "group-repo-xc";

    private static final String REPOSITORY_GROUP_XF = "group-repo-xf";

    private static final String REPOSITORY_GROUP_XH = "group-repo-xh";

    protected Set<RepositoryDto> getRepositories()
    {
        Set<RepositoryDto> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_XE, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_XL, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_XZ, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_XD, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_XG, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_XK, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_XO, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_XB, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_XC, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_XF, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_XH, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_XA, Maven2LayoutProvider.ALIAS));

        return repositories;
    }


    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void artifactDeletionShouldDeleteArtifactFromParentGroupRepositoryIndex(
            @MavenRepository(repositoryId = REPOSITORY_LEAF_XE, setup = MavenIndexedRepositorySetup.class)
                    Repository repositoryLeafXe,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_XL, setup = MavenIndexedRepositorySetup.class)
                    Repository repositoryLeafXl,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_XZ, setup = MavenIndexedRepositorySetup.class)
                    Repository repositoryLeafXz,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_XD, setup = MavenIndexedRepositorySetup.class)
                    Repository repositoryLeafXd,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_XG, setup = MavenIndexedRepositorySetup.class)
                    Repository repositoryLeafXg,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_XK, setup = MavenIndexedRepositorySetup.class)
                    Repository repositoryLeafXk,
            @Group({ REPOSITORY_LEAF_XE,
                     REPOSITORY_LEAF_XZ })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_XC, setup = MavenIndexedRepositorySetup.class)
                    Repository repositoryGroupXc,
            @Group({ REPOSITORY_GROUP_XC,
                     REPOSITORY_LEAF_XD,
                     REPOSITORY_LEAF_XL })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_XB, setup = MavenIndexedRepositorySetup.class)
                    Repository repositoryGroupXb,
            @Group({ REPOSITORY_LEAF_XG,
                     REPOSITORY_GROUP_XB })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_XO, setup = MavenIndexedRepositorySetup.class)
                    Repository repositoryGroupXo,
            @Group({ REPOSITORY_GROUP_XC,
                     REPOSITORY_LEAF_XD,
                     REPOSITORY_LEAF_XL })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_XF, setup = MavenIndexedRepositorySetup.class)
                    Repository repositoryGroupXf,
            @Group(repositories = { REPOSITORY_GROUP_XF,
                                    REPOSITORY_LEAF_XK },
                    rules = { @Rule(
                            pattern = ".*(com|org)/artifacts/to/update/releases/update-group.*",
                            repositories = REPOSITORY_LEAF_XD,
                            type = DENY)
                    })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_XH, setup = MavenIndexedRepositorySetup.class)
                    Repository repositoryGroupXh,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_XL, id = "com.artifacts.to.delete.releases:delete-group", versions = { "1.2.1",
                                                                                                                                     "1.2.2" })
                    Path artifactLeafXl,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_XG, id = "com.artifacts.to.delete.releases:delete-group", versions = { "1.2.1",
                                                                                                                                     "1.2.2" })
                    Path artifactLeafXg,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_XD, id = "com.artifacts.to.update.releases:update-group", versions = { "1.2.1",
                                                                                                                                     "1.2.2" })
                    Path artifactLeafXd,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_XK, id = "com.artifacts.to.update.releases:update-group", versions = { "1.2.1" })
                    Path artifactLeafXk)
            throws Exception
    {

        generateMavenMetadata(STORAGE0, repositoryLeafXl.getId());
        generateMavenMetadata(STORAGE0, repositoryLeafXg.getId());
        generateMavenMetadata(STORAGE0, repositoryLeafXd.getId());
        generateMavenMetadata(STORAGE0, repositoryLeafXk.getId());

        rebuildIndexes(getRepositories());

        String artifactPath = "com/artifacts/to/delete/releases/delete-group/1.2.1/delete-group-1.2.1.jar";

        String contextId = IndexContextHelper.getContextId(STORAGE0,
                                                           repositoryGroupXf.getId(),
                                                           IndexTypeEnum.LOCAL.getType());

        Assumptions.assumeTrue(repositoryIndexManager.isPresent());
        RepositoryIndexer indexer = repositoryIndexManager.get().getRepositoryIndexer(contextId);

        RepositoryPath artifactFile = repositoryPathResolver.resolve(repositoryLeafXl, artifactPath);

        indexer.addArtifactToIndex(artifactFile);

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  repositoryGroupXf.getId(),
                                                  "+g:com.artifacts.to.delete.releases +a:delete-group +v:1.2.1 +e:jar",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(artifactSearchService.contains(request));

        RepositoryFiles.delete(artifactFile, false);

        assertFalse(Files.exists(artifactFile), "Failed to delete artifact file " + artifactFile.toAbsolutePath());

        assertFalse(artifactSearchService.contains(request));
    }

}
