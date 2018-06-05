package org.carlspring.strongbox.controllers.layout.maven;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.providers.search.OrientDbSearchProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Throwables;

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
                                                       .resolve("local")
                                                       .resolve(STORAGE_SC_TEST)
                                                       .resolve(REPOSITORY_RELEASES);


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

        MutableRepository repository = createRepository(STORAGE_SC_TEST, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy(), true);

        generateArtifact(repository.getBasedir(), "org.carlspring.strongbox.searches:test-project:1.0.11.3:jar");
        generateArtifact(repository.getBasedir(), "org.carlspring.strongbox.searches:test-project:1.0.11.3.1:jar");
        generateArtifact(repository.getBasedir(), "org.carlspring.strongbox.searches:test-project:1.0.11.3.2:jar");

        if (repositoryIndexManager.isPresent())
        {
            final RepositoryIndexer repositoryIndexer = repositoryIndexManager.get()
                                                                              .getRepositoryIndexer(STORAGE_SC_TEST + ":" +
                                                                                                    REPOSITORY_RELEASES + ":" +
                                                                                                    IndexTypeEnum.LOCAL.getType());

            assertNotNull(repositoryIndexer);

            reIndex(STORAGE_SC_TEST, REPOSITORY_RELEASES, "org/carlspring/strongbox/searches");
        }
    }

    @Override
    public void shutdown()
    {
        try
        {
            closeIndexersForRepository(STORAGE_SC_TEST, REPOSITORY_RELEASES);
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
        
        super.shutdown();
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE_SC_TEST, REPOSITORY_RELEASES, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Test
    public void testIndexSearches()
            throws Exception
    {
        Assume.assumeTrue(repositoryIndexManager.isPresent());

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
    }

    @Test
    public void testDumpIndex()
            throws Exception
    {
        Assume.assumeTrue(repositoryIndexManager.isPresent());

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
