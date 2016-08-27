package org.carlspring.strongbox.artifact.coordinates;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author carlspring
 */
public class MavenArtifactConverterTest
{

    MavenArtifactPathConverter converter = new MavenArtifactPathConverter();


    @Test
    public void testArtifactPathToCoordinatesConversion()
            throws Exception
    {
        String path = "org/carlspring/strongbox/coordinates-test/1.2.3/coordinates-test-1.2.3.jar";

        MavenArtifactCoordinates coordinates = (MavenArtifactCoordinates) converter.convertPathToCoordinates(path);

        assertEquals("Failed to convert path to artifact coordinates!", "org.carlspring.strongbox", coordinates.getGroupId());
        assertEquals("Failed to convert path to artifact coordinates!", "coordinates-test", coordinates.getArtifactId());
        assertEquals("Failed to convert path to artifact coordinates!", "1.2.3", coordinates.getVersion());
        assertEquals("Failed to convert path to artifact coordinates!", null, coordinates.getClassifier());
        assertEquals("Failed to convert path to artifact coordinates!", null, coordinates.getExtension());
    }

    @Test
    public void testArtifactCoordinatesToPathConversion()
            throws Exception
    {
        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates("org.carlspring.strongbox",
                                                                            "coordinates-test",
                                                                            "1.2.3",
                                                                            null,
                                                                            "jar");

        String path = converter.convertCoordinatesToPath(coordinates);

        assertEquals("Failed to convert artifact coordinates to path!",
                     "org/carlspring/strongbox/coordinates-test/1.2.3/coordinates-test-1.2.3.jar",
                     path);

        System.out.println(path);

        MavenArtifactCoordinates coordinatesWithClassifier = new MavenArtifactCoordinates("org.carlspring.strongbox",
                                                                                          "coordinates-test",
                                                                                          "1.2.3",
                                                                                          "jdk15",
                                                                                          "jar");

        path = converter.convertCoordinatesToPath(coordinatesWithClassifier);

        assertEquals("Failed to convert artifact coordinates to path!",
                     "org/carlspring/strongbox/coordinates-test/1.2.3/coordinates-test-1.2.3-jdk15.jar",
                     path);

        System.out.println(path);
    }

}
