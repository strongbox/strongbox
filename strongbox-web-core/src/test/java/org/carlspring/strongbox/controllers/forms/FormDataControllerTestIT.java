package org.carlspring.strongbox.controllers.forms;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
public class FormDataControllerTestIT
        extends RestAssuredBaseTest
{

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl("/api/formData");
    }

    @Test
    public void testGetUserFields()
    {
        String url = getContextBaseUrl() + "/userFields";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetStorageFields()
    {
        String url = getContextBaseUrl() + "/storageFields";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetStorageNames()
    {
        String url = getContextBaseUrl() + "/storageNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetStorageNamesFilteredByTerm()
    {
        String url = getContextBaseUrl() + "/storageNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("term", "prox")
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetRepositoryNamesFilteredByStorageId()
    {
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("storageId", STORAGE0)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetRepositoryNamesFilteredStorageIdAndSearchTerm()
    {
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("storageId", STORAGE0)
               .param("term", "SHOT")
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetRepositoryNamesWithStorageId()
    {
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("withStorageId", true)
               .when()
               .get(url)
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
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("withStorageId", true)
               .param("storageId", STORAGE0)
               .when()
               .get(url)
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
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("withStorageId", true)
               .param("storageId", STORAGE0)
               .param("term", "sna")
               .when()
               .get(url)
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
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("withStorageId", true)
               .param("storageId", STORAGE0)
               .param("type", RepositoryTypeEnum.HOSTED.getType())
               .when()
               .get(url)
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
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("withStorageId", true)
               .param("storageId", STORAGE0)
               .param("type", RepositoryTypeEnum.HOSTED.getType())
               .param("term", "sn")
               .when()
               .get(url)
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
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("withStorageId", true)
               .param("storageId", "storage-common-proxies")
               .param("type", RepositoryTypeEnum.GROUP.getType())
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetRepositoryNamesWithStorageIdFilteredByStorageIdTermAndTypeGroup()
    {
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("withStorageId", true)
               .param("storageId", "storage-common-proxies")
               .param("type", RepositoryTypeEnum.GROUP.getType())
               .param("term", "group-com")
               .when()
               .get(url)
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
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("withStorageId", true)
               .param("storageId", STORAGE0)
               .param("type", RepositoryTypeEnum.PROXY.getType())
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(equalTo(0)));

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("withStorageId", true)
               .param("storageId", "storage-common-proxies")
               .param("type", RepositoryTypeEnum.PROXY.getType())
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetRepositoryNamesWithStorageIdFilteredByLayout()
    {
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("withStorageId", true)
               .param("layout", MavenArtifactCoordinates.LAYOUT_NAME)
               .when()
               .get(url)
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
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("withStorageId", true)
               .param("storageId", "storage-common-proxies")
               .param("type", RepositoryTypeEnum.PROXY.getType())
               .param("term", "maven")
               .when()
               .get(url)
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
        String url = getContextBaseUrl() + "/repositoryNamesInGroupRepositories";
        mockMvc
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(url)
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
        String url = getContextBaseUrl() + "/repositoryNamesInGroupRepositories";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("term", "car")
               .when()
               .get(url)
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
        String url = getContextBaseUrl() + "/repositoryNamesInGroupRepositories";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("storageId", "storage-common-proxies")
               .when()
               .get(url)
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
        String url = getContextBaseUrl() + "/repositoryNamesInGroupRepositories";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("storageId", "public")
               .param("groupRepositoryId", "maven-group")
               .when()
               .get(url)
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
        String url = getContextBaseUrl() + "/repositoryNamesInGroupRepositories";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("storageId", "storage-common-proxies")
               .param("groupRepositoryId", "group-common-proxies")
               .param("term", "car")
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(equalTo(1)));
    }

}
