package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MetadataMerger;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.util.MessageDigestUtils;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import static org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration.generateArtifact;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by yury on 9/26/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
@WithUserDetails("admin")
@Rollback(false)
@Ignore
public class SpringClientTest
        extends BackendBaseTest
{

    public static final String PACKAGING_JAR = "jar";
    private static final String TEST_RESOURCES = "target/test-resources";
    private static final File GENERATOR_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                           "/local");
    private static final File REPOSITORY_BASEDIR_RELEASES = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/storage0/releases");
    private static final String STORAGE = "storage0";
    private static final String REPOSITORY_WITH_TRASH = "releases-with-trash";
    private static final File BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory()).getAbsoluteFile();
    private static final String REPOSITORY_WITH_TRASH_BASEDIR = BASEDIR.getAbsolutePath() +
                                                                "/storages/" + STORAGE + "/" + REPOSITORY_WITH_TRASH;
    private static final File ARTIFACT_FILE_IN_TRASH = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/.trash/" +
                                                                "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                                                                "test-artifact-undelete-1.0.jar").getAbsoluteFile();
    private static final Logger logger = LoggerFactory.getLogger(SpringClientTest.class);
    public static boolean INITIALIZED;
    SpringClient client = new SpringClient().getTestInstanceLoggedInAsAdmin();
    @Inject
    ObjectMapper objectMapper;
    private MetadataMerger metadataMerger;

    @Before
    public void setUpClass()
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

        final String gavtc = "org.carlspring.strongbox.undelete:test-artifact-undelete::jar";

        System.out.println("REPOSITORY_WITH_TRASH_BASEDIR: " + REPOSITORY_WITH_TRASH_BASEDIR);
        System.out.println("BASEDIR.getAbsolutePath(): " + BASEDIR.getAbsolutePath());

        generateArtifact(REPOSITORY_WITH_TRASH_BASEDIR, gavtc, "1.0");
        generateArtifact(BASEDIR.getAbsolutePath() + "/storages/" + STORAGE + "/releases", gavtc, "1.1");

        // Delete the artifact (this one should get placed under the .trash)
        client.delete(STORAGE,
                      REPOSITORY_WITH_TRASH,
                      "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar");

        // Delete the artifact (this one shouldn't get placed under the .trash)
        client.delete(STORAGE,
                      "releases",
                      "org/carlspring/strongbox/undelete/test-artifact-undelete/1.1/test-artifact-undelete-1.1.jar");
    }

    private void removeDir(File dir)
    {

        if (dir == null)
        {
            return;
        }

        System.out.println("Removing directory " + dir.getAbsolutePath());

        if (dir.isDirectory())
        {
            File[] files = dir.listFiles();
            if (files != null)
            {
                for (File file : files)
                {
                    removeDir(file);
                }
            }
        }
        else
        {
            boolean res = dir.delete();
            System.out.println("Remove " + dir.getAbsolutePath() + " " + res);
        }
    }

    @Test
    @WithUserDetails("admin")
    public void testCreateAndDeleteStorageTo()
            throws IOException, JAXBException
    {

        final String storageId = "storage2";
        final String repositoryId1 = "repository0";
        final String repositoryId2 = "repository1";

        Storage storage2 = new Storage(storageId);

        client.addStorage(storage2);

        Repository r1 = new Repository(repositoryId1);
        r1.setAllowsRedeployment(true);
        r1.setSecured(true);
        r1.setStorage(storage2);
        r1.setProxyConfiguration(createProxyConfiguration());

        Repository r2 = new Repository(repositoryId2);
        r2.setAllowsRedeployment(true);
        r2.setSecured(true);
        r2.setStorage(storage2);

        client.addRepository(r1);
        client.addRepository(r2);

        client.getRepository(r1.getStorage().getId(), r1.getId());
        client.deleteRepository(r1.getStorage().getId(), r1.getId(), true);


    }

    @Test
    @WithUserDetails("admin")
    public void searchTest()
            throws IOException
    {

        String response = client.search("g:org.carlspring.maven a:test-project", MediaType.APPLICATION_JSON);

        System.out.println(response);

        Assert.assertTrue("Received unexpected response!",
                          response.contains("\"version\" : \"1.0.11.3\"") &&
                          response.contains("\"version\" : \"1.0.11.3.1\""));
    }


    @Test
    public void artifactTests()
            throws IOException, ArtifactTransportException, ArtifactOperationException
    {


        client.undeleteTrash();

        String url = getContextBaseUrl() + "/storages/storage0/releases";
        String pathToJar = "/org/carlspring/strongbox/partial/partial-foo/3.1/partial-foo-3.1.jar";

        logger.info("Getting " + url + "...");

        System.out.println(client.pathExists(pathToJar, url));

        String md5Remote = MessageDigestUtils.readChecksumFile(client.getResource(pathToJar + ".md5", url));
        System.out.println(md5Remote + "    0000000000000   ");


        ResponseEntity repositoryRoot = client.getResourceWithResponse("");

        String artifactPath = "com/artifacts/to/delete/releases/delete-foo/1.2.2";

        File deletedArtifact = new File(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath() + "/" +
                                        artifactPath).getAbsoluteFile();

        assertTrue("Failed to locate artifact file '" + deletedArtifact.getAbsolutePath() + "'!",
                   deletedArtifact.exists());

        client.delete("storage0", "releases", artifactPath);

        assertFalse("Failed to delete artifact file '" + deletedArtifact.getAbsolutePath() + "'!",
                    deletedArtifact.exists());

        client.deleteTrash(STORAGE, REPOSITORY_WITH_TRASH);

        client.deleteTrash();

        client.undelete(STORAGE,
                        REPOSITORY_WITH_TRASH,
                        "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar");


    }


    private ProxyConfiguration createProxyConfiguration()
    {
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration();
        proxyConfiguration.setHost("localhost");
        proxyConfiguration.setPort(8080);
        proxyConfiguration.setUsername("user1");
        proxyConfiguration.setPassword("pass2");
        proxyConfiguration.setType("http");
        List<String> nonProxyHosts = new ArrayList<>();
        nonProxyHosts.add("localhost");
        nonProxyHosts.add("some-hosts.com");
        proxyConfiguration.setNonProxyHosts(nonProxyHosts);

        return proxyConfiguration;
    }
}
