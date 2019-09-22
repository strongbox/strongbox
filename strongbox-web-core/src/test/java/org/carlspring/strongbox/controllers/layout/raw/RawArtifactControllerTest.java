package org.carlspring.strongbox.controllers.layout.raw;

import org.carlspring.strongbox.artifact.generator.NullArtifactGenerator;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.rest.common.RawRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Martin Todorov
 * @author Pablo Tirado
 */
@IntegrationTest
public class RawArtifactControllerTest
        extends RawRestAssuredBaseTest
{

    private static final String REPOSITORY_RELEASES = "ract-raw-releases";

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testDeploy(@TestRepository(layout = RawLayoutProvider.ALIAS,
                                           repositoryId = REPOSITORY_RELEASES)
                           Repository repository)
            throws IOException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String path = "org/foo/bar/blah.txt";
        byte[] content = "This is a test file\n".getBytes();

        // Push
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" + path;

        given().header(HttpHeaders.USER_AGENT, "Raw/*")
               .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
               .body(content)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        assertPathExists(url);

        InputStream is = client.getResource(url);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int len;
        final int size = 64;
        byte[] bytes = new byte[size];
        while ((len = is.read(bytes, 0, size)) != -1)
        {
            baos.write(bytes, 0, len);
        }

        baos.flush();

        assertThat(new String(baos.toByteArray())).as("Deployed content mismatch!").isEqualTo(new String(content));

        logger.debug("Read '{}',", new String(baos.toByteArray()));
    }

    @ExtendWith({RepositoryManagementTestExecutionListener.class,
                 ArtifactManagementTestExecutionListener.class })
    @Test
    public void testResolveViaHostedRepository(@TestRepository(layout = RawLayoutProvider.ALIAS,
                                                               repositoryId = REPOSITORY_RELEASES)
                                               Repository repository,
                                               @TestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                             resource = "org/foo/bar/blah.zip",
                                                             generator = NullArtifactGenerator.class)
                                               Path artifactPath)
    {
        final String pathStr = "org/foo/bar/blah.zip";

        RepositoryPath artifactRepositoryPath = repositoryPathResolver.resolve(repository, pathStr);
        assertThat(Files.exists(artifactRepositoryPath.toAbsolutePath())).as("Artifact does not exist!").isTrue();
    }

}
