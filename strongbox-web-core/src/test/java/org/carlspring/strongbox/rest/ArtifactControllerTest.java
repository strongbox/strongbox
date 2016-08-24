package org.carlspring.strongbox.rest;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.util.MessageDigestUtils;

import javax.xml.bind.JAXBException;
import java.io.*;

import com.google.common.io.ByteStreams;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.module.mockmvc.response.MockMvcResponse;
import com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import com.jayway.restassured.response.Headers;
import com.jayway.restassured.response.ResponseBody;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration.generateArtifact;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

/**
 * Created by yury on 8/3/16.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
public class ArtifactControllerTest
        extends BackendBaseTest
{

    private static final String TEST_RESOURCES = "target/test-resources";

    private static final File GENERATOR_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                           "/local");

    private static final File REPOSITORY_BASEDIR_RELEASES = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/storage0/releases");

    private static final Logger logger = LoggerFactory.getLogger(ArtifactControllerTest.class);

    @BeforeClass
    public static void setUpClass()
            throws Exception
    {
        generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                         "org.carlspring.strongbox.resolve.only:foo",
                         "1.1" // Used by testResolveViaProxy()
        );

        // Generate releases
        // Used by testPartialFetch():
        generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                         "org.carlspring.strongbox.partial:partial-foo",
                         "3.1", // Used by testPartialFetch()
                         "3.2"  // Used by testPartialFetch()
        );

        // Used by testCopy*():
        generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                         "org.carlspring.strongbox.copy:copy-foo",
                         "1.1", // Used by testCopyArtifactFile()
                         "1.2"  // Used by testCopyArtifactDirectory()
        );

        // Used by testDelete():
        generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-foo",
                         "1.2.1", // Used by testDeleteArtifactFile
                         "1.2.2"  // Used by testDeleteArtifactDirectory
        );

        generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                         "org.carlspring.strongbox.partial:partial-foo",
                         "3.1", // Used by testPartialFetch()
                         "3.2"  // Used by testPartialFetch()
        );

        generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                         "org.carlspring.strongbox.browse:foo-bar",
                         "1.0", // Used by testDirectoryListing()
                         "2.4"  // Used by testDirectoryListing()
        );

        //noinspection ResultOfMethodCallIgnored
        new File(TEST_RESOURCES).mkdirs();
    }

    @Test
    @WithUserDetails("admin")
    public void testUserAuth()
            throws Exception
    {

        String url = getContextBaseUrl() + "/storages/greet";


        given()
                .contentType(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(200)
                .body(containsString("success"))
                .toString();
    }


    @Test
    @WithUserDetails("admin")
    public void testPartialFetch()
            throws Exception
    {
        // test that given artifact exists
        String url = getContextBaseUrl() + "/storages/storage0/releases";
        String pathToJar = "/org/carlspring/strongbox/partial/partial-foo/3.1/partial-foo-3.1.jar";

        logger.info("Getting " + url + "...");

        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .param("path", pathToJar)
                .when()
                .get(url)
                //.peek()
                .then()
                .statusCode(200);

        // read remote checksum
        String md5Remote = MessageDigestUtils.readChecksumFile(
                getArtifactAsStream(pathToJar + ".md5", url)
        );
        String sha1Remote = MessageDigestUtils.readChecksumFile(
                getArtifactAsStream(pathToJar + ".sha1", url)
        );
        logger.info("Remote md5 checksum " + md5Remote);
        logger.info("Remote sha1 checksum " + sha1Remote);

        // calculate local checksum for given algorithms
        InputStream is = getArtifactAsStream(pathToJar, url);
        System.out.println("Wrote " + is.available() + " bytes.");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteStreams.copy(is, baos); // full copying

        MultipleDigestOutputStream mdos = new MultipleDigestOutputStream(baos);
        mdos.write(baos.toByteArray());

        int total = baos.size(); // FIXME
        mdos.flush();
        mdos.close();

        // 642,367 bytes (643 KB on disk)
        // 1,284,734 bytes (1.3 MB on disk)
        System.out.println("Wrote " + total + " bytes.");
        //System.out.println("Partial read, terminated after writing " + partialRead + " bytes.");
        //System.out.println("Partial read, continued and wrote " + len2 + " bytes.");
        //System.out.println("Partial reads: total written bytes: " + (partialRead + len2) + ".");

        final String md5Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        final String sha1Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        System.out.println("MD5   [Remote]: " + md5Remote);
        System.out.println("MD5   [Local ]: " + md5Local);

        System.out.println("SHA-1 [Remote]: " + sha1Remote);
        System.out.println("SHA-1 [Local ]: " + sha1Local);

        File artifact = new File("target/partial-foo-3.1.jar");
        if (artifact.exists())
        {
            artifact.delete();
            artifact.createNewFile();
        }
        FileOutputStream output = new FileOutputStream(artifact);
        output.write(baos.toByteArray());
        output.close();

        assertEquals("Glued partial fetches did not match MD5 checksum!", md5Remote, md5Local);
        assertEquals("Glued partial fetches did not match SHA-1 checksum!", sha1Remote, sha1Local);
    }

    private InputStream getArtifactAsStream(String path,
                                            String url)
    {
        return getArtifactAsStream(path, url, -1);
    }

    private InputStream getArtifactAsStream(String path,
                                            String url,
                                            int offset)
    {
        return new ByteArrayInputStream(getArtifactAsByteArray(path, url, offset));

    }

    private byte[] getArtifactAsByteArray(String path,
                                          String url,
                                          int offset)
    {
        MockMvcRequestSpecification o = given().contentType(MediaType.TEXT_PLAIN_VALUE);
        int statusCode = 200;
        if (offset != -1)
        {
            o = o.header("Range", "bytes=" + offset + "-");
            statusCode = 206;
        }

        MockMvcResponse response = o.param("path", path)
                                    .when()
                                    .get(url);
        Headers allHeaders = response.getHeaders();
        System.out.println("HTTP GET " + url);
        System.out.println("Response headers:");
        allHeaders.forEach(header ->
                           {
                               System.out.println("\t" + header.getName() + " = " + header.getValue());
                           });

        response.then().statusCode(statusCode);
        byte[] result = response.getMockHttpServletResponse().getContentAsByteArray();
        System.out.println("Received " + result.length + " bytes.");

        return result;
    }

    public boolean pathExists(String path)
    {
        String url = getContextBaseUrl() + (path.startsWith("/") ? path : '/' + path);

        logger.debug("Path to artifact: " + url);

        //   WebTarget resource = getClientInstance().target(url);
        //  setupAuthentication(resource);

        //  Response response = resource.request(MediaType.TEXT_PLAIN).get();

        Integer response;

        response =
                given()
                        .contentType(ContentType.TEXT)
                        .when()
                        .get(url)
                        .then()
                        .statusCode(200)
                        .extract()
                        .response()
                        .body()
                        .path("entity");

        return response == 200;
    }

    /**
     * Looks up a storage by it's ID.
     */
    public Storage getStorage(String storageId)
            throws IOException, JAXBException
    {
        String url = getContextBaseUrl() + "/configuration/strongbox/storages/" + storageId;

        //   WebTarget resource = getClientInstance().target(url);
        //    setupAuthentication(resource);

        //    final Response response = resource.request(MediaType.APPLICATION_XML).get();

        ResponseBody response;

        response =
                given()
                        .contentType(ContentType.JSON)
                        .when()
                        .get(url)
                        .then()
                        .statusCode(200)
                        .extract()
                        .response()
                        .body();

        Storage storage = null;
     /*   if (response.getStatus() == 200)
        {
            final String xml = response.readEntity(String.class);

            final ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());

            GenericParser<Storage> parser = new GenericParser<>(Storage.class);

            storage = parser.parse(bais);
        }*/

        return storage;

    }
}
