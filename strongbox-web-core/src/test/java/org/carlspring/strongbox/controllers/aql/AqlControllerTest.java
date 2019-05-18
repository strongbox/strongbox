package org.carlspring.strongbox.controllers.aql;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates.LAYOUT_NAME;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.nio.file.Path;

import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
/**
 * @author sbespalov
 *
 */
@IntegrationTest
@Execution(SAME_THREAD)
public class AqlControllerTest extends MavenRestAssuredBaseTest
{

    private static final String A3 = "org/carlspring/strongbox/searches/test-project/1.0.11.3.2/test-project-1.0.11.3.2.jar";

    private static final String A2 = "org/carlspring/strongbox/searches/test-project/1.0.11.3.1/test-project-1.0.11.3.1.jar";

    private static final String A1 = "org/carlspring/strongbox/searches/test-project/1.0.11.3/test-project-1.0.11.3.jar";

    private static final String S1 = "storage-sc-test";

    private static final String R1 = "sc-releases-search";
    
    @Test
    @ExtendWith({RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class})
    public void testSearchExcludeVersion(@TestRepository(storageId = S1, repositoryId = R1, layout = LAYOUT_NAME) Repository repository,
                                         @TestArtifact(storageId = S1, repositoryId = R1, resource = A1, generator = MavenArtifactGenerator.class) Path artifact1,
                                         @TestArtifact(storageId = S1, repositoryId = R1, resource = A2, generator = MavenArtifactGenerator.class) Path artifact2,
                                         @TestArtifact(storageId = S1, repositoryId = R1, resource = A3, generator = MavenArtifactGenerator.class) Path artifact3)
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .queryParam("query",
                           String.format("storage:%s+repository:%s+groupId:org.carlspring.strongbox.searches+!version:1.0.11.3.1",
                                         S1, R1))
               .when()
               .get(getContextBaseUrl() + "/api/aql")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               // we should have 4 results: 2xjar + 2xpom
               .body("artifact", Matchers.hasSize(4));
    }

    @Test
    public void testBadAqlSyntaxRequest()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .queryParam("query",
                           String.format("storage:%s+repository:%s+groupId:org.carlspring.strongbox.searches-version:1.0.11.3.1",
                                         S1, R1))
               .when()
               .get(getContextBaseUrl() + "/api/aql")
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("error", Matchers.containsString("[1:103]"));
    }

    @Test
    @ExtendWith({RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class})
    public void testSearchValidMavenCoordinates(@TestRepository(storageId = S1, repositoryId = R1, layout = LAYOUT_NAME) Repository repository,
                                                @TestArtifact(storageId = S1, repositoryId = R1, resource = A1, generator = MavenArtifactGenerator.class) Path artifact1,
                                                @TestArtifact(storageId = S1, repositoryId = R1, resource = A2, generator = MavenArtifactGenerator.class) Path artifact2,
                                                @TestArtifact(storageId = S1, repositoryId = R1, resource = A3, generator = MavenArtifactGenerator.class) Path artifact3)
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("query", String.format("storage:%s+repository:%s+layout:maven+groupId:org.carlspring.strongbox.*", S1, R1))
                .when()
                .get(getContextBaseUrl() + "/api/aql")
                .peek()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("artifact", Matchers.hasSize(6));
    }
    
    @Test
    public void testSearchInvalidMavenCoordinates()
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
