package org.carlspring.strongbox.controllers.raw;

import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.rest.common.RawRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.Assert.assertEquals;

/**
 * @author Martin Todorov
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RawArtifactControllerTest
        extends RawRestAssuredBaseTest
{

    private static final String TEST_RESOURCES = "target/test-resources";

    private static final String REPOSITORY_RELEASES1 = "ract-raw-releases";


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES1));

        return repositories;
    }

    @Override
    public void init()
            throws Exception
    {
        super.init();

        Repository repository1 = new Repository(REPOSITORY_RELEASES1);
        repository1.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository1.setStorage(getConfiguration().getStorage(STORAGE0));
        repository1.setLayout(RawLayoutProvider.ALIAS);

        createRepository(repository1);

        //noinspection ResultOfMethodCallIgnored
        new File(TEST_RESOURCES).mkdirs();
    }

    @Test
    public void testDeploy()
            throws IOException
    {
        String path = "org/foo/bar/blah.txt";
        byte[] content = "This is a test file\n".getBytes();

        // Push
        String artfactUrl = getContextBaseUrl() + "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES1 + "/" + path;

        given().header("user-agent", "Raw/*")
               .header("Content-Type", "multipart/form-data")
               .body(content)
               .when()
               .put(artfactUrl)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        assertPathExists(artfactUrl);

        InputStream is = client.getResource(artfactUrl);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int len;
        final int size = 64;
        byte[] bytes = new byte[size];
        while ((len = is.read(bytes, 0, size)) != -1)
        {
            baos.write(bytes, 0, len);
        }

        baos.flush();

        assertEquals("Deployed content mismatch!", new String(content), new String(baos.toByteArray()));

        System.out.println("Read '" + new String(baos.toByteArray()) + "'.");
    }

}
