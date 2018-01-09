package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.VersionValidatorType;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RepositoryVersionValidatorsManagementControllerTest
        extends MavenRestAssuredBaseTest
{

    @Inject
    private ConfigurationManager configurationManager;

    @Override
    public void init()
            throws Exception
    {
        super.init();

        Repository repository1 = new Repository("redeployment-validated-only");
        repository1.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository1.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repository1.setVersionValidators(new HashSet<>(Arrays.asList(VersionValidatorType.REDEPLOYMENT)));

        createRepository(repository1);

        Repository repository2 = new Repository("all-validators");
        repository2.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository2.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));

        createRepository(repository2);

        Repository repository3 = new Repository("single-validator-only");
        repository3.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository3.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repository3.setVersionValidators(new HashSet<>(Arrays.asList(VersionValidatorType.REDEPLOYMENT)));

        createRepository(repository3);
    }

    @Test
    public void expectedOneValidatorForRedeploymentValidatedOnlyRepository()
            throws Exception
    {
        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/configuration/repositories/version-validators/storage0/redeployment-validated-only")
                          .peek()
                          .then()
                          .body(equalTo("[ \"REDEPLOYMENT\" ]"))
                          .statusCode(200);
    }

    @Test
    public void expectedThreeValidatorsForReleaseRepository()
            throws Exception
    {
        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/configuration/repositories/version-validators/storage0/releases")
                          .peek()
                          .then()
                          .body(equalTo("[ \"REDEPLOYMENT\", \"RELEASE\", \"SNAPSHOT\" ]"))
                          .statusCode(200);
    }

    @Test
    public void validatorsForReleaseRepositoryShouldBeRemovableAndFailSafe()
            throws Exception
    {
        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .delete("/configuration/repositories/version-validators/storage0/all-validators/SNAPSHOT")
                          .peek()
                          .then()
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/configuration/repositories/version-validators/storage0/all-validators")
                          .peek()
                          .then()
                          .body(equalTo("[ \"REDEPLOYMENT\", \"RELEASE\" ]"))
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .delete("/configuration/repositories/version-validators/storage0/all-validators/SNAPSHOT")
                          .peek()
                          .then()
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/configuration/repositories/version-validators/storage0/all-validators")
                          .peek()
                          .then()
                          .body(equalTo("[ \"REDEPLOYMENT\", \"RELEASE\" ]"))
                          .statusCode(200);
    }

    @Test
    public void validatorsForReleaseRepositoryShouldBeAdditableAndFailSafe()
            throws Exception
    {
        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/configuration/repositories/version-validators/storage0/single-validator-only")
                          .peek()
                          .then()
                          .body(equalTo("[ \"REDEPLOYMENT\" ]"))
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .put("/configuration/repositories/version-validators/storage0/single-validator-only/SNAPSHOT")
                          .peek()
                          .then()
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/configuration/repositories/version-validators/storage0/single-validator-only")
                          .peek()
                          .then()
                          .body(anyOf(
                                  equalTo("[ \"REDEPLOYMENT\", \"SNAPSHOT\" ]"),
                                  equalTo("[ \"SNAPSHOT\", \"REDEPLOYMENT\" ]")
                          ))
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .put("/configuration/repositories/version-validators/storage0/single-validator-only/SNAPSHOT")
                          .peek()
                          .then()
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/configuration/repositories/version-validators/storage0/single-validator-only")
                          .peek()
                          .then()
                          .body(anyOf(
                                  equalTo("[ \"REDEPLOYMENT\", \"SNAPSHOT\" ]"),
                                  equalTo("[ \"SNAPSHOT\", \"REDEPLOYMENT\" ]")
                          ))
                          .statusCode(200);
    }

}