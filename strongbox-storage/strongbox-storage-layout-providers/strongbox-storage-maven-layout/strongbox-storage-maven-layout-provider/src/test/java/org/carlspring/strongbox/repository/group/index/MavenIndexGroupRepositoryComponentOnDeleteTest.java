package org.carlspring.strongbox.repository.group.index;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.util.IndexContextHelper;

import javax.inject.Inject;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
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

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;


    protected Set<MutableRepository> getRepositories()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
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

    @BeforeEach
    public void initialize()
            throws Exception
    {
        createLeaf(STORAGE0, REPOSITORY_LEAF_XE);
        createLeaf(STORAGE0, REPOSITORY_LEAF_XL);
        createLeaf(STORAGE0, REPOSITORY_LEAF_XZ);
        createLeaf(STORAGE0, REPOSITORY_LEAF_XD);
        createLeaf(STORAGE0, REPOSITORY_LEAF_XG);
        createLeaf(STORAGE0, REPOSITORY_LEAF_XK);

        createGroup(REPOSITORY_GROUP_XC, STORAGE0, REPOSITORY_LEAF_XE, REPOSITORY_LEAF_XZ);
        createGroup(REPOSITORY_GROUP_XB, STORAGE0, REPOSITORY_GROUP_XC, REPOSITORY_LEAF_XD, REPOSITORY_LEAF_XL);
        createGroup(REPOSITORY_GROUP_XO, STORAGE0, REPOSITORY_LEAF_XG, REPOSITORY_GROUP_XB);
        createGroup(REPOSITORY_GROUP_XF, STORAGE0, REPOSITORY_GROUP_XC, REPOSITORY_LEAF_XD, REPOSITORY_LEAF_XL);
        createGroup(REPOSITORY_GROUP_XH, STORAGE0, REPOSITORY_GROUP_XF, REPOSITORY_LEAF_XK);

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_LEAF_XL).getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_LEAF_XG).getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_LEAF_XD).getAbsolutePath(),
                         "com.artifacts.to.update.releases:update-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_LEAF_XK).getAbsolutePath(),
                         "com.artifacts.to.update.releases:update-group",
                         new String[]{ "1.2.1" }
        );

        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_XL);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_XG);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_XD);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_XK);

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
        createRoutingRuleSet(REPOSITORY_GROUP_XH,
                             new String[]{ REPOSITORY_LEAF_XD },
                             ".*(com|org)/artifacts/to/update/releases/update-group.*",
                             ROUTING_RULE_TYPE_DENIED);

        rebuildIndexes(getRepositories());
    }

    @Test
    public void artifactDeletionShouldDeleteArtifactFromParentGroupRepositoryIndex()
            throws Exception
    {

        String artifactPath = "com/artifacts/to/delete/releases/delete-group/1.2.1/delete-group-1.2.1.jar";

        String contextId = IndexContextHelper.getContextId(STORAGE0,
                                                           REPOSITORY_GROUP_XF,
                                                           IndexTypeEnum.LOCAL.getType());

        RepositoryIndexer indexer = repositoryIndexManager.get().getRepositoryIndexer(contextId);

        MutableRepository repository = mavenRepositoryFactory.createRepository(REPOSITORY_LEAF_XL);
        repository.setStorage(configurationManagementService.getMutableConfigurationClone().getStorage(STORAGE0));

        RepositoryPath artifactFile = repositoryPathResolver.resolve(new Repository(repository), artifactPath);

        indexer.addArtifactToIndex(artifactFile);

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  REPOSITORY_GROUP_XF,
                                                  "+g:com.artifacts.to.delete.releases +a:delete-group +v:1.2.1 +e:jar",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(artifactSearchService.contains(request));

        RepositoryFiles.delete(artifactFile, false);

        assertFalse(Files.exists(artifactFile), "Failed to delete artifact file " + artifactFile.toAbsolutePath());

        assertFalse(artifactSearchService.contains(request));
    }

}
