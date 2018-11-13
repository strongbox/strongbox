package org.carlspring.strongbox.controllers.aql;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

/**
 * @author sbespalov
 *
 */
@IntegrationTest
@ExtendWith(SpringExtension.class)
public class AqlControllerTest extends MavenRestAssuredBaseTest
{

    private static final String STORAGE_SC_TEST = "storage-sc-test";

    private static final String REPOSITORY_RELEASES = "sc-releases-search";

    @BeforeAll
    public static void cleanUp()
        throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Override
    @BeforeEach
    public void init()
        throws Exception
    {
        super.init();

        createStorage(STORAGE_SC_TEST);

        MutableRepository repository = createRepository(STORAGE_SC_TEST, REPOSITORY_RELEASES,
                                                        RepositoryPolicyEnum.RELEASE.getPolicy(), true);

        generateArtifact(repository.getBasedir(), "org.carlspring.strongbox.searches:test-project:1.0.11.3:jar");
        generateArtifact(repository.getBasedir(), "org.carlspring.strongbox.searches:test-project:1.0.11.3.1:jar");
        generateArtifact(repository.getBasedir(), "org.carlspring.strongbox.searches:test-project:1.0.11.3.2:jar");
    }

    @Override
    @AfterEach
    public void shutdown()
    {
        try
        {
            removeRepositories();
            cleanUp();
        }
        catch (Exception e)
        {
            throw new UndeclaredThrowableException(e);
        }

        super.shutdown();
    }

    private void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE_SC_TEST, REPOSITORY_RELEASES, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Test
    public void testSearchExcludeVersion()
        throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .queryParam("query",
                           String.format("storage:%s+repository:%s+groupId:org.carlspring.strongbox.searches+!version:1.0.11.3.1",
                                         STORAGE_SC_TEST, REPOSITORY_RELEASES))
               .when()
               .get(getContextBaseUrl() + "/api/aql")
               .then()
               .statusCode(HttpStatus.OK.value())
               // we should have 4 results: 2xjar + 2xpom
               .body("artifact", Matchers.hasSize(4));
    }

    @Test
    public void testBadAqlSyntaxRequest()
        throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .queryParam("query",
                           String.format("storage:%s+repository:%s+groupId:org.carlspring.strongbox.searches-version:1.0.11.3.1",
                                         STORAGE_SC_TEST, REPOSITORY_RELEASES))
               .when()
               .get(getContextBaseUrl() + "/api/aql")
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("error", Matchers.containsString("[1:103]"));
    }

    @Test
    public void testSearchValidMavenCoordinates()
            throws Exception {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("query", "layout:maven+groupId:org.carlspring.strongbox.*")
                .when()
                .get(getContextBaseUrl() + "/api/aql")
                .peek()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("artifact", Matchers.hasSize(6));
    }
    
    @Test
    public void testSearchInvalidMavenCoordinates()
        throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .queryParam("query", "layout:unknown-layout+id:org.carlspring.strongbox.*")
               .when()
               .get(getContextBaseUrl() + "/api/aql")
               .then()
               .log()
               .body()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("error", Matchers.equalTo("Unknown layout [unknown-layout]."));
    }
}
