package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.validation.deployment.RedeploymentValidator;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;

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
    private MavenRepositoryFactory mavenRepositoryFactory;

    @Inject
    private RedeploymentValidator redeploymentValidator;


    @Inject
    private Maven2LayoutProvider maven2LayoutProvider;


    @Override
    public void init()
            throws Exception
    {
        super.init();

        Repository repository1 = mavenRepositoryFactory.createRepository(STORAGE0, "releases-with-single-validator");
        repository1.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository1.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repository1.setArtifactCoordinateValidators(new LinkedHashSet<>(Collections.singletonList(redeploymentValidator.getAlias())));

        createRepository(repository1);

        Repository repository2 = mavenRepositoryFactory.createRepository(STORAGE0, "releases-with-default-validators");
        repository2.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());

        createRepository(repository2);

        Repository repository3 = new Repository("single-validator-only");
        repository3.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository3.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repository3.setArtifactCoordinateValidators(new LinkedHashSet<>(Collections.singletonList(redeploymentValidator.getAlias())));

        createRepository(repository3);
    }

    @Test
    public void expectOneValidator()
    {
        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/configuration/artifact-coordinate-validators/storage0/releases-with-single-validator")
                          .peek()
                          .then()
                          .body(equalTo("[ \"redeployment-validator\" ]"))
                          .statusCode(200);
    }

    @Test
    public void expectedTwoValidatorsForRepositoryWithDefaultValidators()
    {
        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/configuration/artifact-coordinate-validators/storage0/releases-with-default-validators")
                          .peek()
                          .then()
                          .body(anyOf(equalTo("[ \"redeployment-validator\", \"maven-release-version-validator\" ]"),
                                      equalTo("[ \"maven-release-version-validator\", \"redeployment-validator\" ]")))
                          .statusCode(200);
    }

    @Test
    public void validatorsForReleaseRepositoryShouldBeRemovableAndFailSafe()
    {
        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .delete("/configuration/artifact-coordinate-validators/storage0/releases-with-default-validators/maven-snapshot-version-validator")
                          .peek()
                          .then()
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/configuration/artifact-coordinate-validators/storage0/releases-with-default-validators")
                          .peek()
                          .then()
                          .body(anyOf(equalTo("[ \"redeployment-validator\", \"maven-release-version-validator\" ]"),
                                      equalTo("[ \"maven-release-version-validator\", \"redeployment-validator\" ]")))
                          .statusCode(200);
    }

    @Test
    public void validatorsForReleaseRepositoryShouldBeAdditableAndFailSafe()
    {
        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/configuration/artifact-coordinate-validators/storage0/releases-with-single-validator")
                          .peek()
                          .then()
                          .body(equalTo("[ \"redeployment-validator\" ]"))
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .put("/configuration/artifact-coordinate-validators/storage0/releases-with-single-validator/maven-snapshot-version-validator")
                          .peek()
                          .then()
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/configuration/artifact-coordinate-validators/storage0/releases-with-single-validator")
                          .peek()
                          .then()
                          .body(anyOf(equalTo("[ \"redeployment-validator\", \"maven-snapshot-version-validator\" ]"),
                                      equalTo("[ \"maven-snapshot-version-validator\", \"redeployment-validator\" ]")))
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .put("/configuration/artifact-coordinate-validators/storage0/releases-with-single-validator/maven-snapshot-version-validator")
                          .peek()
                          .then()
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/configuration/artifact-coordinate-validators/storage0/releases-with-single-validator")
                          .peek()
                          .then()
                          .body(anyOf(equalTo("[ \"redeployment-validator\", \"maven-snapshot-version-validator\" ]"),
                                      equalTo("[ \"maven-snapshot-version-validator\", \"redeployment-validator\" ]")))
                          .statusCode(200);
    }

}
