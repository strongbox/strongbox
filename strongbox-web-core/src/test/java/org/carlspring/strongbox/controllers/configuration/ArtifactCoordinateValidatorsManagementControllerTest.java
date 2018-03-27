package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.validation.deployment.RedeploymentValidator;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.controllers.configuration.ArtifactCoordinateValidatorsManagementController.*;
import static org.hamcrest.Matchers.*;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 * @author Aditya Srinivasan
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class ArtifactCoordinateValidatorsManagementControllerTest
        extends MavenRestAssuredBaseTest
{

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;

    @Inject
    private RedeploymentValidator redeploymentValidator;

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, "releases-with-single-validator"));
        repositories.add(createRepositoryMock(STORAGE0, "releases-with-default-validators"));
        repositories.add(createRepositoryMock(STORAGE0, "another-releases-with-default-validators"));
        repositories.add(createRepositoryMock(STORAGE0, "single-validator-only"));

        return repositories;
    }

    @Override
    public void init()
            throws Exception
    {
        super.init();

        Repository repository1 = mavenRepositoryFactory.createRepository(STORAGE0, "releases-with-single-validator");
        repository1.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository1.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repository1.setArtifactCoordinateValidators(
                new LinkedHashSet<>(Collections.singletonList(redeploymentValidator.getAlias())));

        createRepository(repository1);

        Repository repository2 = mavenRepositoryFactory.createRepository(STORAGE0, "releases-with-default-validators");
        repository2.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());

        createRepository(repository2);

        Repository repository3 = mavenRepositoryFactory.createRepository(STORAGE0,
                                                                         "another-releases-with-default-validators");
        repository3.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());

        createRepository(repository3);

        Repository repository4 = new Repository("single-validator-only");
        repository4.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository4.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repository4.setArtifactCoordinateValidators(
                new LinkedHashSet<>(Collections.singletonList(redeploymentValidator.getAlias())));

        createRepository(repository4);

        setContextBaseUrl(getContextBaseUrl() + "/api/configuration/artifact-coordinate-validators");
    }

    @Test
    public void expectOneValidator()
    {
        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}";
        String repositoryId = "releases-with-single-validator";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, STORAGE0, repositoryId)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("versionValidators", containsInAnyOrder("redeployment-validator"));
    }

    @Test
    public void expectedThreeDefaultValidatorsForRepositoryWithDefaultValidators()
    {
        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}";
        String repositoryId = "another-releases-with-default-validators";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, STORAGE0, repositoryId)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("versionValidators",
                     containsInAnyOrder("maven-release-version-validator", "maven-snapshot-version-validator",
                                        "redeployment-validator"));
    }

    @Test
    public void shouldNotGetValidatorWithNoStorageFound()
    {
        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}";
        String storageId = "storage-not-found";
        String repositoryId = "releases-with-single-validator";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, storageId, repositoryId)
               .peek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body("message", equalTo(NOT_FOUND_STORAGE_MESSAGE));
    }

    @Test
    public void shouldNotGetValidatorWithNoRepositoryFound()
    {
        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}";
        String repositoryId = "releases-not-found";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, STORAGE0, repositoryId)
               .peek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body("message", equalTo(NOT_FOUND_REPOSITORY_MESSAGE));
    }

    @Test
    public void validatorsForReleaseRepositoryShouldBeRemovableAndFailSafeWithResponseInJson()
    {
        validatorsForReleaseRepositoryShouldBeRemovableAndFailSafe(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void validatorsForReleaseRepositoryShouldBeRemovableAndFailSafeWithResponseInText()
    {
        validatorsForReleaseRepositoryShouldBeRemovableAndFailSafe(MediaType.TEXT_PLAIN_VALUE);
    }

    private void validatorsForReleaseRepositoryShouldBeRemovableAndFailSafe(String acceptHeader)
    {
        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}/{alias}";
        String repositoryId = "releases-with-default-validators";
        String alias = "maven-snapshot-version-validator";

        given().header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .put(url, STORAGE0, repositoryId, alias)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_ADD));

        given().header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .delete(url, STORAGE0, repositoryId, alias)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_DELETE));

        url = getContextBaseUrl() + "/{storageId}/{repositoryId}";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, STORAGE0, repositoryId)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("versionValidators",
                     containsInAnyOrder("redeployment-validator", "maven-release-version-validator"));
    }

    @Test
    public void shouldNotRemoveAliasNotFoundWithResponseInJson()
    {
        shouldNotRemoveAliasNotFound(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void shouldNotRemoveAliasNotFoundWithResponseInText()
    {
        shouldNotRemoveAliasNotFound(MediaType.TEXT_PLAIN_VALUE);
    }

    private void shouldNotRemoveAliasNotFound(String acceptHeader)
    {
        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}/{alias}";
        String repositoryId = "releases-with-default-validators";
        String alias = "alias-not-found";

        given().header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .delete(url, STORAGE0, repositoryId, alias)
               .peek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body(containsString(NOT_FOUND_ALIAS_MESSAGE));
    }

    @Test
    public void validatorsForReleaseRepositoryShouldBeAdditableAndFailSafeWithResponseInJson()
    {
        validatorsForReleaseRepositoryShouldBeAdditableAndFailSafe(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void validatorsForReleaseRepositoryShouldBeAdditableAndFailSafeWithResponseInText()
    {
        validatorsForReleaseRepositoryShouldBeAdditableAndFailSafe(MediaType.TEXT_PLAIN_VALUE);
    }
    
    @Test
    public void getACollectionOfArtifactCoordinateValidators(){

        String url = getContextBaseUrl() + "/validators";
        
        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString("versionValdidators"),containsString("Maven 2"));
    }

    @Test
    public void getAArtifactCoordinateValidatorsForLayoutProvider(){

        String url = getContextBaseUrl() + "/validators";
        String layoutProvider = "Maven 2";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url,layoutProvider)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString("supportedLayoutProviders"),containsString(layoutProvider));
    }

    private void validatorsForReleaseRepositoryShouldBeAdditableAndFailSafe(String acceptHeader)
    {
        String urlList = getContextBaseUrl() + "/{storageId}/{repositoryId}";
        String urlAdd = getContextBaseUrl() + "/{storageId}/{repositoryId}/{alias}";
        String repositoryId = "releases-with-single-validator";
        String alias = "/maven-snapshot-version-validator";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(urlList, STORAGE0, repositoryId)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("versionValidators", containsInAnyOrder("redeployment-validator"));

        given().header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .put(urlAdd, STORAGE0, repositoryId, alias)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_ADD));

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(urlList, STORAGE0, repositoryId)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("versionValidators",
                     containsInAnyOrder("redeployment-validator", "maven-snapshot-version-validator"));

        given().header(HttpHeaders.ACCEPT, acceptHeader)
               .when()
               .put(urlAdd, STORAGE0, repositoryId, alias)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_ADD));

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(urlList, STORAGE0, repositoryId)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("versionValidators",
                     containsInAnyOrder("redeployment-validator", "maven-snapshot-version-validator"));
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

}
