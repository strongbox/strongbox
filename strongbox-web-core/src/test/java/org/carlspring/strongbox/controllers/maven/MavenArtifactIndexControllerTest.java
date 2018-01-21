package org.carlspring.strongbox.controllers.maven;

import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.xml.configuration.repository.MavenRepositoryConfiguration;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Throwables;
import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

/**
 * @author Kate Novik
 * @author Martin Todorov
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class MavenArtifactIndexControllerTest
        extends MavenRestAssuredBaseTest
{

    private final static String STORAGE_ID = "storage-indexing-tests";

    private static final String REPOSITORY_RELEASES_1 = "aict-releases-1";

    private static final String REPOSITORY_RELEASES_2 = "aict-releases-2";


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE_ID, REPOSITORY_RELEASES_1));
        repositories.add(createRepositoryMock(STORAGE_ID, REPOSITORY_RELEASES_2));

        return repositories;
    }

    @Override
    public void init()
            throws Exception
    {
        super.init();

        // prepare storage: create it from Java code instead of putting <storage/> in strongbox.xml
        createStorage(STORAGE_ID);

        // Used by:
        // - testRebuildIndexForRepositoryWithPath()
        // - testRebuildIndexForRepository()
        // - testRebuildIndexesInStorage()
        // - testRebuildIndexesInStorage()
        MavenRepositoryConfiguration mavenRepositoryConfiguration = new MavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(true);

        Repository repository1 = new Repository(REPOSITORY_RELEASES_1);
        repository1.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository1.setStorage(configurationManager.getConfiguration().getStorage(STORAGE_ID));
        repository1.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(repository1);

        // Used by testRebuildIndexesInStorage()
        Repository repository2 = new Repository(REPOSITORY_RELEASES_2);
        repository2.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository2.setStorage(configurationManager.getConfiguration().getStorage(STORAGE_ID));
        repository2.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(repository2);
    }

    @Override
    public void shutdown()
    {
        try
        {
            getRepositoryIndexManager().closeIndexersForRepository(STORAGE_ID, REPOSITORY_RELEASES_1);
            getRepositoryIndexManager().closeIndexersForRepository(STORAGE_ID, REPOSITORY_RELEASES_2);
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
        super.shutdown();
    }

    @Test
    public void testRebuildIndexForRepositoryWithPath()
            throws Exception
    {
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:1.0");
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:1.0:javadoc");
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:1.0:sources");
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:1.1");
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:1.1:jar:javadoc");
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:1.1:jar:sources");

        final String artifactPath = "org/carlspring/strongbox/indexes/strongbox-test";

        client.rebuildMetadata(STORAGE_ID, REPOSITORY_RELEASES_1, artifactPath);
        client.rebuildIndexes(STORAGE_ID, REPOSITORY_RELEASES_1, artifactPath);

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_1,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:1.0 +p:jar");

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_1,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:1.0 c:javadoc +p:jar");

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_1,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:1.0 c:sources +p:jar");

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_1,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:1.1 +p:jar");

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_1,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:1.1 c:javadoc +p:jar");

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_1,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:1.1 c:sources +p:jar");
    }

    @Test
    public void testRebuildIndexForRepository()
            throws Exception
    {
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_2).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:2.0");
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_2).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:2.1");

        client.rebuildMetadata(STORAGE_ID, REPOSITORY_RELEASES_2, null);
        client.rebuildIndexes(STORAGE_ID, REPOSITORY_RELEASES_2, null);

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_2,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:2.0 +p:jar");

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_2,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:2.1 +p:jar");
    }

    @Test
    public void testRebuildIndexesInStorage()
            throws Exception
    {
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:1.3");
        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_2).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test:2.3");

        client.rebuildMetadata(STORAGE_ID, null, null);
        client.rebuildIndexes(STORAGE_ID, null, null);

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_1,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:1.3 +p:jar");

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_2,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:2.3 +p:jar");
    }

    @Test
    public void shouldReturnBadRequestWhenIndexingIsNotEnabled()
            throws Exception
    {
        String url = getContextBaseUrl() + "/storages/public/public-group/.index/nexus-maven-repository-index.gz";

        given().get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldDownloadPackedIndex()
            throws Exception
    {
        client.rebuildIndexes("storage0", "releases", null);

        String url = getContextBaseUrl() + "/storages/storage0/releases/.index/nexus-maven-repository-index.gz";

        given().get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .contentType("application/x-gzip")
               .body(CoreMatchers.notNullValue());
    }

    @Test
    public void shouldDownloadIndexProperties()
            throws Exception
    {
        client.rebuildIndexes("storage0", "releases", null);

        String url = getContextBaseUrl() + "/storages/storage0/releases/.index/nexus-maven-repository-index.properties";

        given().get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .contentType("text/plain")
               .body(CoreMatchers.notNullValue());
    }
}
