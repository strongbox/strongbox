package org.carlspring.strongbox.controllers.aql;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

import java.util.LinkedHashSet;
import java.util.Set;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.junit.Assume;
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

        // prepare storage: create it from Java code instead of putting
        // <storage/> in strongbox.xml
        createStorage(STORAGE_SC_TEST);

        Repository repository = createRepository(STORAGE_SC_TEST, REPOSITORY_RELEASES,
                                                 RepositoryPolicyEnum.RELEASE.getPolicy(), true);

        generateArtifact(repository.getBasedir(), "org.carlspring.strongbox.searches:test-project:1.0.11.3:jar");
        generateArtifact(repository.getBasedir(), "org.carlspring.strongbox.searches:test-project:1.0.11.3.1:jar");
        generateArtifact(repository.getBasedir(), "org.carlspring.strongbox.searches:test-project:1.0.11.3.2:jar");

    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE_SC_TEST, REPOSITORY_RELEASES));

        return repositories;
    }

    @Test
    public void testCommonSearch()
        throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .queryParam("query",
                           String.format("storage:%s+repository:%s+groupId:org.carlspring.strongbox.searches",
                                         STORAGE_SC_TEST, REPOSITORY_RELEASES))
               .when()
               .get(getContextBaseUrl() + "/api/aql")
               .then()
               .statusCode(HttpStatus.OK.value());
    }

}
