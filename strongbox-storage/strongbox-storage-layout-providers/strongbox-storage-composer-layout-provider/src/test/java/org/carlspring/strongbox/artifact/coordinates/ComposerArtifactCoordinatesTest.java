package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import static org.junit.jupiter.api.Assertions.*;

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
        packages.add("symfony/polyfill-ctype");
        packages.add("guzzlehttp/psr7/library");
        packages.add("psr/log");
        packages.add("symfony/console");
        packages.add("symfony/event-dispatcher/1.2.4-dev/library");
        packages.add("symfony/finder");
        packages.add("symfony/debug");
        packages.add("doctrine/instantiator");
        packages.add("symfony/process/v1.3.5-p");
        packages.add("psr/http-message");
        packages.add("magicbart/zf2-log/library");
        packages.add("phpbe/ui-category-tree");
        packages.add("zhilayun/bbc/v1.4.6");
        packages.add("abdulmatinsanni/api-x");
        packages.add("gesagtgetan/krakenoptimizer");
        packages.add("razielsd/beanstalk-logger");
        packages.add("vargas/translate-bundle/composer-plugin");
        packages.add("pidsolutions/logger/project");
        packages.add("pidsolutions/request");
        packages.add("pidsolutions/i18n");

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
            ComposerArtifactCoordinates artifactCoordinates = ComposerArtifactCoordinates.parse("symfony");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ComposerArtifactCoordinates artifactCoordinates = ComposerArtifactCoordinates.parse("symfony/");
        });

    }

    @Test
    public void testGetPathFromCoordinates()
    {

        for (String p : packages)
        {

            ComposerArtifactCoordinates artifactCoordinates = ComposerArtifactCoordinates.parse(p);
            assertEquals(p, artifactCoordinates.toPath(), "Invalid path from coordinates");

        }

    }

    @Test
    public void testParsingProducesCorrectVersions()
    {
        for (String p : packages)
        {
            ComposerArtifactCoordinates artifactCoordinates = ComposerArtifactCoordinates.parse(p);
            if (artifactCoordinates.getVersion() != null)
            {
                SemanticVersion.parse(artifactCoordinates.getVersion());
            }
        }
    }

}
