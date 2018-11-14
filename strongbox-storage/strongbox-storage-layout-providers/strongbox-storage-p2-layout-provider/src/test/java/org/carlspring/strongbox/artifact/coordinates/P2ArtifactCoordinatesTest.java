package org.carlspring.strongbox.artifact.coordinates;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class P2ArtifactCoordinatesTest
{

    private static final String ID = "myID";

    private static final String VERSION = "1.0.0";

    private static final String CLASSIFIER = "osgi.bundle";

    private static final String PATH = ID + "/" + VERSION + "/" + CLASSIFIER;

    @Test
    public void testEquals()
    {
        P2ArtifactCoordinates ar1 = new P2ArtifactCoordinates(ID, VERSION, CLASSIFIER);
        P2ArtifactCoordinates ar2 = new P2ArtifactCoordinates(ID, VERSION, CLASSIFIER);
        P2ArtifactCoordinates ar3 = new P2ArtifactCoordinates("anotherID", "1.0.0", "feature.group");

        assertTrue(ar1.equals(ar2));
        assertFalse(ar1.equals(ar3));
    }

    @Test
    public void testCreateArtifact()
    {
        P2ArtifactCoordinates artifactCoordinates = P2ArtifactCoordinates.create(PATH);
        assertEquals(ID, artifactCoordinates.getId());
        assertEquals(VERSION, artifactCoordinates.getVersion());
        assertEquals(CLASSIFIER, artifactCoordinates.getClassifier());
    }

    @Test
    public void testToPath()
    {
        P2ArtifactCoordinates artifactCoordinates = new P2ArtifactCoordinates(ID, VERSION, CLASSIFIER);
        assertEquals(PATH, artifactCoordinates.toPath());
    }
}
