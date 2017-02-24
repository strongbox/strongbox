package org.carlspring.strongbox.services;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationWithIndexing;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class RepositoryManagementServiceImplTest
        extends TestCaseWithArtifactGenerationWithIndexing
{

    private static final String STORAGES_BASEDIR = ConfigurationResourceResolver.getVaultDirectory() + "/storages";


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Before
    public void setUp()
            throws Exception
    {
        createRepository(STORAGE0, "repository-management-releases-test-create-repository", false);

        createRepository(STORAGE0, "repository-management-releases-test-create-and-delete", false);

        createRepositoryWithArtifacts(STORAGE0,
                                      "repository-management-releases-test-merge-1",
                                      false,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "6.2.2");

        createRepositoryWithArtifacts(STORAGE0,
                                      "repository-management-releases-test-merge-2",
                                      false,
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
        repositories.add(mockRepositoryMock(STORAGE0, "repository-management-releases-test-create-repository"));
        repositories.add(mockRepositoryMock(STORAGE0, "repository-management-releases-test-create-and-delete"));
        repositories.add(mockRepositoryMock(STORAGE0, "repository-management-releases-test-merge-1"));
        repositories.add(mockRepositoryMock(STORAGE0, "repository-management-releases-test-merge-2"));

        return repositories;
    }

    @Test
    public void testCreateRepository()
            throws IOException, JAXBException
    {
        String repositoryId = "repository-management-releases-test-create-repository";

        File repositoryBaseDir = new File(STORAGES_BASEDIR, "storage0/" + repositoryId);

        assertTrue("Failed to create repository '" + repositoryId + "'!", repositoryBaseDir.exists());
    }

    @Test
    public void testCreateAndDelete()
            throws Exception
    {
        String repositoryId = "repository-management-releases-test-create-and-delete";

        File basedir = new File(STORAGES_BASEDIR + "/" + STORAGE0);
        File repositoryDir = new File(basedir, repositoryId);

        assertTrue("Failed to create the repository \"" + repositoryDir.getAbsolutePath() + "\"!", repositoryDir.exists());

        getRepositoryIndexManager().closeIndexer(STORAGE0 + ":" + repositoryId + ":local");

        getRepositoryManagementService().removeRepository(STORAGE0, repositoryId);

        assertFalse("Failed to remove the repository!", repositoryDir.exists());
    }

    @Test
    public void testMerge()
            throws Exception
    {
        String repositoryId1 = "repository-management-releases-test-merge-1";
        String repositoryId2 = "repository-management-releases-test-merge-2";

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  repositoryId1,
                                                  "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.2 +p:jar");

        assertTrue(artifactSearchService.contains(request));

        request = new SearchRequest(STORAGE0,
                                    repositoryId1,
                                    "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.3 +p:jar");

        assertFalse(artifactSearchService.contains(request));

        getRepositoryManagementService().mergeIndexes(STORAGE0, repositoryId2, STORAGE0, repositoryId1);

        request = new SearchRequest(STORAGE0,
                                    repositoryId1,
                                    "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.3 +p:jar");

        assertTrue("Failed to merge!", artifactSearchService.contains(request));
    }

}
