package org.carlspring.strongbox.artifact.coordinates;

import org.junit.Assert;
import org.junit.Test;

public class P2ArtifactCoordinatesTest {

    private static final String ID = "myID";

    private static final String VERSION = "1.0.0";

    private static final String CLASSIFIER = "osgi.bundle";

    private static final String PATH = ID + "/" + VERSION + "/" + CLASSIFIER;

    @Test
    public void testEquals() {
        P2ArtifactCoordinates ar1 = new P2ArtifactCoordinates(ID, VERSION, CLASSIFIER);
        P2ArtifactCoordinates ar2 = new P2ArtifactCoordinates(ID, VERSION, CLASSIFIER);
        P2ArtifactCoordinates ar3 = new P2ArtifactCoordinates("anotherID", "1.0.0", "feature.group");

        Assert.assertTrue(ar1.equals(ar2));
        Assert.assertFalse(ar1.equals(ar3));
    }

    @Test
    public void testCreateArtifact() {
        P2ArtifactCoordinates artifactCoordinates = P2ArtifactCoordinates.create(PATH);
        Assert.assertEquals(ID, artifactCoordinates.getId());
        Assert.assertEquals(VERSION, artifactCoordinates.getVersion());
        Assert.assertEquals(CLASSIFIER, artifactCoordinates.getClassifier());
    }

    @Test
    public void testToPath() {
        P2ArtifactCoordinates artifactCoordinates = new P2ArtifactCoordinates(ID, VERSION, CLASSIFIER);
        Assert.assertEquals(PATH, artifactCoordinates.toPath());
    }
}
