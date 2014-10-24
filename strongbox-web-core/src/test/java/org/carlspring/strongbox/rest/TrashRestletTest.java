package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.carlspring.strongbox.client.ArtifactClient;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.testing.AssignedPorts;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static junit.framework.Assert.assertFalse;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/strongbox-*-context.xml",
                                 "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class TrashRestletTest
{

    private final static File BASEDIR = new File(ConfigurationResourceResolver.getBasedir()).getAbsoluteFile();

    private static final String STORAGE = "storage0";

    private static final String REPOSITORY_WITH_TRASH = "releases-with-trash";

    private final static String REPOSITORY_WITH_TRASH_BASEDIR = BASEDIR.getParentFile().getAbsolutePath() +
                                                                "/storages/" + STORAGE + "/" + REPOSITORY_WITH_TRASH;

    private ArtifactClient client = new ArtifactClient();

    private String gavtc = "org.carlspring.strongbox:test-artifact-to-trash::jar";

    private static final File ARTIFACT_FILE_IN_TRASH = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/.trash/" +
                                                                "org/carlspring/strongbox/test-artifact-to-trash/1.0/" +
                                                                "test-artifact-to-trash-1.0.jar").getAbsoluteFile();

    @Autowired
    private AssignedPorts assignedPorts;


    @Before
    public void setUp()
            throws Exception
    {
        ArtifactGenerator generator = new ArtifactGenerator(REPOSITORY_WITH_TRASH_BASEDIR);
        generator.generate(gavtc, "1.0");

        generator.setBasedir(BASEDIR.getParentFile().getAbsolutePath() + "/storages/" + STORAGE + "/releases");
        generator.generate(gavtc, "1.1");

        client = new ArtifactClient();
        client.setUsername("maven");
        client.setPassword("password");
        client.setPort(assignedPorts.getPort("port.jetty.listen"));
        client.setContextBaseUrl("http://localhost:" + client.getPort());

        // Delete the artifact (this one should get placed under the .trash)
        client.delete(STORAGE,
                      REPOSITORY_WITH_TRASH,
                      "org/carlspring/strongbox/test-artifact-to-trash/1.0/test-artifact-to-trash-1.0.jar",
                      true);

        // Delete the artifact (this one shouldn't get placed under the .trash)
        client.delete(STORAGE,
                      "releases",
                      "org/carlspring/strongbox/test-artifact-to-trash/1.1/test-artifact-to-trash-1.1.jar",
                      true);
    }

    @Test
    public void testForceDeleteArtifactNotAllowed()
            throws Exception
    {
        final String artifactPath = "org/carlspring/strongbox/test-artifact-to-trash/1.0/test-artifact-to-trash-1.0.jar";

        final File repositoryDir = new File("target/storages/storage0/releases-with-trash/.trash");
        final File artifactFile = new File(repositoryDir, artifactPath);

        System.out.println("Artifact file: " + artifactFile.getAbsolutePath());

        Assert.assertTrue("Should have moved the artifact to the trash during a force delete operation, " +
                          "when allowsForceDeletion is not enabled!",
                          artifactFile.exists());
    }

    @Test
    public void testForceDeleteArtifactAllowed()
            throws Exception
    {
        final String artifactPath = "org/carlspring/strongbox/test-artifact-to-trash/1.1/test-artifact-to-trash-1.1.jar";

        final File repositoryTrashDir = new File("target/storages/storage0/releases/.trash");
        final File repositoryDir = new File("target/storages/storage0/releases/.trash");

        Assert.assertFalse("Failed to delete artifact during a force delete operation!",
                           new File(repositoryTrashDir, artifactPath).exists());
        Assert.assertFalse("Failed to delete artifact during a force delete operation!",
                           new File(repositoryDir, artifactPath).exists());
    }

    @Test
    public void testDeleteArtifactAndEmptyTrashForRepository()
            throws Exception
    {
        client.deleteTrash(STORAGE, REPOSITORY_WITH_TRASH);

        assertFalse("Failed to empty trash for repository '" + REPOSITORY_WITH_TRASH + "'!", ARTIFACT_FILE_IN_TRASH.exists());
    }

    @Test
    public void testDeleteArtifactAndEmptyTrashForAllRepositories()
            throws Exception
    {
        client.deleteTrash();

        assertFalse("Failed to empty trash for repository '" + REPOSITORY_WITH_TRASH + "'!", ARTIFACT_FILE_IN_TRASH.exists());
    }

}
