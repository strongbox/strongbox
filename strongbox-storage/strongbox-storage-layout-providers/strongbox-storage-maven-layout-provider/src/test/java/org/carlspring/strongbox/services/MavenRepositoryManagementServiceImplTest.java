package org.carlspring.strongbox.services;

import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.config.Maven2LayoutProviderConfig;
import org.carlspring.strongbox.config.StorageCoreConfig;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class MavenRepositoryManagementServiceImplTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{
    private static boolean initialized = false;

    private static final String STORAGES_BASEDIR = ConfigurationResourceResolver.getVaultDirectory() + "/storages";

    private static final String REPOSITORY_RELEASES_1 = "rmsi-releases-1";

    private static final String REPOSITORY_RELEASES_2 = "rmsi-releases-2";

    private static final String REPOSITORY_RELEASES_MERGE_1 = "rmsi-releases-merge-1";

    private static final String REPOSITORY_RELEASES_MERGE_2 = "rmsi-releases-merge-2";

    @org.springframework.context.annotation.Configuration
    @Import({ CommonConfig.class,
              StorageCoreConfig.class,
              Maven2LayoutProviderConfig.class })
    public static class SpringConfig { }

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @PostConstruct
    public void initialize()
            throws Exception
    {
        if (initialized)
        {
            return;
        }
        initialized = true;
        createRepository(STORAGE0, REPOSITORY_RELEASES_1, true);

        createRepository(STORAGE0, REPOSITORY_RELEASES_2, true);

        createRepositoryWithArtifacts(STORAGE0,
                                      REPOSITORY_RELEASES_MERGE_1,
                                      true,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "6.2.2");

        createRepositoryWithArtifacts(STORAGE0,
                                      REPOSITORY_RELEASES_MERGE_2,
                                      true,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "6.2.3");
    }

    @PreDestroy
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_MERGE_1));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_MERGE_2));

        return repositories;
    }

    @Test
    public void testCreateRepository()
            throws IOException, JAXBException
    {
        File repositoryBaseDir = new File(STORAGES_BASEDIR, STORAGE0 + "/" + REPOSITORY_RELEASES_1);

        assertTrue("Failed to create repository '" + REPOSITORY_RELEASES_1 + "'!", repositoryBaseDir.exists());
    }

    @Test
    public void testCreateAndDelete()
            throws Exception
    {
        File basedir = new File(STORAGES_BASEDIR + "/" + STORAGE0);
        File repositoryDir = new File(basedir, REPOSITORY_RELEASES_2);

        assertTrue("Failed to create the repository \"" + repositoryDir.getAbsolutePath() + "\"!", repositoryDir.exists());

        getRepositoryIndexManager().closeIndexer(STORAGE0 + ":" + REPOSITORY_RELEASES_2 + ":" + IndexTypeEnum.LOCAL.getType());

        getRepositoryManagementService().removeRepository(STORAGE0, REPOSITORY_RELEASES_2);

        assertFalse("Failed to remove the repository!", repositoryDir.exists());
    }

    @Test
    public void testMerge()
            throws Exception
    {
        // dumpIndex(STORAGE0, REPOSITORY_RELEASES_MERGE_1, IndexTypeEnum.LOCAL.getType());

        // 1) Check that an exists in the first repository
        SearchRequest request = new SearchRequest(STORAGE0,
                                                  REPOSITORY_RELEASES_MERGE_1,
                                                  "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.2 +p:jar",
                                                  MavenIndexerSearchProvider.ALIAS);

        artifactSearchService.search(request);

        assertTrue(artifactSearchService.contains(request));

        // 2) Check that the artifact which exists in the second repository does not exist
        //    in the index of the first repository.
        request = new SearchRequest(STORAGE0,
                                    REPOSITORY_RELEASES_MERGE_1,
                                    "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.3 +p:jar",
                                    MavenIndexerSearchProvider.ALIAS);

        assertFalse(artifactSearchService.contains(request));

        MavenRepositoryFeatures features = (MavenRepositoryFeatures) getFeatures(STORAGE0, REPOSITORY_RELEASES_MERGE_2);

        // 3) Merge the second repository into the first one
        features.mergeIndexes(STORAGE0,
                              REPOSITORY_RELEASES_MERGE_2,
                              STORAGE0,
                              REPOSITORY_RELEASES_MERGE_1);

        // 4) Check that the merged repository now has both artifacts
        request = new SearchRequest(STORAGE0,
                                    REPOSITORY_RELEASES_MERGE_1,
                                    "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.3 +p:jar",
                                    MavenIndexerSearchProvider.ALIAS);

        assertTrue("Failed to merge!", artifactSearchService.contains(request));
    }

}
