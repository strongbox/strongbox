package org.carlspring.strongbox.artifact.coordinates;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(ar1).isEqualTo(ar2);
        assertThat(ar1).isNotEqualTo(ar3);
    }

    @Test
    public void testCreateArtifact()
    {
        P2ArtifactCoordinates artifactCoordinates = P2ArtifactCoordinates.create(PATH);
        assertThat(artifactCoordinates.getId()).isEqualTo(ID);
        assertThat(artifactCoordinates.getVersion()).isEqualTo(VERSION);
        assertThat(artifactCoordinates.getClassifier()).isEqualTo(CLASSIFIER);
    }

    @Test
    public void testToPath()
    {
        P2ArtifactCoordinates artifactCoordinates = new P2ArtifactCoordinates(ID, VERSION, CLASSIFIER);
        assertThat(artifactCoordinates.buildPath()).isEqualTo(PATH);
    }
}
