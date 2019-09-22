/*
 * Copyright 2019 Carlspring Consulting & Development Ltd.
 * Copyright 2014 Dmitry Sviridov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.carlspring.strongbox.storage.metadata.nuget;

import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * Test class dependencies
 * 
 * @author sviridov
 */
@Execution(CONCURRENT)
public class DependencyTest
{

    /**
     * Checking the toString method
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testToString()
        throws Exception
    {
        // GIVEN
        Dependency dependency = new Dependency();
        // WHEN
        dependency.setId("PACKAGE_ID");
        dependency.versionRange = VersionRange.parse("1.2.3");
        // THEN
        assertThat(dependency.toString()).as("toString - identifier and version concatenation").isEqualTo("PACKAGE_ID:1.2.3");
    }

    /**
     * Checking dependency line parsing for all frameworks
     *
     * @throws NugetFormatException
     *             dependency string does not match format
     *             NuGet
     */
    @Test
    public void testParceDependencyAllFrameworks()
        throws NugetFormatException
    {
        // GIVEN
        final String dependencyString = "PACKAGE_ID:1.2.3:";
        // WHEN
        Dependency result = Dependency.parseString(dependencyString);
        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).as("Package ID").isEqualTo("PACKAGE_ID");
        assertThat(result.versionRange).as("Package Version Range").isEqualTo(VersionRange.parse("1.2.3"));
        assertThat(result.framework).isNull();
    }

    /**
     * Checking dependency line parsing for a specific framework
     *
     * @throws NugetFormatException
     *             dependency string does not match format
     *             NuGet
     */
    @Test
    public void testParceDependencyFixedFramework()
        throws NugetFormatException
    {
        // GIVEN
        final String dependencyString = "PACKAGE_ID:1.2.3:net20";
        // WHEN
        Dependency result = Dependency.parseString(dependencyString);
        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).as("Package ID").isEqualTo("PACKAGE_ID");
        assertThat(result.versionRange).as("Package Version Range").isEqualTo(VersionRange.parse("1.2.3"));
        assertThat(result.framework).isEqualTo(Framework.net20);
    }

    /**
     * Verification of recognition of dependencies from the string
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParse()
        throws Exception
    {
        // GIVEN
        final String dependencyString = "PACKAGE_ID:1.2.3";
        // WHEN
        Dependency result = Dependency.parseString(dependencyString);
        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).as("Package ID").isEqualTo("PACKAGE_ID");
        assertThat(result.versionRange).as("Package Version Range").isEqualTo(VersionRange.parse("1.2.3"));
    }

    /**
     * Verification of recognition of dependencies from the string for a
     * specific version
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseFixedVersionDependency()
        throws Exception
    {
        // GIVEN
        final String dependencyString = "PACKAGE_ID:[1.2.3]";
        // WHEN
        Dependency result = Dependency.parseString(dependencyString);
        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).as("Package ID").isEqualTo("PACKAGE_ID");
        assertThat(result.versionRange).as("Package Version Range").isEqualTo(VersionRange.parse("[1.2.3]"));
    }

    /**
     * Check recognition of dependencies from the line for the latest version
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseLastVersionDependency()
        throws Exception
    {
        // GIVEN
        final String dependencyString = "PACKAGE_ID:";
        // WHEN
        Dependency result = Dependency.parseString(dependencyString);
        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).as("Package ID").isEqualTo("PACKAGE_ID");
        assertThat(result.versionRange.isLatestVersion()).as("This is the latest version").isTrue();
    }

    /**
     * Check recognition of dependencies from the line for non-release version
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseNonReleaseVersionDependency()
        throws Exception
    {
        // GIVEN
        final String dependencyString = "PACKAGE.ID:[3.0.0.1029-rc]";
        // WHEN
        Dependency result = Dependency.parseString(dependencyString);
        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).as("Package ID").isEqualTo("PACKAGE.ID");
        assertThat(result.versionRange.isFixedVersion()).as("This is the fixed version").isTrue();
        assertThat(result.versionRange.getLowVersion()).as("Package Version").isEqualTo(SemanticVersion.parse("3.0.0.1029-rc"));
    }

    /**
     * Verification of recognition of dependencies from the string for
     * non-release version and
     * open top interval
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseNonReleaseDependency()
        throws Exception
    {
        // GIVEN
        final String dependencyString = "PACKAGE.ID:[2.5-a,3.0)";
        // WHEN
        Dependency result = Dependency.parseString(dependencyString);
        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).as("Package ID").isEqualTo("PACKAGE.ID");
        assertThat(result.versionRange.getLowVersion()).as("Lower Range").isEqualTo(SemanticVersion.parse("2.5-a"));
        assertThat(result.versionRange.getLowBorderType()).as("Type Bottom Range").isEqualTo(VersionRange.BorderType.INCLUDE);
        assertThat(result.versionRange.getTopVersion()).as("Upper Range Limit").isEqualTo(SemanticVersion.parse("3.0"));
        assertThat(VersionRange.BorderType.EXCLUDE)
                .as("Type of the upper bound of the range")
                .isEqualTo(result.versionRange.getTopBorderType());
    }

}
