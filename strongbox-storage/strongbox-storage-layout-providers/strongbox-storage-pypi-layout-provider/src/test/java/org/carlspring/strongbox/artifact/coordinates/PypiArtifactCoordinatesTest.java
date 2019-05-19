package org.carlspring.strongbox.artifact.coordinates;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author carlspring
 */
@Execution(CONCURRENT)
public class PypiArtifactCoordinatesTest
{

    @Test
    public void testArtifactFilePathToCoordinatesConversion()
    {
        PypiArtifactCoordinates c = PypiArtifactCoordinates.parse("hello-strongbox-pip-1.2.3.tar.gz");

        assertEquals("hello-strongbox-pip", c.getName());
        assertEquals("1.2.3", c.getVersion());
        assertEquals("tar.gz", c.getExtension());
        assertEquals("hello-strongbox-pip/1.2.3/hello-strongbox-pip-1.2.3.tar.gz", c.getPath());
    }

    @Test
    public void testArtifactFullPathToCoordinatesConversion()
    {
        PypiArtifactCoordinates c = PypiArtifactCoordinates.parse("hello-strongbox-pip/1.2.3/hello-strongbox-pip-1.2.3.tar.gz");

        assertEquals("hello-strongbox-pip", c.getName());
        assertEquals("1.2.3", c.getVersion());
        assertEquals("tar.gz", c.getExtension());
        assertEquals("hello-strongbox-pip/1.2.3/hello-strongbox-pip-1.2.3.tar.gz", c.getPath());
    }

    @Test
    public void testArtifactFullPathToCoordinatesConversionWithInvalidPath()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            PypiArtifactCoordinates.parse("hello-strongbox-pip/hello-strongbox-pip-1.2.tar.gz");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            PypiArtifactCoordinates.parse("hello-strongbox-pip/break-it/1.2.3/hello-strongbox-pip-1.2.3.tar.gz");
        });
    }

    @Test
    public void testArtifactFilePathToCoordinatesConversionWithInvalidVersion()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            PypiArtifactCoordinates.parse("hello-strongbox-pip-1.2.tar.gz");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            PypiArtifactCoordinates.parse("hello-strongbox-pip-1.2.3.4.tar.gz");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            PypiArtifactCoordinates.parse("hello-strongbox-pip-1.2.3.4-rc1.tar.gz");
        });
    }

    @Test
    public void testArtifactFullPathToCoordinatesConversionWithInvalidVersion()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            PypiArtifactCoordinates.parse("hello-strongbox-pip/1.2/hello-strongbox-pip-1.2.tar.gz");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            PypiArtifactCoordinates.parse("hello-strongbox-pip/1.2.3.4/hello-strongbox-pip-1.2.3.4.tar.gz");
        });
    }

}
