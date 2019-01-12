package org.carlspring.strongbox.artifact.coordinates;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author sbespalov
 */
@Execution(CONCURRENT)
public class NpmArtifactCoorinatesTest
{

    @Test
    public void testArtifactPathToCoordinatesConversion()
    {
        NpmArtifactCoordinates c = NpmArtifactCoordinates.parse("react-redux/react-redux/5.0.6/react-redux-5.0.6.tgz");

        assertNull(c.getScope());
        assertEquals("react-redux", c.getName());
        assertEquals("5.0.6", c.getVersion());
        assertEquals("tgz", c.getExtension());
        
        c = NpmArtifactCoordinates.parse("@types/node/8.0.51/node-8.0.51.tgz");

        assertEquals("@types", c.getScope());
        assertEquals("node", c.getName());
        assertEquals("8.0.51", c.getVersion());
        assertEquals("tgz", c.getExtension());
    }

    @Test
    public void testMetadataPathToCoordinatesConversion()
    {
        NpmArtifactCoordinates c = NpmArtifactCoordinates.parse("react-redux/react-redux/5.0.6/package.json");

        assertNull(c.getScope());
        assertEquals("react-redux", c.getName());
        assertEquals("5.0.6", c.getVersion());
        assertEquals("json", c.getExtension());

        c = NpmArtifactCoordinates.parse("@types/node/8.0.51/package.json");

        assertEquals("@types", c.getScope());
        assertEquals("node", c.getName());
        assertEquals("8.0.51", c.getVersion());
        assertEquals("json", c.getExtension());
    }
    
    @Test
    public void testVersionAssertion()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            NpmArtifactCoordinates.parse("@types/node/8.beta1/node-8.beta1.tgz");
        });
    }

    @Test
    public void testNameAssertion()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            NpmArtifactCoordinates.parse("@types/_node/8.0.51/node-8.0.51.tgz");
        });
    }

}
