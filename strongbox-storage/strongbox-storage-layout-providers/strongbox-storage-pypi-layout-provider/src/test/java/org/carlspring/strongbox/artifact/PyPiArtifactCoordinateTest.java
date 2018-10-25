package org.carlspring.strongbox.artifact.coordinates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * @author Gregory Reshetniak
 *
 */
public class PyPiArtifactCoordinateTest
{

    @Test
    public void testArtifactPathToCoordinatesConversion()
    {
        PyPiArtifactCoordinates c = PyPiArtifactCoordinates.parse("distribution-1.0-1-py32-None-Any.whl");

        assertEquals("distribution", c.getDistribution());
        assertEquals("1.0", c.getVersion());
        assertEquals("1", c.getBuildTag());
        assertEquals("py32", c.getPythonTag());
        assertEquals("None", c.getAbiTag());
        assertEquals("Any", c.getPlatformTag());
    }

}
