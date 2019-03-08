package org.carlspring.strongbox.nuget.metadata;

import java.util.EnumSet;

import org.carlspring.strongbox.nuget.metadata.Framework;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Test of information about the frameworks for which the package is intended
 */
public class FrameworksTest {

    /**
     * Checking the extraction of a list of frameworks from the query string
     */
    @Test
    public void testParse() {
        //GIVEN
        String targetFramework = "net40|net40|net35|net40|net40|net40|net40|net40|net40|net40|net40|net40|net40|net40|net40";
        //WHEN
        EnumSet<Framework> result = Framework.parse(targetFramework);
        //THEN
        assertThat(result, is(hasItems(Framework.net40, Framework.net35)));
    }

    /**
     * Checking the extraction of a list of frameworks from an empty query string
     */
    @Test
    public void testParseEmptyString() {
        //GIVEN
        String targetFramework = "";
        //WHEN
        EnumSet<Framework> result = Framework.parse(targetFramework);
        //THEN
        assertThat(result, is(hasItems(Framework.values())));
    }

    /**
     * Checking the extraction of a list of frameworks separated by pluses
     */
    @Test
    public void testParsePlusDelimeted() {
        //GIVEN
        String targetFramework = "portable-net45+sl40+wp71+win80";
        //WHEN
        EnumSet<Framework> result = Framework.parse(targetFramework);
        //THEN
        assertThat(result, is(hasItems(Framework.net45, Framework.sl4, Framework.portable_net45, Framework.wp71)));
    }

    /**
     * Verifying the receipt of a complete set of net20 frameworks
     */
    @Test
    public void testGetFullSet() {
        //GIVEN
        Framework framework = Framework.net20;
        //WHEN
        EnumSet<Framework> result = framework.getFullCopabilySet();
        //THEN
        Framework[] expected = {Framework.net20};
        assertArrayEquals(expected, result.toArray(new Framework[1]));
    }
}
