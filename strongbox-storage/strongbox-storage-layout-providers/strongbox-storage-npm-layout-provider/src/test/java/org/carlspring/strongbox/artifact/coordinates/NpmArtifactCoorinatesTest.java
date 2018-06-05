package org.carlspring.strongbox.artifact.coordinates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * @author sbespalov
 *
 */
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

    
    @Test(expected = IllegalArgumentException.class)
    public void testVersionAssertion()
    {
        NpmArtifactCoordinates.parse("@types/node/8.beta1/node-8.beta1.tgz");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNameAssertion()
    {
        NpmArtifactCoordinates.parse("@types/NODE/8.0.51/node-8.0.51.tgz");
    }

}
