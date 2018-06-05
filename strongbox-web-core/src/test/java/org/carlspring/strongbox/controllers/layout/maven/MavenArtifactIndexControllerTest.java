package org.carlspring.strongbox.controllers.layout.maven;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.xml.configuration.repository.MutableMavenRepositoryConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Throwables;

import io.restassured.http.Header;
import io.restassured.module.mockmvc.response.MockMvcResponse;

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

    @Inject
    private MavenRepositoryFeatures features;

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE_ID, REPOSITORY_RELEASES_1, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE_ID, REPOSITORY_RELEASES_2, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Before
    public void isIndexingEnabled()
    {
        Assume.assumeTrue(repositoryIndexManager.isPresent());
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
        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(true);

        MutableRepository repository1 = mavenRepositoryFactory.createRepository(REPOSITORY_RELEASES_1);
        repository1.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository1.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(STORAGE_ID, repository1);

        // Used by testRebuildIndexesInStorage()
        MutableRepository repository2 = mavenRepositoryFactory.createRepository(REPOSITORY_RELEASES_2);
        repository2.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository2.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(STORAGE_ID, repository2);
    }

    @Override
    public void shutdown()
    {
        try
        {
            closeIndexersForRepository(STORAGE_ID, REPOSITORY_RELEASES_1);
            closeIndexersForRepository(STORAGE_ID, REPOSITORY_RELEASES_2);
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
        MockMvcResponse mockMvcResponse = client.rebuildIndexes(STORAGE_ID, REPOSITORY_RELEASES_1, artifactPath);
        mockMvcResponse.then().statusCode(HttpStatus.OK.value());

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
    public void shouldNotBeAllowedToProvideAbsolutePaths()
            throws Exception
    {
        MockMvcResponse mockMvcResponse = client.rebuildIndexes(STORAGE_ID, REPOSITORY_RELEASES_2, "/");
        mockMvcResponse.then().body("error", CoreMatchers.equalTo("Only valid relative paths are allowed")).statusCode(
                HttpStatus.BAD_REQUEST.value());
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
        MockMvcResponse mockMvcResponse = client.rebuildIndexes(STORAGE_ID, REPOSITORY_RELEASES_2, null);
        mockMvcResponse.then().statusCode(HttpStatus.OK.value());

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
        MockMvcResponse mockMvcResponse = client.rebuildIndexes(STORAGE_ID, null, null);
        mockMvcResponse.then().statusCode(HttpStatus.OK.value());

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_1,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:1.3 +p:jar");

        assertIndexContainsArtifact(STORAGE_ID,
                                    REPOSITORY_RELEASES_2,
                                    "+g:org.carlspring.strongbox.indexes +a:strongbox-test +v:2.3 +p:jar");
    }

    @Test
    public void shouldReturnNotFoundWhenIndexingIsNotEnabled()
    {
        String url = getContextBaseUrl() + "/storages/public/maven-group/.index/nexus-maven-repository-index.gz";

        given().header(new Header("User-Agent", "Maven/*"))
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldDownloadPackedIndex()
            throws Exception
    {
        ((IndexedMavenRepositoryFeatures) features).pack(STORAGE_ID, REPOSITORY_RELEASES_1);

        String url = getContextBaseUrl() + "/storages/" + STORAGE_ID + "/" + REPOSITORY_RELEASES_1 +
                     "/.index/nexus-maven-repository-index.gz";

        given().header(new Header("User-Agent", "Maven/*"))
               .get(url)
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
        ((IndexedMavenRepositoryFeatures) features).pack(STORAGE_ID, REPOSITORY_RELEASES_1);

        String url = getContextBaseUrl() + "/storages/" + STORAGE_ID + "/" + REPOSITORY_RELEASES_1 +
                     "/.index/nexus-maven-repository-index.properties";

        given().header(new Header("User-Agent", "Maven/*"))
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .contentType("text/plain")
               .body(CoreMatchers.notNullValue());
    }
}
