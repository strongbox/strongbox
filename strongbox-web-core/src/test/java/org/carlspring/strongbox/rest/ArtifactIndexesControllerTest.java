package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.booters.StorageBooter;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration.generateArtifact;
import static org.junit.Assert.assertTrue;

/**
 * @author Kate Novik.
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ArtifactIndexesControllerTest
        extends RestAssuredBaseTest
{

    private static final File REPOSITORY_BASEDIR_1 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage0/releases-one");

    private static final File REPOSITORY_BASEDIR_2 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage0/releases-two");

    private static final File REPOSITORY_BASEDIR_3 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage1/releases");

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_INDEXES = "org/carlspring/strongbox/indexes/strongbox-test-one";

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    private ArtifactSearchService artifactSearchService;

    @Inject
    private StorageBooter storageBooter;

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    private static boolean initialized;

    @Before
    public void setUp()
            throws Exception
    {
        if (!initialized)
        {
            super.init();

//            // Initialize indexes (for IDE launches)
//            if (repositoryIndexManager.getIndexes()
//                                      .isEmpty())
//            {
//                for (Storage storage : configurationManagementService.getConfiguration()
//                                                                     .getStorages()
//                                                                     .values())
//                {
//                    for (Repository repository : storage.getRepositories()
//                                                        .values())
//                    {
//                        storageBooter.reInitializeRepositoryIndex(storage.getId(), repository.getId());
//                    }
//                }
//            }

            // to remove previous generated artifacts if they are present
            removeDir(REPOSITORY_BASEDIR_1.getAbsolutePath() + "/org/carlspring/strongbox/indexes");
            removeDir(REPOSITORY_BASEDIR_2.getAbsolutePath() + "/org/carlspring/strongbox/indexes");
            removeDir(REPOSITORY_BASEDIR_3.getAbsolutePath() + "/org/carlspring/strongbox/indexes");


            Repository repository1 = new Repository("releases-one");
            repository1.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
            Storage storage = configurationManagementService.getStorage("storage0");
            repository1.setStorage(storage);
            storage.addOrUpdateRepository(repository1);
            repositoryManagementService.createRepository("storage0", "releases-one");

            //Create released artifact
            String ga1 = "org.carlspring.strongbox.indexes:strongbox-test-one::jar";
            generateArtifact(REPOSITORY_BASEDIR_1.getAbsolutePath(), ga1, "1.0");

            //Create released artifact
            String ga2 = "org.carlspring.strongbox.indexes:strongbox-test-two::jar";
            generateArtifact(REPOSITORY_BASEDIR_1.getAbsolutePath(), ga2, "1.0");

            Repository repository2 = new Repository("releases-two");
            repository2.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
            repository2.setStorage(storage);
            storage.addOrUpdateRepository(repository2);
            repositoryManagementService.createRepository("storage0", "releases-two");

            //Create released artifact
            generateArtifact(REPOSITORY_BASEDIR_2.getAbsolutePath(), ga1, "1.0");

            //Create storage and repository for testing rebuild metadata in storages
            Storage newStorage = new Storage("storage1");
            Repository repository3 = new Repository("releases");
            repository3.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
            repository3.setStorage(newStorage);
            configurationManagementService.addOrUpdateStorage(newStorage);
            newStorage.addOrUpdateRepository(repository3);
            repositoryManagementService.createRepository("storage1", "releases");

            //Create released artifact
            generateArtifact(REPOSITORY_BASEDIR_3.getAbsolutePath(), ga1, "1.0");

            initialized = true;
        }
    }

    @Test
    public void testRebuildArtifactsIndexes()
            throws Exception
    {
        client.rebuildIndexes("storage0", "releases-one", ARTIFACT_BASE_PATH_STRONGBOX_INDEXES);


        SearchRequest request = new SearchRequest("storage0",
                                                  "releases-one",
                                                  "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request));
    }

    @Test
    public void testRebuildIndexesInRepository()
            throws Exception
    {
        client.rebuildIndexes("storage0", "releases-one", null);

        SearchRequest request1 = new SearchRequest("storage0",
                                                   "releases-one",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest("storage0",
                                                   "releases-one",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-two +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request2));

    }

    @Test
    public void testRebuildIndexesInStorage()
            throws Exception
    {
        client.rebuildIndexes("storage0");

        SearchRequest request1 = new SearchRequest("storage0",
                                                   "releases-one",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-two +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest("storage0",
                                                   "releases-two",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request2));
    }

    @Test
    public void testRebuildIndexesInStorages()
            throws Exception
    {
        client.rebuildIndexes();


        SearchRequest request1 = new SearchRequest("storage0",
                                                   "releases-one",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest("storage1",
                                                   "releases",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request2));

    }

}
