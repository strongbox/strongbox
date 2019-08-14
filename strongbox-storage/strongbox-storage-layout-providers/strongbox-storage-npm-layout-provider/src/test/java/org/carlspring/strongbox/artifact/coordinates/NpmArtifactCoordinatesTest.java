package org.carlspring.strongbox.artifact.coordinates;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author sbespalov
 * @author Pablo Tirado
 */
@Execution(CONCURRENT)
public class NpmArtifactCoordinatesTest
{
    @Test
    public void testArtifactPathToCoordinatesConversion()
    {
        NpmArtifactCoordinates coordinates = NpmArtifactCoordinates.parse(
                "react-redux/react-redux/5.0.6/react-redux-5.0.6.tgz");

        assertNull(coordinates.getScope());
        assertEquals("react-redux", coordinates.getName());
        assertEquals("5.0.6", coordinates.getVersion());
        assertEquals("tgz", coordinates.getExtension());

        coordinates = NpmArtifactCoordinates.parse("@types/node/8.0.51/node-8.0.51.tgz");

        assertEquals("@types", coordinates.getScope());
        assertEquals("node", coordinates.getName());
        assertEquals("8.0.51", coordinates.getVersion());
        assertEquals("tgz", coordinates.getExtension());
    }

    @Test
    public void testMetadataPathToCoordinatesConversion()
    {
        NpmArtifactCoordinates coordinates = NpmArtifactCoordinates.parse("react-redux/react-redux/5.0.6/package.json");

        assertNull(coordinates.getScope());
        assertEquals("react-redux", coordinates.getName());
        assertEquals("5.0.6", coordinates.getVersion());
        assertEquals("json", coordinates.getExtension());

        coordinates = NpmArtifactCoordinates.parse("@types/node/8.0.51/package.json");

        assertEquals("@types", coordinates.getScope());
        assertEquals("node", coordinates.getName());
        assertEquals("8.0.51", coordinates.getVersion());
        assertEquals("json", coordinates.getExtension());
    }

    @Test
    void testVersionAssertion()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> NpmArtifactCoordinates.parse("@types/node/8.beta1/node-8.beta1.tgz"));
    }

    @Test
    void testNameAssertion()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> NpmArtifactCoordinates.parse("@types/_node/8.0.51/node-8.0.51.tgz"));
    }
}
