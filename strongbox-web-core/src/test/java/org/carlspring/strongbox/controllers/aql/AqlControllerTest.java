package org.carlspring.strongbox.controllers.aql;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

import java.util.LinkedHashSet;
import java.util.Set;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author sbespalov
 *
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class AqlControllerTest extends MavenRestAssuredBaseTest
{

    private static final String STORAGE_SC_TEST = "storage-sc-test";

    private static final String REPOSITORY_RELEASES = "sc-releases-search";

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

        createStorage(STORAGE_SC_TEST);

        MutableRepository repository = createRepository(STORAGE_SC_TEST, REPOSITORY_RELEASES,
                                                        RepositoryPolicyEnum.RELEASE.getPolicy(), true);

        generateArtifact(repository.getBasedir(), "org.carlspring.strongbox.searches:test-project:1.0.11.3:jar");
        generateArtifact(repository.getBasedir(), "org.carlspring.strongbox.searches:test-project:1.0.11.3.1:jar");
        generateArtifact(repository.getBasedir(), "org.carlspring.strongbox.searches:test-project:1.0.11.3.2:jar");

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

}
