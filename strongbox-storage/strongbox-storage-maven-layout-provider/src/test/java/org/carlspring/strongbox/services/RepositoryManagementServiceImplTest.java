package org.carlspring.strongbox.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationAndIndexing;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class RepositoryManagementServiceImplTest
        extends TestCaseWithArtifactGenerationAndIndexing
{

    private static final String STORAGES_BASEDIR = ConfigurationResourceResolver.getVaultDirectory() + "/storages";

    public static final String REPOSITORY_RELEASES_1 = "rmsi-releases-1";

    public static final String REPOSITORY_RELEASES_2 = "rmsi-releases-2";

    public static final String REPOSITORY_RELEASES_MERGE_1 = "rmsi-releases-merge-1";

    public static final String REPOSITORY_RELEASES_MERGE_2 = "rmsi-releases-merge-2";


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
        SearchRequest request = new SearchRequest(STORAGE0,
                                                  REPOSITORY_RELEASES_MERGE_1,
                                                  "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.2 +p:jar");

        assertTrue(artifactSearchService.contains(request));

        request = new SearchRequest(STORAGE0,
                                    REPOSITORY_RELEASES_MERGE_1,
                                    "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.3 +p:jar");

        assertFalse(artifactSearchService.contains(request));

        getRepositoryManagementService().mergeIndexes(STORAGE0,
                                                      REPOSITORY_RELEASES_MERGE_2,
                                                      STORAGE0,
                                                      REPOSITORY_RELEASES_MERGE_1);

        request = new SearchRequest(STORAGE0,
                                    REPOSITORY_RELEASES_MERGE_1,
                                    "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.3 +p:jar");

        assertTrue("Failed to merge!", artifactSearchService.contains(request));
    }

}
