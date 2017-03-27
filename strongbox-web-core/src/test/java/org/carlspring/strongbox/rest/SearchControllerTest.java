package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Alex Oreshkevich
 * @author Martin Todorov
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class SearchControllerTest
        extends RestAssuredBaseTest
{

    private static final String STORAGE_SC_TEST = "storage-sc-test";

    private static final String REPOSITORY_RELEASES = "sc-releases-search";

    private static final File REPOSITORY_RELEASES_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/" + STORAGE_SC_TEST + "/" +
                                                                     REPOSITORY_RELEASES);

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Override
    public void init()
            throws Exception
    {
        super.init();

        // prepare storage: create it from Java code instead of putting <storage/> in strongbox.xml
        createStorage(STORAGE_SC_TEST);

        createRepository(STORAGE_SC_TEST, REPOSITORY_RELEASES, true);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR, "org.carlspring.strongbox.searches:test-project:1.0.11.3");
        generateArtifact(REPOSITORY_RELEASES_BASEDIR, "org.carlspring.strongbox.searches:test-project:1.0.11.3.1");
        generateArtifact(REPOSITORY_RELEASES_BASEDIR, "org.carlspring.strongbox.searches:test-project:1.0.11.3.2");

        final RepositoryIndexer repositoryIndexer = repositoryIndexManager.getRepositoryIndexer(STORAGE_SC_TEST + ":" +
                                                                                                REPOSITORY_RELEASES + ":" +
                                                                                                IndexTypeEnum.LOCAL.getType());

        assertNotNull(repositoryIndexer);

        repositoryManagementService.reIndex(STORAGE_SC_TEST, REPOSITORY_RELEASES, "org/carlspring/strongbox/searches");
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE_SC_TEST, REPOSITORY_RELEASES));

        return repositories;
    }

    @Test
    public void testSearches()
            throws Exception
    {
        String indexQuery = "g:org.carlspring.strongbox.searches a:test-project";
        String dbQuery = "groupId=org.carlspring.strongbox.searches;artifactId=test-project;";

        // testSearchPlainText
        String response = client.search(indexQuery, MediaType.TEXT_PLAIN_VALUE);

        assertTrue("Received unexpected response! \n" + response + "\n",
                   response.contains("test-project-1.0.11.3.jar") &&
                           response.contains("test-project-1.0.11.3.1.jar"));
        assertEquals("DB search response don't match!", response, client.search(dbQuery, MediaType.TEXT_PLAIN_VALUE));


        // testSearchJSON
        response = client.search(indexQuery, MediaType.APPLICATION_JSON_VALUE);

        assertTrue("Received unexpected response! \n" + response + "\n",
                   response.contains("\"version\" : \"1.0.11.3\"") &&
                           response.contains("\"version\" : \"1.0.11.3.1\""));
        assertEquals("DB search response don't match!", response, client.search(dbQuery, MediaType.APPLICATION_JSON_VALUE));
        
        // testSearchXML
         response = client.search(indexQuery, MediaType.APPLICATION_XML_VALUE);
        
         assertTrue("Received unexpected response! \n" + response + "\n",
         response.contains(">1.0.11.3<") && response.contains(">1.0.11.3.1<"));
         assertEquals("DB search response don't match!", response, client.search(dbQuery, MediaType.APPLICATION_XML_VALUE));
    }

}
