package org.carlspring.strongbox.controllers.forms;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@Transactional
public class FormDataControllerTestIT
        extends RestAssuredBaseTest
{

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl(getContextBaseUrl() + "/api/formData");
    }

    @Test
    public void testGetUserFields()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/userFields")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetStorageFields()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/storageFields")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetStorageNames()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/storageNames")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetStorageNamesFilteredByTerm()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/storageNames?term=prox")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetRepositoryNamesFilteredByStorageId()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/repositoryNames?storageId=storage0")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetRepositoryNamesFilteredStorageIdAndSearchTerm()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/repositoryNames?storageId=storage0&term=SHOT")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetRepositoryNamesWithStorageId()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/repositoryNames?withStorageId=true")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThan(0)))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(hasItem("public:maven-group")));
    }

    @Test
    public void testGetRepositoryNamesWithStorageIdFilteredByStorageId()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/repositoryNames?withStorageId=true&storageId=storage0")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThan(0)))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(hasItem("storage0:releases")));
    }

    @Test
    public void testGetRepositoryNamesWithStorageIdFilteredByStorageIdAndTerm()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/repositoryNames?withStorageId=true&storageId=storage0&term=sna")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThan(0)))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(hasItem("storage0:snapshots")));
    }

    @Test
    public void testGetRepositoryNamesWithStorageIdFilteredByByStorageIdAndTypeHosted()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/repositoryNames?withStorageId=true&type=hosted&storageId=storage0")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThanOrEqualTo(2)))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(hasItem("storage0:snapshots")))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(hasItem("storage0:releases")));
    }

    @Test
    public void testGetRepositoryNamesWithStorageIdFilteredByStorageIdTermAndTypeHosted()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/repositoryNames?withStorageId=true&type=hosted&storageId=storage0&term=sn")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThan(0)))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(hasItem("storage0:snapshots")))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values")
                                            .value(not(hasItem("storage0:releases"))));

    }

    @Test
    public void testGetRepositoryNamesWithStorageIdFilteredByStorageIdAndTypeGroup()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() +
                    "/repositoryNames?withStorageId=true&type=group&storageId=storage-common-proxies")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetRepositoryNamesWithStorageIdFilteredByStorageIdTermAndTypeGroup()
    {

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() +
                    "/repositoryNames?withStorageId=true&type=group&storageId=storage-common-proxies&term=group-com")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThan(0)))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values")
                                            .value(hasItem("storage-common-proxies:group-common-proxies")))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values")
                                            .value(not(hasItem("storage-common-proxies:maven-oracle"))));
    }

    @Test
    public void testGetRepositoryNamesWithStorageIdFilteredByStorageIdAndTypeProxy()
    {
        // storage0 has no proxies.
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/repositoryNames?withStorageId=true&type=proxy&storageId=storage0")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(equalTo(0)));

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() +
                    "/repositoryNames?withStorageId=true&type=proxy&storageId=storage-common-proxies")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetRepositoryNamesWithStorageIdFilteredByLayout()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/repositoryNames?withStorageId=true&layout=" +
                    MavenArtifactCoordinates.LAYOUT_NAME)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThan(0)))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(hasItem("storage-common-proxies:group-common-proxies")))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(not(hasItem("storage-nuget:nuget.org"))))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(not(hasItem("storage-pypi:pypi-releases"))))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(not(hasItem("storage-npm:npm-releases"))));
    }

    @Test
    public void testGetRepositoryNamesWithStorageIdFilteredByStorageIdTermAndTypeProxy()
    {

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() +
                    "/repositoryNames?withStorageId=true&type=proxy&storageId=storage-common-proxies&term=maven")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThanOrEqualTo(2)))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values")
                                            .value(hasItem("storage-common-proxies:maven-central")))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values")
                                            .value(not(hasItem("storage-common-proxies:group-common-proxies"))));

    }

    @Test
    public void testGetRepositoryNamesInGroup()
    {
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(getContextBaseUrl() + "/repositoryNamesInGroupRepositories")
                .peek()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("formDataValues", notNullValue())
                .body("formDataValues[0].values", hasSize(greaterThan(1)))
                .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values")
                                             .value(hasItem("springsource-snapshots")));
    }

    @Test
    public void testGetRepositoryNamesInGroupFilteredByTerm()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/repositoryNamesInGroupRepositories?term=car")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThan(0)))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(hasItem("carlspring")));
    }


    @Test
    public void testGetRepositoryNamesInGroupFilteredByStorageId()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() +
                    "/repositoryNamesInGroupRepositories?storageId=storage-common-proxies")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThan(0)))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values")
                                            .value(hasItem("carlspring")));
    }

    @Test
    public void testGetRepositoryNamesInGroupFilteredByStorageIdAndGroupRepositoryId()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() +
                    "/repositoryNamesInGroupRepositories?storageId=public&groupRepositoryId=maven-group")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThan(0)))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(hasItem("storage0:releases")))
               // randomly picked repository which is not part of the maven-group repositories collection to verify
               // we're not listing just "any" collection of repositories but only the ones from `maven-group`.
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(not(hasItem("nuget-group"))));
    }

    @Test
    public void testGetRepositoryNamesInGroupFilteredByStorageIdAndGroupRepositoryIdAndTerm()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() +
                    "/repositoryNamesInGroupRepositories?storageId=storage-common-proxies&groupRepositoryId=group-common-proxies&term=car")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(equalTo(1)));
    }

}
