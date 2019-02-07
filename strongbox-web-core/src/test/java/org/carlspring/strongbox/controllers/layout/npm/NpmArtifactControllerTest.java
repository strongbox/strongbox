package org.carlspring.strongbox.controllers.layout.npm;

import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.NpmPackageGenerator;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
import org.carlspring.strongbox.rest.common.NpmRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.NpmRepositoryFactory;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;

@IntegrationTest
@ExtendWith(SpringExtension.class)
public class NpmArtifactControllerTest
        extends NpmRestAssuredBaseTest
{

    private static final String REPOSITORY_RELEASES = "npm-releases-test";

    @Inject
    private NpmRepositoryFactory npmRepositoryFactory;

    @Inject
    @Qualifier("contextBaseUrl")
    private String contextBaseUrl;

    NpmPackageGenerator packageGenerator;


    @BeforeAll
    public static void cleanUp()
        throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES, NpmLayoutProvider.ALIAS));

        return repositories;
    }

    @Override
    @BeforeEach
    public void init()
        throws Exception
    {
        super.init();

        MutableRepository repository = npmRepositoryFactory.createRepository(REPOSITORY_RELEASES);
        repository.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());

        createRepository(STORAGE0, repository);

        String repositoryBasedir = getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES).getAbsolutePath();

        packageGenerator = new NpmPackageGenerator(repositoryBasedir);
    }

    @Test
    public void testViewPackage()
        throws Exception
    {
        NpmArtifactCoordinates coordinates = NpmArtifactCoordinates.of("@carlspring/npm-test-view", "1.0.0");
        Path publishJsonPath = packageGenerator.of(coordinates).buildPublishJson();

        byte[] publishJsonContent = Files.readAllBytes(publishJsonPath);

        //Publish
        given().header("User-Agent", "npm/*")
               .header("Content-Type", "application/json")
               .body(publishJsonContent)
               .when()
               .put(contextBaseUrl + "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES + "/" +
                    coordinates.getId())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        // View OK
        given().header("User-Agent", "npm/*")
               .header("Content-Type", "application/json")
               .when()
               .get(contextBaseUrl + "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES + "/" +
                       coordinates.getId() + "/" + coordinates.getVersion())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());
        
        // View 404
        given().header("User-Agent", "npm/*")
               .header("Content-Type", "application/json")
               .when()
               .get(contextBaseUrl + "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES + "/" +
                       coordinates.getId() + "/1.0.1")
               .peek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());
    }
    
    @Test
    public void testPackageCommonFlow()
        throws Exception
    {
        NpmArtifactCoordinates coordinates = NpmArtifactCoordinates.of("@carlspring/npm-test-release", "1.0.0");
        Path publishJsonPath = packageGenerator.of(coordinates).buildPublishJson();
        Path packagePath = packageGenerator.getPackagePath();

        byte[] publishJsonContent = Files.readAllBytes(publishJsonPath);

        //Publish
        given().header("User-Agent", "npm/*")
               .header("Content-Type", "application/json")
               .body(publishJsonContent)
               .when()
               .put(contextBaseUrl + "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES + "/" +
                    coordinates.getId())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        //View
        given().header("User-Agent", "npm/*")
               .header("Content-Type", "application/json")
               .when()
               .get(contextBaseUrl + "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES + "/" +
                       coordinates.getId())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());
        
        //Download
        given().header("User-Agent", "npm/*")
               .header("Content-Type", "application/json")
               .when()
               .get(contextBaseUrl + "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES + "/" +
                    coordinates.toResource())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .assertThat()
               .header("Content-Length", equalTo(String.valueOf(Files.size(packagePath))));
    }

}
