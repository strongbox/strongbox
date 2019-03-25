package org.carlspring.strongbox.repository.group.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.routing.MutableRoutingRuleRepository;
import org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.EnabledIf;

/**
 * @author Przemyslaw Fusik
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


    protected Set<MutableRepository> getRepositories()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_XE_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_XL_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_XZ_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_XD_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_XG_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_XK_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_XO_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_XB_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_XC_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_XF_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_XH_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_XA_2, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @BeforeEach
    public void initialize()
            throws Exception
    {
        createLeaf(STORAGE0, REPOSITORY_LEAF_XE_2);
        createLeaf(STORAGE0, REPOSITORY_LEAF_XL_2);
        createLeaf(STORAGE0, REPOSITORY_LEAF_XZ_2);
        createLeaf(STORAGE0, REPOSITORY_LEAF_XD_2);
        createLeaf(STORAGE0, REPOSITORY_LEAF_XG_2);
        createLeaf(STORAGE0, REPOSITORY_LEAF_XK_2);

        createGroup(REPOSITORY_GROUP_XC_2, STORAGE0, REPOSITORY_LEAF_XE_2, REPOSITORY_LEAF_XZ_2);
        createGroup(REPOSITORY_GROUP_XB_2, STORAGE0, REPOSITORY_GROUP_XC_2, REPOSITORY_LEAF_XD_2, REPOSITORY_LEAF_XL_2);
        createGroup(REPOSITORY_GROUP_XO_2, STORAGE0, REPOSITORY_LEAF_XG_2, REPOSITORY_GROUP_XB_2);
        createGroup(REPOSITORY_GROUP_XF_2, STORAGE0, REPOSITORY_GROUP_XC_2, REPOSITORY_LEAF_XD_2, REPOSITORY_LEAF_XL_2);
        createGroup(REPOSITORY_GROUP_XH_2, STORAGE0, REPOSITORY_GROUP_XF_2, REPOSITORY_LEAF_XK_2);

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_LEAF_XL_2).getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_LEAF_XG_2).getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_LEAF_XD_2).getAbsolutePath(),
                         "com.artifacts.to.update.releases:update-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_LEAF_XK_2).getAbsolutePath(),
                         "com.artifacts.to.update.releases:update-group",
                         new String[]{ "1.2.1" }
        );

        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_XL_2);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_XG_2);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_XD_2);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_XK_2);

        /**
         <denied>
             <rule-set group-repository="group-repo-h">
                 <rule pattern=".*(com|org)/artifacts/to/update/releases/update-group.*">
                     <repositories>
                         <repository>leaf-repo-d</repository>
                     </repositories>
                 </rule>
             </rule-set>
         </denied>
         **/
        createAndAddRoutingRule(STORAGE0,
                                REPOSITORY_GROUP_XH_2,
                                Arrays.asList(new MutableRoutingRuleRepository(STORAGE0, REPOSITORY_LEAF_XD_2)),
                                ".*(com|org)/artifacts/to/update/releases/update-group.*",
                                RoutingRuleTypeEnum.DENY);

        rebuildIndexes(getRepositories());
    }

    @Test
    public void whenCreatingNewGroupRepositoryItsIndexShouldContainChildrenArtifacts()
            throws Exception
    {
        MutableRepository repository = createGroup(REPOSITORY_GROUP_XA_2, STORAGE0, REPOSITORY_GROUP_XC_2,
                                                   REPOSITORY_LEAF_XD_2,
                                                   REPOSITORY_LEAF_XL_2);

        RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(new ImmutableRepository(repository));
        // recoded since we scheduled a cron job now
        artifactIndexesService.rebuildIndex(repositoryPath);


        SearchRequest request = new SearchRequest(STORAGE0, REPOSITORY_GROUP_XA_2,
                                                  "+g:com.artifacts.to.delete.releases +a:delete-group +v:1.2.1 +e:jar",
                                                  MavenIndexerSearchProvider.ALIAS);
        assertThat(artifactSearchService.search(request).getResults().size(), Matchers.equalTo(1));

        request = new SearchRequest(STORAGE0, REPOSITORY_GROUP_XA_2,
                                    "+g:com.artifacts.to.delete.releases +a:delete-group +v:1.2.1",
                                    MavenIndexerSearchProvider.ALIAS);

        assertThat(artifactSearchService.search(request).getResults().size(), Matchers.equalTo(2));

        request = new SearchRequest(STORAGE0, REPOSITORY_GROUP_XA_2,
                                    "+g:com.artifacts.to.delete.releases +a:delete-group",
                                    MavenIndexerSearchProvider.ALIAS);

        assertThat(artifactSearchService.search(request).getResults().size(), Matchers.equalTo(4));
    }

}
