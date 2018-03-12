package org.carlspring.strongbox.controllers.maven;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.generator.MavenArtifactDeployer;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.providers.search.OrientDbSearchProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Throwables;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Alex Oreshkevich
 * @author Martin Todorov
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class MavenSearchControllerTest
        extends MavenRestAssuredBaseTest
{

    private static final String STORAGE_SC_TEST = "storage-sc-test";

    private static final String REPOSITORY_RELEASES = "sc-releases-search";

    private static final Path GENERATOR_BASEDIR = Paths.get(ConfigurationResourceResolver.getVaultDirectory())
                                                       .resolve("local");


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

        cleanUp();
        
        // prepare storage: create it from Java code instead of putting <storage/> in strongbox.xml
        createStorage(STORAGE_SC_TEST);

        createRepository(STORAGE_SC_TEST, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy(), true);

        MavenArtifactDeployer artifactDeployer = buildArtifactDeployer(GENERATOR_BASEDIR);
        
        MavenArtifact a1 = generateArtifact(GENERATOR_BASEDIR.toString(), "org.carlspring.strongbox.searches:test-project:1.0.11.3");
        MavenArtifact a2 = generateArtifact(GENERATOR_BASEDIR.toString(), "org.carlspring.strongbox.searches:test-project:1.0.11.3.1");
        MavenArtifact a3 = generateArtifact(GENERATOR_BASEDIR.toString(), "org.carlspring.strongbox.searches:test-project:1.0.11.3.2");

        artifactDeployer.deploy(a1, STORAGE_SC_TEST, REPOSITORY_RELEASES);
        artifactDeployer.deploy(a2, STORAGE_SC_TEST, REPOSITORY_RELEASES);
        artifactDeployer.deploy(a3, STORAGE_SC_TEST, REPOSITORY_RELEASES);
        
        final RepositoryIndexer repositoryIndexer = repositoryIndexManager.getRepositoryIndexer(STORAGE_SC_TEST + ":" +
                                                                                                REPOSITORY_RELEASES + ":" +
                                                                                                IndexTypeEnum.LOCAL.getType());

        assertNotNull(repositoryIndexer);

        reIndex(STORAGE_SC_TEST, REPOSITORY_RELEASES, "org/carlspring/strongbox/searches");
    }

    @Override
    public void shutdown()
    {
        try
        {
            getRepositoryIndexManager().closeIndexersForRepository(STORAGE_SC_TEST, REPOSITORY_RELEASES);
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
        super.shutdown();
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE_SC_TEST, REPOSITORY_RELEASES));

        return repositories;
    }

    @Test
    public void testIndexSearches()
            throws Exception
    {
        testSearches("+g:org.carlspring.strongbox.searches +a:test-project",
                     MavenIndexerSearchProvider.ALIAS);
    }
    
    @Test
    public void testDbSearches()
            throws Exception
    {
        testSearches("groupId=org.carlspring.strongbox.searches;artifactId=test-project;",
                     OrientDbSearchProvider.ALIAS);
    }

    private void testSearches(String query,
                              String searchProvider)
            throws Exception
    {
        // testSearchPlainText
        String response = client.search(query, MediaType.TEXT_PLAIN_VALUE, searchProvider);

        assertTrue("Received unexpected search results! \n" + response + "\n",
                   response.contains("test-project-1.0.11.3.jar") &&
                   response.contains("test-project-1.0.11.3.1.jar"));

        // testSearchJSON
        response = client.search(query, MediaType.APPLICATION_JSON_VALUE, searchProvider);

        assertTrue("Received unexpected search results! \n" + response + "\n",
                   response.contains("\"version\" : \"1.0.11.3\"") &&
                   response.contains("\"version\" : \"1.0.11.3.1\""));
        
        // testSearchXML
        response = client.search(query, MediaType.APPLICATION_XML_VALUE, searchProvider);

        assertTrue("Received unexpected search results! \n" + response + "\n",
                   response.contains("\"1.0.11.3\"") &&
                   response.contains("\"1.0.11.3.1\""));
    }

    @Test
    public void testDumpIndex()
            throws Exception
    {

        // /storages/storage0/releases/.index/local
        // this index is present but artifacts are missing
        dumpIndex("storage0", "releases");

        // this index is not empty
        dumpIndex(STORAGE_SC_TEST, REPOSITORY_RELEASES);

        // this index is not present, and even storage is not present
        // just to make sure that dump method will not produce any exceptions
        dumpIndex("foo", "bar");
    }
}
