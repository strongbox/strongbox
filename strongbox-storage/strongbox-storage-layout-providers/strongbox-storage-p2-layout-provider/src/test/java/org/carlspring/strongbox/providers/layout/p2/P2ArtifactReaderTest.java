package org.carlspring.strongbox.providers.layout.p2;

import org.carlspring.strongbox.artifact.coordinates.P2ArtifactCoordinates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class P2ArtifactReaderTest
{

    private static final String CLASSIFIER = "osgi.bundle";
    private static final String ID = "com.carlspring.bundle";
    private static final String VERSION = "1.0.1";
    private static final String PATH = String.format("%s/%s/%s", ID, VERSION, CLASSIFIER);

    @Test
    public void testGetArtifact()
            throws URISyntaxException, IOException
    {
        P2ArtifactCoordinates foundArtifact = getArtifact();

        assertEquals(CLASSIFIER, foundArtifact.getClassifier());
        assertEquals(ID, foundArtifact.getId());
        assertEquals(VERSION, foundArtifact.getVersion());
    }

    @Test
    public void testNullPath()
            throws IOException
    {
        assertThrows(FileNotFoundException.class, () -> {
            assertNull(P2ArtifactReader.getArtifact(".", null));
        });
    }

    @Test
    public void testNullBaseDir()
            throws IOException
    {
        assertThrows(FileNotFoundException.class, () -> {
            assertNull(P2ArtifactReader.getArtifact(null, ""));
        });
    }

    @Test
    public void testNullParameters()
            throws IOException
    {
        assertThrows(FileNotFoundException.class, () -> {
            assertNull(P2ArtifactReader.getArtifact(null, null));
        });
    }

    @Test
    public void testInvalidPath()
            throws IOException
    {
        assertThrows(FileNotFoundException.class, () -> {
            assertNull(P2ArtifactReader.getArtifact(".", "some/invalid@path"));
            assertNull(P2ArtifactReader.getArtifact(".", "somePath"));
            assertNull(P2ArtifactReader.getArtifact(".", ""));
            assertNull(P2ArtifactReader.getArtifact(".", "osgi.bundle/missingName/1.0.1"));
        });
    }

    @Test
    public void testInvalidBaseDir()
            throws IOException
    {
        assertThrows(FileNotFoundException.class, () -> {
            assertNull(P2ArtifactReader.getArtifact("inavlidRoot!", "somePath"));
        });
    }

    @Test
    public void testEmpryParameters()
            throws IOException
    {
        assertThrows(FileNotFoundException.class, () -> {
            assertNull(P2ArtifactReader.getArtifact("", ""));
        });
    }

    @Test
    public void testArtifactProperties()
            throws URISyntaxException, IOException
    {
        P2ArtifactCoordinates foundArtifact = getArtifact();
        Map<String, String> properties = foundArtifact.getProperties();
        assertNotNull(properties);

        Map<String, String> expectedProperties = new HashMap<>();
        expectedProperties.put("id", ID);
        expectedProperties.put("version", VERSION);
        expectedProperties.put("classifier", CLASSIFIER);
        expectedProperties.put("download.size", "1");
        expectedProperties.forEach(
                (key, value) ->
                {
                    assertTrue(properties.containsKey(key), key + " not found");
                    assertEquals(properties.get(key), value);
                });
    }

    @Test
    public void testMapping()
            throws URISyntaxException, IOException
    {
        final String repoDir = getRepoDir();
        P2ArtifactCoordinates foundArtifact = getArtifact(repoDir);
        final String expectedFilename = String.format("%s/plugins/%s_%s.jar", repoDir, ID, VERSION);
        assertEquals(expectedFilename, foundArtifact.getFilename());
    }

    private P2ArtifactCoordinates getArtifact()
            throws URISyntaxException, IOException
    {
        return getArtifact(getRepoDir());
    }

    private String getRepoDir()
            throws URISyntaxException
    {
        return new File(getClass().getResource("artifacts.xml").toURI()).getParent();
    }

    private P2ArtifactCoordinates getArtifact(String repoDir)
            throws IOException
    {
        return P2ArtifactReader.getArtifact(repoDir, PATH);
    }
}
