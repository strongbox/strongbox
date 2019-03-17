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

package org.carlspring.strongbox.storage.metadata.nuget.metadata;

import org.carlspring.strongbox.storage.metadata.nuget.VersionRange;

import org.junit.jupiter.api.Test;
import org.semver.Version;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sviridov
 */
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
        versionRange.setLowVersion(Version.parse("1.2.3"));
        versionRange.setTopVersion(Version.parse("2.3.1"));
        versionRange.setLowBorderType(VersionRange.BorderType.INCLUDE);
        versionRange.setTopBorderType(VersionRange.BorderType.INCLUDE);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertEquals("[1.2.3,2.3.1]", result, "Version Range String");
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
        versionRange.setLowVersion(Version.parse("1.2.3"));
        versionRange.setTopVersion(Version.parse("2.3.1"));
        versionRange.setLowBorderType(VersionRange.BorderType.EXCLUDE);
        versionRange.setTopBorderType(VersionRange.BorderType.EXCLUDE);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertEquals("(1.2.3,2.3.1)", result, "Version Range String");
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
        versionRange.setLowVersion(Version.parse("1.2.3"));
        versionRange.setTopVersion(Version.parse("2.3.1"));
        versionRange.setLowBorderType(VersionRange.BorderType.INCLUDE);
        versionRange.setTopBorderType(VersionRange.BorderType.EXCLUDE);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertEquals("[1.2.3,2.3.1)", result, "Version Range String");
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
        versionRange.setLowVersion(Version.parse("1.2.3"));
        versionRange.setTopVersion(null);
        versionRange.setLowBorderType(VersionRange.BorderType.INCLUDE);
        versionRange.setTopBorderType(null);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertEquals("1.2.3", result, "Version Range String");
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
        versionRange.setTopVersion(Version.parse("1.2.3"));
        versionRange.setLowBorderType(null);
        versionRange.setTopBorderType(VersionRange.BorderType.INCLUDE);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertEquals("(,1.2.3]", result, "Version Range String");
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
        versionRange.setTopVersion(Version.parse("1.2.3"));
        versionRange.setLowBorderType(null);
        versionRange.setTopBorderType(VersionRange.BorderType.EXCLUDE);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertEquals("(,1.2.3)", result, "Version Range String");
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
        versionRange.setLowVersion(Version.parse("1.2.3"));
        versionRange.setTopVersion(Version.parse("1.2.3"));
        versionRange.setLowBorderType(VersionRange.BorderType.INCLUDE);
        versionRange.setTopBorderType(VersionRange.BorderType.INCLUDE);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertEquals("[1.2.3]", result, "Version Range String");
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
        versionRange.setLowVersion(Version.parse("1.2.3"));
        versionRange.setTopVersion(null);
        versionRange.setLowBorderType(VersionRange.BorderType.EXCLUDE);
        versionRange.setTopBorderType(null);
        // WHEN
        String result = versionRange.toString();
        // THEN
        assertEquals("(1.2.3,)", result, "Version Range String");
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
        assertEquals("", result, "Version Range String");
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
        assertEquals(Version.parse(versionString), versionRange.getLowVersion(), "Lower Bound");
        assertEquals(VersionRange.BorderType.INCLUDE, versionRange.getLowBorderType(), "Type of lower border");
        assertNull(versionRange.getTopVersion(), "Upper Bound");
        assertNull(versionRange.getTopBorderType(), "Type of upper border");
        assertTrue(versionRange.isSimpleRange(), "Simple Version");
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
        assertNull(versionRange.getLowVersion(), "Lower bound");
        assertNull(versionRange.getLowBorderType(), "Type lower border");
        assertEquals(Version.parse("1.0"), versionRange.getTopVersion(), "Upper bound");
        assertEquals(VersionRange.BorderType.INCLUDE, versionRange.getTopBorderType(), "Type of upper border");
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
        assertNull(versionRange.getLowVersion(), "Lower bound");
        assertNull(versionRange.getLowBorderType(), "Type lower border");
        assertEquals(Version.parse("1.0"), versionRange.getTopVersion(), "Upper bound");
        assertEquals(VersionRange.BorderType.EXCLUDE, versionRange.getTopBorderType(), "Type of upper border");
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
        assertEquals(Version.parse("1.0"), versionRange.getLowVersion(), "Lower Bound");
        assertEquals(VersionRange.BorderType.INCLUDE, versionRange.getLowBorderType(), "Type of lower border");
        assertEquals(Version.parse("1.0"), versionRange.getTopVersion(), "Upper Bound");
        assertEquals(VersionRange.BorderType.INCLUDE, versionRange.getTopBorderType(), "Type of upper border");
        assertTrue(versionRange.isFixedVersion(), "fixed version");
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
        assertEquals(Version.parse("1.0"), versionRange.getLowVersion(), "Lower Bound");
        assertEquals(VersionRange.BorderType.EXCLUDE, versionRange.getLowBorderType(), "Type lower border");
        assertNull(versionRange.getTopVersion(), "Upper Bound");
        assertNull(versionRange.getTopBorderType(), "Type of upper border");
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
        assertEquals(Version.parse("1.0"), versionRange.getLowVersion(), "Lower Bound");
        assertEquals(VersionRange.BorderType.EXCLUDE, versionRange.getLowBorderType(), "Type lower border");
        assertEquals(Version.parse("2.0"), versionRange.getTopVersion(), "Upper Bound");
        assertEquals(VersionRange.BorderType.EXCLUDE, versionRange.getTopBorderType(), "Type of upper border");
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
        assertEquals(Version.parse("1.0"), versionRange.getLowVersion(), "Lower Bound");
        assertEquals(VersionRange.BorderType.INCLUDE, versionRange.getLowBorderType(), "Type of lower border");
        assertEquals(Version.parse("2.0"), versionRange.getTopVersion(), "Upper Bound");
        assertEquals(VersionRange.BorderType.INCLUDE, versionRange.getTopBorderType(), "Type of upper border");
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
        assertNull(versionRange.getTopVersion(), "Lower bound");
        assertNull(versionRange.getTopBorderType(), "Type lower border");
        assertNull(versionRange.getTopVersion(), "Upper Bound");
        assertNull(versionRange.getTopBorderType(), "Type of upper border");
        assertTrue(versionRange.isLatestVersion(), "fixed version");
    }
}