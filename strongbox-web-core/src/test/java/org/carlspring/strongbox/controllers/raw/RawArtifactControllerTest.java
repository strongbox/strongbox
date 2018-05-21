package org.carlspring.strongbox.controllers.raw;

import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.rest.common.RawRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.Assert.*;

/**
 * @author Martin Todorov
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RawArtifactControllerTest
        extends RawRestAssuredBaseTest
{

    private static final String TEST_RESOURCES = "target/test-resources";

    private static final String REPOSITORY_RELEASES = "ract-raw-releases";

    private static final String REPOSITORY_PROXY = "ract-raw-proxy";


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES, RawLayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_PROXY, RawLayoutProvider.ALIAS));

        return repositories;
    }

    @Override
    public void init()
            throws Exception
    {
        super.init();

        Repository repository = new Repository(REPOSITORY_RELEASES);
        repository.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository.setStorage(getConfiguration().getStorage(STORAGE0));
        repository.setLayout(RawLayoutProvider.ALIAS);

        createRepository(repository);

        //noinspection ResultOfMethodCallIgnored
        Files.createDirectories(Paths.get(TEST_RESOURCES));

        createFile(repository, "org/foo/bar/blah.zip");

        createProxyRepository(STORAGE0,
                              REPOSITORY_PROXY,
                              "http://www-eu.apache.org/dist");
        // Required for http://www-eu.apache.org/dist/maven/maven-3/3.5.3/binaries/apache-maven-3.5.3-bin.tar.gz
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    @Test
    public void testDeploy()
            throws IOException
    {
        String path = "org/foo/bar/blah.txt";
        byte[] content = "This is a test file\n".getBytes();

        // Push
        String artfactUrl = getContextBaseUrl() + "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES + "/" + path;

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

    @Test
    public void testResolveViaHostedRepository()
            throws Exception
    {
        String artifactPath = "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES + "/org/foo/bar/blah.zip";

        resolveArtifact(artifactPath);
    }

    @Test
    public void testResolveArtifactViaProxy()
            throws Exception
    {
        String artifactPath = "/storages/" + STORAGE0 + "/" + REPOSITORY_PROXY +
                              "/maven/maven-3/3.5.3/binaries/apache-maven-3.5.3-bin.tar.gz";

        resolveArtifact(artifactPath);
    }

    private void resolveArtifact(String artifactPath)
            throws NoSuchAlgorithmException, IOException
    {
        String url = getContextBaseUrl() + artifactPath;

        logger.debug("Requesting " + url + "...");

        InputStream is = client.getResource(url);
        if (is == null)
        {
            fail("Failed to resolve " + artifactPath + "!");
        }

        File testResources = new File(TEST_RESOURCES, artifactPath);
        if (!testResources.getParentFile().exists())
        {
            //noinspection ResultOfMethodCallIgnored
            testResources.getParentFile().mkdirs();
        }

        FileOutputStream fos = new FileOutputStream(new File(TEST_RESOURCES, artifactPath));
        MultipleDigestOutputStream mdos = new MultipleDigestOutputStream(fos);

        int total = 0;
        int len;
        final int size = 1024;
        byte[] bytes = new byte[size];

        while ((len = is.read(bytes, 0, size)) != -1)
        {
            mdos.write(bytes, 0, len);
            total += len;
        }

        mdos.flush();
        mdos.close();

        assertTrue("Resolved a zero-length artifact!", total > 0);
    }

}
