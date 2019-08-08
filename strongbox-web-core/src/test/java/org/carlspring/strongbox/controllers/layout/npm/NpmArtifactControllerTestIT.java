package org.carlspring.strongbox.controllers.layout.npm;

import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.rest.common.NpmRestAssuredBaseTest;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.repository.NpmRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;

import javax.inject.Inject;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class NpmArtifactControllerTestIT
        extends NpmRestAssuredBaseTest
{

    private static final String REPOSITORY_PROXY = "nactit-npm-proxy";

    private static final String REPOSITORY_GROUP = "nactit-npm-group";

    private static final String REMOTE_URL = "https://registry.npmjs.org/";

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Override
    @BeforeEach
    public void init()
        throws Exception
    {
        super.init();
    }

    /**
     * Note: This test requires an Internet connection.
     *
     * @throws Exception
     */
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testResolveArtifactViaProxy(@Remote(url = REMOTE_URL)
                                            @NpmRepository(storageId = STORAGE0,
                                                           repositoryId = REPOSITORY_PROXY)
                                            Repository proxyRepository)
            throws Exception
    {
        // https://registry.npmjs.org/compression/-/compression-1.7.2.tgz
        String artifactPath =
                "/storages/" + proxyRepository.getStorage().getId() + "/" + proxyRepository.getId() + "/" +
                "compression/-/compression-1.7.2.tgz";

        resolveArtifact(artifactPath);
    }

    /**
     * Note: This test requires an Internet connection.
     *
     * @throws Exception
     */
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testResolveArtifactViaGroupWithProxy(@Remote(url = REMOTE_URL)
                                                     @NpmRepository(storageId = STORAGE0,
                                                                    repositoryId = REPOSITORY_PROXY)
                                                     Repository proxyRepository,
                                                     @Group(repositories = REPOSITORY_PROXY)
                                                     @NpmRepository(storageId = STORAGE0,
                                                                    repositoryId = REPOSITORY_GROUP)
                                                     Repository groupRepository)
            throws Exception
    {
        // https://registry.npmjs.org/compression/-/compression-1.7.2.tgz
        String artifactPath =
                "/storages/" + groupRepository.getStorage().getId() + "/" + groupRepository.getId() + "/" +
                "compression/-/compression-1.7.2.tgz";

        resolveArtifact(artifactPath);
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testViewArtifactViaProxy(@Remote(url = REMOTE_URL)
                                         @NpmRepository(storageId = STORAGE0,
                                                        repositoryId = REPOSITORY_PROXY)
                                         Repository proxyRepository)
    {
        final String storageId = proxyRepository.getStorage().getId();
        final String repositoryId = proxyRepository.getId();

        NpmArtifactCoordinates coordinates = NpmArtifactCoordinates.of("react", "16.5.0");

        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{artifactId}";
        given().when()
               .get(url, storageId, repositoryId, coordinates.getId())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .and()
               .body("name", CoreMatchers.equalTo("react"))
               .body("versions.size()", Matchers.greaterThan(0));

        ArtifactEntry artifactEntry = artifactEntryService.findOneArtifact(storageId,
                                                                           repositoryId,
                                                                           coordinates.toPath());
        assertNotNull(artifactEntry);
        assertTrue(artifactEntry instanceof RemoteArtifactEntry);
        assertFalse(((RemoteArtifactEntry)artifactEntry).getIsCached());
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testSearchArtifactViaProxy(@Remote(url = REMOTE_URL)
                                           @NpmRepository(storageId = STORAGE0,
                                                          repositoryId = REPOSITORY_PROXY)
                                           Repository proxyRepository)
    {
        final String storageId = proxyRepository.getStorage().getId();
        final String repositoryId = proxyRepository.getId();

        String url = getContextBaseUrl() +
                     "/storages/{storageId}/{repositoryId}/-/v1/search?text=reston&size=10";
        given().when()
               .get(url, storageId, repositoryId)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .and()
               .body("objects.package.name",
                     CoreMatchers.hasItem("Reston"));
        
        ArtifactEntry artifactEntry = artifactEntryService.findOneArtifact(storageId,
                                                                           repositoryId,
                                                                           "Reston/Reston/0.2.0/Reston-0.2.0.tgz");
        assertNotNull(artifactEntry);
        assertTrue(artifactEntry instanceof RemoteArtifactEntry);
        assertFalse(((RemoteArtifactEntry)artifactEntry).getIsCached());
    }
}
