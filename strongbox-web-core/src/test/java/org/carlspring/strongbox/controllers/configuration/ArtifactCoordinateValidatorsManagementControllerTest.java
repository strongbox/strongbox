package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.MavenRedeploymentValidatorRepositorySetup;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.carlspring.strongbox.controllers.configuration.ArtifactCoordinateValidatorsManagementController.NOT_FOUND_ALIAS_MESSAGE;
import static org.carlspring.strongbox.controllers.configuration.ArtifactCoordinateValidatorsManagementController.SUCCESSFUL_ADD;
import static org.carlspring.strongbox.controllers.configuration.ArtifactCoordinateValidatorsManagementController.SUCCESSFUL_DELETE;
import static org.carlspring.strongbox.web.RepositoryMethodArgumentResolver.NOT_FOUND_REPOSITORY_MESSAGE;
import static org.carlspring.strongbox.web.RepositoryMethodArgumentResolver.NOT_FOUND_STORAGE_MESSAGE;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;


/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 * @author Aditya Srinivasan
 */
@ActiveProfiles({ "test",
                  "ArtifactCoordinateValidatorsManagementControllerTest" })
@IntegrationTest
@Execution(SAME_THREAD)
public class ArtifactCoordinateValidatorsManagementControllerTest
        extends MavenRestAssuredBaseTest
{
    private final static String REPOSITORY_RELEASES_SINGLE_VALIDATOR = "releases-with-single-validator";

    private final static String REPOSITORY_RELEASES_DEFAULT_VALIDATORS = "releases-with-default-validators";

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl("/api/configuration/artifact-coordinate-validators");
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    void expectOneValidator(@MavenRepository(repositoryId = REPOSITORY_RELEASES_SINGLE_VALIDATOR,
                                             setup = MavenRedeploymentValidatorRepositorySetup.class)
                            Repository repository)
    {
        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}";

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, repository.getStorage().getId(), repository.getId())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("versionValidators", containsInAnyOrder("redeployment-validator"));
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    void expectedThreeDefaultValidatorsForRepositoryWithDefaultValidators(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DEFAULT_VALIDATORS)
            Repository repository)
            throws IOException
    {
        configurationManagementService.setRepositoryArtifactCoordinateValidators();

        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}";

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, repository.getStorage().getId(), repository.getId())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("versionValidators",
                     containsInAnyOrder("maven-release-version-validator",
                                        "maven-snapshot-version-validator",
                                        "redeployment-validator"));
    }

    @Test
    void shouldNotGetValidatorWithNoStorageFound()
    {
        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}";
        String storageId = "storage-not-found";
        String repositoryId = REPOSITORY_RELEASES_SINGLE_VALIDATOR;
        String message = String.format(NOT_FOUND_STORAGE_MESSAGE, storageId);

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, storageId, repositoryId)
               .peek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body("message", equalTo(message));
    }

    @Test
    void shouldNotGetValidatorWithNoRepositoryFound()
    {
        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}";
        String storageId = STORAGE0;
        String repositoryId = "releases-not-found";
        String message = String.format(NOT_FOUND_REPOSITORY_MESSAGE, storageId, repositoryId);

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, storageId, repositoryId)
               .peek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body("message", equalTo(message));
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void validatorsForReleaseRepositoryShouldBeRemovableAndFailSafe(
            String acceptHeader,
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DEFAULT_VALIDATORS)
            Repository repository)
            throws IOException
    {
        configurationManagementService.setRepositoryArtifactCoordinateValidators();

        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}/{alias}";
        String storageId = repository.getStorage().getId();
        String repositoryId = repository.getId();
        String alias = "test-validator";

        mockMvc.accept(acceptHeader)
               .when()
               .put(url, storageId, repositoryId, alias)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_ADD));

        mockMvc.accept(acceptHeader)
               .when()
               .delete(url, storageId, repositoryId, alias)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_DELETE));

        url = getContextBaseUrl() + "/{storageId}/{repositoryId}";

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, storageId, repositoryId)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("versionValidators",
                     containsInAnyOrder("maven-release-version-validator",
                                        "maven-snapshot-version-validator",
                                        "redeployment-validator"));
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void shouldNotRemoveAliasNotFound(String acceptHeader,
                                      @MavenRepository(repositoryId = REPOSITORY_RELEASES_DEFAULT_VALIDATORS)
                                      Repository repository)
            throws IOException
    {
        configurationManagementService.setRepositoryArtifactCoordinateValidators();

        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}/{alias}";
        String storageId = repository.getStorage().getId();
        String repositoryId = repository.getId();
        String alias = "alias-not-found";

        mockMvc.accept(acceptHeader)
               .when()
               .delete(url, storageId, repositoryId, alias)
               .peek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body(containsString(NOT_FOUND_ALIAS_MESSAGE));
    }

    @Test
    public void getCollectionOfArtifactCoordinateValidators()
    {
        String url = getContextBaseUrl() + "/validators";

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString("versionValidators"), containsString(Maven2LayoutProvider.ALIAS));
    }

    @Test
    public void getArtifactCoordinateValidatorsForLayoutProvider()
    {
        String url = getContextBaseUrl() + "/validators";
        String layoutProvider = Maven2LayoutProvider.ALIAS;

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, layoutProvider)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString("supportedLayoutProviders"), containsString(layoutProvider));
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void validatorsForReleaseRepositoryShouldBeAddableAndFailSafe(String acceptHeader,
                                                                  @MavenRepository(repositoryId = REPOSITORY_RELEASES_SINGLE_VALIDATOR,
                                                                                   setup = MavenRedeploymentValidatorRepositorySetup.class)
                                                                  Repository repository)
    {
        String urlList = getContextBaseUrl() + "/{storageId}/{repositoryId}";
        String urlAdd = getContextBaseUrl() + "/{storageId}/{repositoryId}/{alias}";
        String storageId = repository.getStorage().getId();
        String repositoryId = repository.getId();
        String alias = "maven-snapshot-version-validator";

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(urlList, storageId, repositoryId)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("versionValidators", containsInAnyOrder("redeployment-validator"));

        mockMvc.accept(acceptHeader)
               .when()
               .put(urlAdd, storageId, repositoryId, alias)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_ADD));

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(urlList, storageId, repositoryId)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("versionValidators",
                     containsInAnyOrder("redeployment-validator", "maven-snapshot-version-validator"));

        mockMvc.accept(acceptHeader)
               .when()
               .put(urlAdd, storageId, repositoryId, alias)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_ADD));

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(urlList, storageId, repositoryId)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("versionValidators",
                     containsInAnyOrder("redeployment-validator", "maven-snapshot-version-validator"));
    }


}
