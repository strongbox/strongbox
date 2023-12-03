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
 *
 * @author sviridov
 */
@Execution(CONCURRENT)
public class VersionRangeTest
{

    /**
     * Range test with boundaries enabled
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testToStringDoubleInclude()
        throws Exception
    {
        // GIVEN
        VersionRange versionRange = new VersionRange();
        versionRange.setLowVersion(SemanticVersion.parse("1.2.3"));
        versionRange.setTopVersion(SemanticVersion.parse("2.3.1"));
        versionRange.setLowBorderType(VersionRange.BorderType.INCLUDE);
        versionRange.setTopBorderType(VersionRange.BorderType.INCLUDE);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertThat(result).as("Version Range String").isEqualTo("[1.2.3,2.3.1]");
    }

    /**
     * Range test with excluded boundaries
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testToStringDoubleExclude()
        throws Exception
    {
        // GIVEN
        VersionRange versionRange = new VersionRange();
        versionRange.setLowVersion(SemanticVersion.parse("1.2.3"));
        versionRange.setTopVersion(SemanticVersion.parse("2.3.1"));
        versionRange.setLowBorderType(VersionRange.BorderType.EXCLUDE);
        versionRange.setTopBorderType(VersionRange.BorderType.EXCLUDE);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertThat(result).as("Version Range String").isEqualTo("(1.2.3,2.3.1)");
    }

    /**
     * Range test with one enabled and one excluded boundary.
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testToStringIncludeExclude()
        throws Exception
    {
        // GIVEN
        VersionRange versionRange = new VersionRange();
        versionRange.setLowVersion(SemanticVersion.parse("1.2.3"));
        versionRange.setTopVersion(SemanticVersion.parse("2.3.1"));
        versionRange.setLowBorderType(VersionRange.BorderType.INCLUDE);
        versionRange.setTopBorderType(VersionRange.BorderType.EXCLUDE);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertThat(result).as("Version Range String").isEqualTo("[1.2.3,2.3.1)");
    }

    /**
     * Test range not limited from above
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testToStringGreaterThanInclude()
        throws Exception
    {
        // GIVEN
        VersionRange versionRange = new VersionRange();
        versionRange.setLowVersion(SemanticVersion.parse("1.2.3"));
        versionRange.setTopVersion(null);
        versionRange.setLowBorderType(VersionRange.BorderType.INCLUDE);
        versionRange.setTopBorderType(null);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertThat(result).as("Version Range String").isEqualTo("1.2.3");
    }

    /**
     * Test range is not limited to the bottom, including the upper limit
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testToStringLesserThanInclude()
        throws Exception
    {
        // GIVEN
        VersionRange versionRange = new VersionRange();
        versionRange.setLowVersion(null);
        versionRange.setTopVersion(SemanticVersion.parse("1.2.3"));
        versionRange.setLowBorderType(null);
        versionRange.setTopBorderType(VersionRange.BorderType.INCLUDE);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertThat(result).as("Version Range String").isEqualTo("(,1.2.3]");
    }

    /**
     * The test range is not limited to the bottom, excluding the upper limit
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testToStringLesserThanExclude()
        throws Exception
    {
        // GIVEN
        VersionRange versionRange = new VersionRange();
        versionRange.setLowVersion(null);
        versionRange.setTopVersion(SemanticVersion.parse("1.2.3"));
        versionRange.setLowBorderType(null);
        versionRange.setTopBorderType(VersionRange.BorderType.EXCLUDE);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertThat(result).as("Version Range String").isEqualTo("(,1.2.3)");
    }

    /**
     * Test range for fixed version
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testToStringFixedVersion()
        throws Exception
    {
        // GIVEN
        VersionRange versionRange = new VersionRange();
        versionRange.setLowVersion(SemanticVersion.parse("1.2.3"));
        versionRange.setTopVersion(SemanticVersion.parse("1.2.3"));
        versionRange.setLowBorderType(VersionRange.BorderType.INCLUDE);
        versionRange.setTopBorderType(VersionRange.BorderType.INCLUDE);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertThat(result).as("Version Range String").isEqualTo("[1.2.3]");
    }

    /**
     * The test range is not limited from above, not including the border
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testToStringGreaterThanExclude()
        throws Exception
    {
        // GIVEN
        VersionRange versionRange = new VersionRange();
        versionRange.setLowVersion(SemanticVersion.parse("1.2.3"));
        versionRange.setTopVersion(null);
        versionRange.setLowBorderType(VersionRange.BorderType.EXCLUDE);
        versionRange.setTopBorderType(null);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertThat(result).as("Version Range String").isEqualTo("(1.2.3,)");
    }

    /**
     * Test the latest version
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testToStringLastVersion()
        throws Exception
    {
        // GIVEN
        VersionRange versionRange = new VersionRange();
        versionRange.setLowVersion(null);
        versionRange.setTopVersion(null);
        versionRange.setLowBorderType(null);
        versionRange.setTopBorderType(null);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertThat(result).as("Version Range String").isEqualTo("");
    }

    /**
     * Parsing test of the range is not limited from above
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseSimpleVersion()
        throws Exception
    {
        // GIVEN
        String versionString = "1.0";
        // WHEN
        VersionRange versionRange = VersionRange.parse(versionString);
        // THEN
        assertThat(versionRange.getLowVersion()).as("Lower Bound").isEqualTo(SemanticVersion.parse(versionString));
        assertThat(versionRange.getLowBorderType()).as("Type of lower border").isEqualTo(VersionRange.BorderType.INCLUDE);
        assertThat(versionRange.getTopVersion()).as("Upper Bound").isNull();
        assertThat(versionRange.getTopBorderType()).as("Type of upper border").isNull();
        assertThat(versionRange.isSimpleRange()).as("Simple Version").isTrue();
    }

    /**
     * Test of parsing the range is not limited to the bottom
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseLesserThanInclude()
        throws Exception
    {
        // GIVEN
        String versionString = "(,1.0]";
        // WHEN
        VersionRange versionRange = VersionRange.parse(versionString);
        // THEN
        assertThat(versionRange.getLowVersion()).as("Lower bound").isNull();
        assertThat(versionRange.getLowBorderType()).as("Type lower border").isNull();
        assertThat(versionRange.getTopVersion()).as("Upper bound").isEqualTo(SemanticVersion.parse("1.0"));
        assertThat(versionRange.getTopBorderType()).as("Type of upper border").isEqualTo(VersionRange.BorderType.INCLUDE);
    }

    /**
     * Parsing test of the range is not limited to the bottom, excluding the
     * upper limit
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseLesserThanExclude()
        throws Exception
    {
        // GIVEN
        String versionString = "(,1.0)";
        // WHEN
        VersionRange versionRange = VersionRange.parse(versionString);
        // THEN
        assertThat(versionRange.getLowVersion()).as("Lower bound").isNull();
        assertThat(versionRange.getLowBorderType()).as("Type lower border").isNull();
        assertThat(versionRange.getTopVersion()).as("Upper bound").isEqualTo(SemanticVersion.parse("1.0"));
        assertThat(versionRange.getTopBorderType()).as("Type of upper border").isEqualTo(VersionRange.BorderType.EXCLUDE);
    }

    /**
     * Parsing test of the range is not limited to the bottom, excluding the
     * upper limit
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseFixedVersion()
        throws Exception
    {
        // GIVEN
        String versionString = "[1.0]";
        // WHEN
        VersionRange versionRange = VersionRange.parse(versionString);
        // THEN
        assertThat(versionRange.getLowVersion()).as("Lower Bound").isEqualTo(SemanticVersion.parse("1.0"));
        assertThat(versionRange.getLowBorderType()).as("Type of lower border").isEqualTo(VersionRange.BorderType.INCLUDE);
        assertThat(versionRange.getTopVersion()).as("Upper Bound").isEqualTo(SemanticVersion.parse("1.0"));
        assertThat(versionRange.getTopBorderType()).as("Type of upper border").isEqualTo(VersionRange.BorderType.INCLUDE);
        assertThat(versionRange.isFixedVersion()).as("fixed version").isTrue();
    }

    /**
     * Parsing test of the range is not limited to the bottom, excluding the
     * upper limit
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseGreaterThanExclude()
        throws Exception
    {
        // GIVEN
        String versionString = "(1.0,)";
        // WHEN
        VersionRange versionRange = VersionRange.parse(versionString);
        // THEN
        assertThat(versionRange.getLowVersion()).as("Lower Bound").isEqualTo(SemanticVersion.parse("1.0"));
        assertThat(versionRange.getLowBorderType()).as("Type lower border").isEqualTo(VersionRange.BorderType.EXCLUDE);
        assertThat(versionRange.getTopVersion()).as("Upper Bound").isNull();
        assertThat(versionRange.getTopBorderType()).as("Type of upper border").isNull();
    }

    /**
     * Range parsing test with excluded boundaries
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseDoubleExclude()
        throws Exception
    {
        // GIVEN
        String versionString = "(1.0,2.0)";
        // WHEN
        VersionRange versionRange = VersionRange.parse(versionString);
        // THEN
        assertThat(versionRange.getLowVersion()).as("Lower Bound").isEqualTo(SemanticVersion.parse("1.0"));
        assertThat(versionRange.getLowBorderType()).as("Type lower border").isEqualTo(VersionRange.BorderType.EXCLUDE);
        assertThat(versionRange.getTopVersion()).as("Upper Bound").isEqualTo(SemanticVersion.parse("2.0"));
        assertThat(versionRange.getTopBorderType()).as("Type of upper border").isEqualTo(VersionRange.BorderType.EXCLUDE);
    }

    /**
     * Range parsing test with boundaries enabled
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseDoubleInclude()
        throws Exception
    {
        // GIVEN
        String versionString = "[1.0,2.0]";
        // WHEN
        VersionRange versionRange = VersionRange.parse(versionString);
        // THEN
        assertThat(versionRange.getLowVersion()).as("Lower Bound").isEqualTo(SemanticVersion.parse("1.0"));
        assertThat(versionRange.getLowBorderType()).as("Type of lower border").isEqualTo(VersionRange.BorderType.INCLUDE);
        assertThat(versionRange.getTopVersion()).as("Upper Bound").isEqualTo(SemanticVersion.parse("2.0"));
        assertThat(versionRange.getTopBorderType()).as("Type of upper border").isEqualTo(VersionRange.BorderType.INCLUDE);
    }

    /**
     * Test parsing the latest version
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseLatestVersion()
        throws Exception
    {
        // GIVEN
        String versionString = "";
        // WHEN
        VersionRange versionRange = VersionRange.parse(versionString);
        // THEN
        assertThat(versionRange.getTopVersion()).as("Lower bound").isNull();
        assertThat(versionRange.getTopBorderType()).as("Type lower border").isNull();
        assertThat(versionRange.getTopVersion()).as("Upper Bound").isNull();
        assertThat(versionRange.getTopBorderType()).as("Type of upper border").isNull();
        assertThat(versionRange.isLatestVersion()).as("fixed version").isTrue();
    }
}
