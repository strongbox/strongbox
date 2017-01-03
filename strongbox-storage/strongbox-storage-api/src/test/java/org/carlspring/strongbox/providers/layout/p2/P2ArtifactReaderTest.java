package org.carlspring.strongbox.providers.layout.p2;

import org.carlspring.strongbox.artifact.coordinates.P2ArtifactCoordinates;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class P2ArtifactReaderTest
{

    private static final String CLASSIFIER = "osgi.bundle";
    private static final String ID = "com.carlspring.bundle";
    private static final String VERSION = "1.0.1";
    private static final String PATH = String.format("%s/%s/%s", ID, VERSION, CLASSIFIER);

    @Test
    public void testGetArtifact()
            throws URISyntaxException
    {
        P2ArtifactCoordinates foundArtifact = getArtifact();

        Assert.assertEquals(CLASSIFIER, foundArtifact.getClassifier());
        Assert.assertEquals(ID, foundArtifact.getId());
        Assert.assertEquals(VERSION, foundArtifact.getVersion());
    }

    @Test
    public void testNullPath()
    {
        Assert.assertNull(P2ArtifactReader.getArtifact(".", null));
    }

    @Test
    public void testNullBaseDir()
    {
        Assert.assertNull(P2ArtifactReader.getArtifact(null, ""));
    }

    @Test
    public void testNullParameters()
    {
        Assert.assertNull(P2ArtifactReader.getArtifact(null, null));
    }

    @Test
    public void testInvalidPath()
    {
        Assert.assertNull(P2ArtifactReader.getArtifact(".", "some/invalid@path"));
        Assert.assertNull(P2ArtifactReader.getArtifact(".", "somePath"));
        Assert.assertNull(P2ArtifactReader.getArtifact(".", ""));
        Assert.assertNull(P2ArtifactReader.getArtifact(".", "osgi.bundle/missingName/1.0.1"));
    }

    @Test
    public void testInvalidBaseDir()
    {
        Assert.assertNull(P2ArtifactReader.getArtifact("inavlidRoot!", "somePath"));
    }

    @Test
    public void testEmpryParameters()
    {
        Assert.assertNull(P2ArtifactReader.getArtifact("", ""));
    }

    @Test
    public void testArtifactProperties()
            throws URISyntaxException
    {
        P2ArtifactCoordinates foundArtifact = getArtifact();
        Map<String, String> properties = foundArtifact.getProperties();
        Assert.assertNotNull(properties);

        Map<String, String> expectedProperties = new HashMap<>();
        expectedProperties.put("id", ID);
        expectedProperties.put("version", VERSION);
        expectedProperties.put("classifier", CLASSIFIER);
        expectedProperties.put("download.size", "1");
        expectedProperties.forEach(
                (key, value) ->
                {
                    Assert.assertTrue(key + " not found", properties.containsKey(key));
                    Assert.assertEquals(properties.get(key), value);
                });
    }

    @Test
    public void testMapping()
            throws URISyntaxException
    {
        final String repoDir = getRepoDir();
        P2ArtifactCoordinates foundArtifact = getArtifact(repoDir);
        final String expectedFilename = String.format("%s/plugins/%s_%s.jar", repoDir, ID, VERSION);
        Assert.assertEquals(expectedFilename, foundArtifact.getFilename());
    }

    private P2ArtifactCoordinates getArtifact()
            throws URISyntaxException
    {
        return getArtifact(getRepoDir());
    }

    private String getRepoDir()
            throws URISyntaxException
    {
        return new File(getClass().getResource("artifacts.xml").toURI()).getParent();
    }

    private P2ArtifactCoordinates getArtifact(String repoDir)
    {
        return P2ArtifactReader.getArtifact(repoDir, PATH);
    }
}
