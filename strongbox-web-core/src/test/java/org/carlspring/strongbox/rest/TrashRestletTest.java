package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.carlspring.strongbox.client.ArtifactClient;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.assertFalse;

/**
 * @author mtodorov
 */
public class TrashRestletTest
{

    private static final Artifact ARTIFACT = new DefaultArtifact("org.carlspring.strongbox",
                                                                 "test-artifact-to-trash",
                                                                 "1.0",
                                                                 "compile",
                                                                 "jar",
                                                                 null,
                                                                 new DefaultArtifactHandler("jar"));

    private final static File BASEDIR = new File(ConfigurationResourceResolver.getBasedir()).getAbsoluteFile();

    private final static String REPOSITORY_WITH_TRASH_BASEDIR = BASEDIR.getParentFile().getAbsolutePath() + "/storages/storage0/releases-with-trash";

    private static final String STORAGE = "storage0";

    private static final String REPOSITORY = "releases-with-trash";

    private ArtifactClient client = new ArtifactClient();

    private static final File ARTIFACT_FILE_IN_TRASH = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/.trash/" +
                                                                "org/carlspring/strongbox/test-artifact-to-trash/1.0/" +
                                                                "test-artifact-to-trash-1.0.jar").getAbsoluteFile();


    @Before
    public void setUp()
            throws Exception
    {
        ArtifactGenerator generator = new ArtifactGenerator(REPOSITORY_WITH_TRASH_BASEDIR);
        generator.generate(ARTIFACT);

        client = new ArtifactClient();
        client.setUsername("maven");
        client.setPassword("password");
        client.setPort(48080);
        client.setContextBaseUrl("trash");

        // Delete the artifact
        client.deleteArtifact(ARTIFACT, STORAGE, REPOSITORY);
    }

    @After
    public void tearDown()
            throws Exception
    {
        //noinspection ResultOfMethodCallIgnored
        ARTIFACT_FILE_IN_TRASH.delete();
    }

    @Test
    public void testDeleteArtifactAndEmptyTrashForRepository()
            throws Exception
    {
        client.deleteTrash(STORAGE, REPOSITORY);

        assertFalse("Failed to empty trash for repository '" + REPOSITORY + "'!", ARTIFACT_FILE_IN_TRASH.exists());
    }

    @Test
    public void testDeleteArtifactAndEmptyTrashForAllRepositories()
            throws Exception
    {
        client.deleteTrash();

        assertFalse("Failed to empty trash for repository '" + REPOSITORY + "'!", ARTIFACT_FILE_IN_TRASH.exists());
    }

}
