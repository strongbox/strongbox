package org.carlspring.strongbox.controllers.aql;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;



/**
 * @author sbespalov
 * @author Pablo Tirado
 */
@IntegrationTest
public class AqlControllerTest
        extends MavenRestAssuredBaseTest
{

    private static final String A1 = "org/carlspring/strongbox/searches/test-project/1.0.11.3/test-project-1.0.11.3.jar";

    private static final String A2 = "org/carlspring/strongbox/searches/test-project/1.0.11.3.1/test-project-1.0.11.3.1.jar";

    private static final String A3 = "org/carlspring/strongbox/searches/test-project/1.0.11.3.2/test-project-1.0.11.3.2.jar";

    private static final String STORAGE_SC_TEST = "storage-sc-test";

    private static final String REPOSITORY_RELEASES = "sc-releases-search";

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl("/api/aql");
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testSearchExcludeVersion(@MavenRepository(storageId = STORAGE_SC_TEST,
                                                          repositoryId = REPOSITORY_RELEASES)
                                         Repository repository,
                                         @MavenTestArtifact(storageId = STORAGE_SC_TEST,
                                                            repositoryId = REPOSITORY_RELEASES,
                                                            resource = A1)
                                         Path artifact1,
                                         @MavenTestArtifact(storageId = STORAGE_SC_TEST,
                                                            repositoryId = REPOSITORY_RELEASES,
                                                            resource = A2)
                                         Path artifact2,
                                         @MavenTestArtifact(storageId = STORAGE_SC_TEST,
                                                            repositoryId = REPOSITORY_RELEASES,
                                                            resource = A3)
                                         Path artifact3)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String url = getContextBaseUrl();
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .queryParam("query",
                           String.format(
                                   "storage:%s+repository:%s+groupId:org.carlspring.strongbox.searches+!version:1.0.11.3.1",
                                   storageId,
                                   repositoryId))
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               // we should have 4 results: 2xjar + 2xpom
               .body("artifact", hasSize(4));
    }

    @Test
    public void testBadAqlSyntaxRequest()
    {
        String url = getContextBaseUrl();
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .queryParam("query",
                           String.format(
                                   "storage:%s+repository:%s+groupId:org.carlspring.strongbox.searches-version:1.0.11.3.1",
                                   STORAGE_SC_TEST, REPOSITORY_RELEASES))
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("error", containsString("[1:103]"));
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testSearchValidMavenCoordinates(@MavenRepository(storageId = STORAGE_SC_TEST,
                                                                 repositoryId = REPOSITORY_RELEASES)
                                                Repository repository,
                                                @MavenTestArtifact(storageId = STORAGE_SC_TEST,
                                                                   repositoryId = REPOSITORY_RELEASES,
                                                                   resource = A1)
                                                Path artifact1,
                                                @MavenTestArtifact(storageId = STORAGE_SC_TEST,
                                                                   repositoryId = REPOSITORY_RELEASES,
                                                                   resource = A2)
                                                Path artifact2,
                                                @MavenTestArtifact(storageId = STORAGE_SC_TEST,
                                                                   repositoryId = REPOSITORY_RELEASES,
                                                                   resource = A3)
                                                Path artifact3)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String url = getContextBaseUrl();
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .queryParam("query",
                           String.format("storage:%s+repository:%s+layout:maven+groupId:org.carlspring.strongbox.*",
                                         storageId,
                                         repositoryId))
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("artifact", hasSize(6));
    }

    @Test
    public void testSearchInvalidMavenCoordinates()
    {
        String url = getContextBaseUrl();
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .queryParam("query", "layout:unknown-layout+id:org.carlspring.strongbox.*")
               .when()
               .get(url)
               .then()
               .log()
               .body()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("error", equalTo("Unknown layout [unknown-layout]."));
    }

}
