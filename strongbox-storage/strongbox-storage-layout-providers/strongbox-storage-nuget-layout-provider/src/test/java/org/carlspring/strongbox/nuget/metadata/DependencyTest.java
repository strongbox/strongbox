package org.carlspring.strongbox.nuget.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.carlspring.strongbox.nuget.NugetFormatException;
import org.carlspring.strongbox.nuget.metadata.Dependency;
import org.carlspring.strongbox.nuget.metadata.Framework;
import org.carlspring.strongbox.nuget.metadata.VersionRange;
import org.junit.jupiter.api.Test;
import org.semver.Version;

/**
 * Test class dependencies
 */
public class DependencyTest {

    /**
     * Checking the toString method
     *
     * @throws Exception error during the test
     */
    @Test
    public void testToString () throws Exception {
        // GIVEN
        Dependency dependency = new Dependency ();
        // WHEN
        dependency.setId ("PACKAGE_ID");
        dependency.versionRange = VersionRange.parse ("1.2.3");
        // THEN
        assertEquals ("PACKAGE_ID:1.2.3", dependency.toString (), "toString - identifier and version concatenation");
    }

    /**
     * Checking dependency line parsing for all frameworks
     *
     * @throws NugetFormatException dependency string does not match format
     * NuGet
     */
    @Test
    public void testParceDependencyAllFrameworks () throws NugetFormatException {
        // GIVEN
        final String dependencyString = "PACKAGE_ID:1.2.3:";
        // WHEN
        Dependency result = Dependency.parseString (dependencyString);
        // THEN
        assertEquals ("PACKAGE_ID", result.getId (), "Package ID");
        assertEquals(VersionRange.parse ("1.2.3"), result.versionRange, "Package Version Range");
        assertNull (result.framework);
    }

    /**
     * Checking dependency line parsing for a specific framework
     *
     * @throws NugetFormatException dependency string does not match format
     * NuGet
     */
    @Test
    public void testParceDependencyFixedFramework () throws NugetFormatException {
        // GIVEN
        final String dependencyString = "PACKAGE_ID:1.2.3:net20";
        // WHEN
        Dependency result = Dependency.parseString (dependencyString);
        // THEN
        assertEquals("PACKAGE_ID", result.getId (), "Package ID");
        assertEquals(VersionRange.parse ("1.2.3"), result.versionRange, "Package Version Range");
        assertEquals(Framework.net20, result.framework);
    }

    /**
     * Verification of recognition of dependencies from the string
     *
     * @throws Exception error during the test
     */
    @Test
    public void testParse () throws Exception {
        // GIVEN
        final String dependencyString = "PACKAGE_ID:1.2.3";
        // WHEN
        Dependency result = Dependency.parseString (dependencyString);
        // THEN
        assertEquals("PACKAGE_ID", result.getId (), "Package ID");
        assertEquals(VersionRange.parse ("1.2.3"), result.versionRange, "Package Version Range");
    }

    /**
     * Verification of recognition of dependencies from the string for a specific version
     *
     * @throws Exception error during the test
     */
    @Test
    public void testParseFixedVersionDependency () throws Exception {
        // GIVEN
        final String dependencyString = "PACKAGE_ID:[1.2.3]";
        // WHEN
        Dependency result = Dependency.parseString (dependencyString);
        // THEN
        assertEquals("PACKAGE_ID", result.getId (), "Package ID");
        assertEquals(VersionRange.parse ("[1.2.3]"), result.versionRange, "Package Version Range");
    }

    /**
     * Check recognition of dependencies from the line for the latest version
     *
     * @throws Exception error during the test
     */
    @Test
    public void testParseLastVersionDependency () throws Exception {
        // GIVEN
        final String dependencyString = "PACKAGE_ID:";
        // WHEN
        Dependency result = Dependency.parseString (dependencyString);
        // THEN
        assertEquals("PACKAGE_ID", result.getId (), "Package ID");
        assertTrue (result.versionRange.isLatestVersion (), "This is the latest version");
    }

    /**
     * Check recognition of dependencies from the line for non-release version
     *
     * @throws Exception error during the test
     */
    @Test
    public void testParseNonReleaseVersionDependency () throws Exception {
        // GIVEN
        final String dependencyString = "PACKAGE.ID:[3.0.0.1029-rc]";
        // WHEN
        Dependency result = Dependency.parseString (dependencyString);
        // THEN
        assertEquals("PACKAGE.ID", result.getId (), "Package ID");
        assertTrue (result.versionRange.isFixedVersion (), "This is the fixed version");
        assertEquals(Version.parse ("3.0.0.1029-rc"), result.versionRange.getLowVersion (), "Package Version");
    }

    /**
     * Verification of recognition of dependencies from the string for non-release version and
     * open top interval
     *
     * @throws Exception error during the test
     */
    @Test
    public void testParseNonReleaseDependency () throws Exception {
         // GIVEN
         final String dependencyString = "PACKAGE.ID:[2.5-a,3.0)";
         // WHEN
         Dependency result = Dependency.parseString (dependencyString);
         // THEN
         assertEquals("PACKAGE.ID", result.getId (), "Package ID");
         assertEquals(Version.parse ("2.5-a"), result.versionRange.getLowVersion (), "Lower Range");
         assertEquals(VersionRange.BorderType.INCLUDE, result.versionRange.getLowBorderType (), "Type Bottom Range");
         assertEquals(Version.parse ("3.0"), result.versionRange.getTopVersion (), "Upper Range Limit");
         assertEquals(VersionRange.BorderType.EXCLUDE, result.versionRange.getTopBorderType (), "Type of the upper bound of the range");
     }
}
