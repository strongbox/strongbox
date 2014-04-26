package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.carlspring.strongbox.client.ArtifactClient;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.junit.Ignore;
import org.junit.Test;
import static junit.framework.Assert.assertFalse;

/**
 * @author mtodorov
 */
public class TrashRestletTest
{

    public static final Artifact ARTIFACT = new DefaultArtifact("org.carlspring.strongbox",
                                                                "test-artifact-to-trash",
                                                                "1.0",
                                                                "compile",
                                                                "jar",
                                                                null,
                                                                new DefaultArtifactHandler("jar"));

    private final static String BASEDIR = "target/strongbox/storages/storage0/releases-with-trash";

    private static final String STORAGE = "storage0";

    public static final String REPOSITORY = "releases-with-trash";


    @Test
    public void testDeleteArtifactAndEmptyTrashForRepository()
            throws Exception
    {
        ArtifactGenerator generator = new ArtifactGenerator(BASEDIR);
        generator.generate(ARTIFACT);

        ArtifactClient client = new ArtifactClient();
        client.setUsername("maven");
        client.setPassword("password");
        client.setPort(48080);
        client.setContextBaseUrl("trash");

        // Delete the artifact
        client.deleteArtifact(ARTIFACT, STORAGE, REPOSITORY);
        client.deleteTrash(REPOSITORY);

        File artifactFileInTrash = new File("target/storages/storage0/releases-with-trash/.trash/" +
                                            "org/carlspring/strongbox/test-artifact-to-trash/1.0/" +
                                            "test-artifact-to-trash-1.0.jar").getAbsoluteFile();


        assertFalse("Failed to empty trash for repository '" + REPOSITORY + "'!", artifactFileInTrash.exists());
    }

    @Test
    public void testDeleteArtifactAndEmptyTrashForAllRepositories()
            throws Exception
    {
        ArtifactGenerator generator = new ArtifactGenerator(BASEDIR);
        generator.generate(ARTIFACT);

        ArtifactClient client = new ArtifactClient();
        client.setUsername("maven");
        client.setPassword("password");
        client.setPort(48080);
        client.setContextBaseUrl("trash");

        // Delete the artifact
        client.deleteArtifact(ARTIFACT, STORAGE, REPOSITORY);
        client.deleteTrash();

        File artifactFileInTrash = new File("target/storages/storage0/releases-with-trash/.trash/" +
                                            "org/carlspring/strongbox/test-artifact-to-trash/1.0/" +
                                            "test-artifact-to-trash-1.0.jar").getAbsoluteFile();


        assertFalse("Failed to empty trash for repository '" + REPOSITORY + "'!", artifactFileInTrash.exists());
    }

}
