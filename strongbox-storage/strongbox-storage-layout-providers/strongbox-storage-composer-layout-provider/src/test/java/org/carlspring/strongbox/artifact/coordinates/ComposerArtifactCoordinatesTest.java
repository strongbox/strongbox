package org.carlspring.strongbox.artifact.coordinates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for ComposerArtifactCoordinates class
 *
 * @author jcoelho
 */
public class ComposerArtifactCoordinatesTest
{

    private static List<String> packages;

    static
    {
        packages = new ArrayList<>();
    }

    @BeforeAll
    static void setup()
    {


        packages.add("symfony/polyfill-mbstring");

        packages.add("psr/log");

        packages.add("symfony/polyfill-ctype");

        packages.add("guzzlehttp/psr7");

    }

    @Test
    public void testCreateArtifactFromParse()
    {

        for (String _package : packages)
        {
            String vendor = _package.split("/")[0];
            String name = _package.split("/")[1];

            ComposerArtifactCoordinates artifactCoordinates = ComposerArtifactCoordinates.parse(_package);
            assertEquals(vendor, artifactCoordinates.getVendor(), "Incorrect vendor parsed");
            assertEquals(name, artifactCoordinates.getName(), "Incorrect name parsed");
        }

    }

    @Test
    public void testCreateArtifactFromParseExceptions()
    {


        assertThrows(IllegalArgumentException.class, () -> {
            ComposerArtifactCoordinates artifactCoordinates = ComposerArtifactCoordinates.parse(
                    "symfony/polyfill-mbstring/invalid");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ComposerArtifactCoordinates artifactCoordinates = ComposerArtifactCoordinates.parse("symfony");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ComposerArtifactCoordinates artifactCoordinates = ComposerArtifactCoordinates.parse("symfony/");
        });

    }

    @Test
    public void testGetPathFromCoordinates()
    {
        for (String _package : packages)
        {

            String vendor = _package.split("/")[0];
            String name = _package.split("/")[1];
            ComposerArtifactCoordinates artifactCoordinates = new ComposerArtifactCoordinates(vendor, name, null, null);

            assertEquals(_package, artifactCoordinates.toPath(), "Invalid path from coordinates");

        }
    }

}
