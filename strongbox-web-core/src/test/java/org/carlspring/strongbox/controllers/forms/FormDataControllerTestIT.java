package org.carlspring.strongbox.controllers.forms;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.ProxyRepositorySetup;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.hamcrest.Matchers.*;

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

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testGetRepositoryNamesFilteredByStorageId(
            @TestRepository(layout = RawLayoutProvider.ALIAS,
                    repositoryId = "fdctit-tgrnfbsi-repository",
                    storageId = "fdctit-tgrnfbsi-storage",
                    setup = ProxyRepositorySetup.class)
                    Repository repository)
    {
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("storageId", repository.getStorage().getId())
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(hasItem(repository.getId())));
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testGetRepositoryNamesFilteredStorageIdAndSearchTerm(
            @TestRepository(layout = RawLayoutProvider.ALIAS,
                    repositoryId = "fdctit-tgrnfsiast-repository",
                    storageId = "fdctit-tgrnfsiast-storage",
                    setup = ProxyRepositorySetup.class)
                    Repository repository)
    {
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("storageId", repository.getStorage().getId())
               .param("term", "tgrnfsiast")
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

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testGetRepositoryNamesWithStorageIdFilteredByStorageId(
            @TestRepository(layout = RawLayoutProvider.ALIAS,
                    repositoryId = "fdctit-tgrnwsifbsi-repository",
                    storageId = "fdctit-tgrnwsifbsi-storage",
                    setup = ProxyRepositorySetup.class)
                    Repository repository)
    {
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("withStorageId", true)
               .param("storageId", repository.getStorage().getId())
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThan(0)))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(
                       hasItem(repository.getStorage().getId() + ":" + repository.getId())));
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testGetRepositoryNamesWithStorageIdFilteredByStorageIdAndTerm(
            @TestRepository(layout = RawLayoutProvider.ALIAS,
                    repositoryId = "fdctit-tgrnwsifbsiat-repository",
                    storageId = "fdctit-tgrnwsifbsiat-storage",
                    setup = ProxyRepositorySetup.class)
                    Repository repository)
    {
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("withStorageId", true)
               .param("storageId", repository.getStorage().getId())
               .param("term", "tgrnw")
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .body("formDataValues[0].values", hasSize(greaterThan(0)))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(
                       hasItem(repository.getStorage().getId() + ":" + repository.getId())));
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
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

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
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

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testGetRepositoryNamesWithStorageIdFilteredByStorageIdAndTypeProxy(
            @TestRepository(layout = RawLayoutProvider.ALIAS,
                    repositoryId = "fdctit-tgrnwsifbsatp-repository",
                    storageId = "fdctit-tgrnwsifbsatp-storage",
                    setup = ProxyRepositorySetup.class)
                    Repository repository)
    {
        String url = getContextBaseUrl() + "/repositoryNames";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .param("withStorageId", true)
               .param("storageId", repository.getStorage().getId())
               .param("type", RepositoryTypeEnum.PROXY.getType())
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("formDataValues", notNullValue())
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(
                       hasItem(repository.getStorage().getId() + ":" + repository.getId())));

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
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(
                       hasItem("storage-common-proxies:group-common-proxies")))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(
                       not(hasItem("storage-nuget:nuget.org"))))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(
                       not(hasItem("storage-pypi:pypi-releases"))))
               .expect(MockMvcResultMatchers.jsonPath("formDataValues[0].values").value(
                       not(hasItem("storage-npm:npm-releases"))));
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

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
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
