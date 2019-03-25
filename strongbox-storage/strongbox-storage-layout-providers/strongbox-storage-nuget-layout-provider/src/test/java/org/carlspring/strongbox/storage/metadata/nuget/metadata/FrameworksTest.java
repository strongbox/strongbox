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

import org.carlspring.strongbox.storage.metadata.nuget.Framework;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Test of information about the frameworks for which the package is intended
 * 
 * @author sviridov
 */
public class FrameworksTest
{

    /**
     * Checking the extraction of a list of frameworks from the query string
     */
    @Test
    public void testParse()
    {
        // GIVEN
        String targetFramework = "net40|net40|net35|net40|net40|net40|net40|net40|net40|net40|net40|net40|net40|net40|net40";
        // WHEN
        EnumSet<Framework> result = Framework.parse(targetFramework);
        // THEN
        assertThat(result, is(hasItems(Framework.net40, Framework.net35)));
    }

    /**
     * Checking the extraction of a list of frameworks from an empty query
     * string
     */
    @Test
    public void testParseEmptyString()
    {
        // GIVEN
        String targetFramework = "";
        // WHEN
        EnumSet<Framework> result = Framework.parse(targetFramework);
        // THEN
        assertThat(result, is(hasItems(Framework.values())));
    }

    /**
     * Checking the extraction of a list of frameworks separated by pluses
     */
    @Test
    public void testParsePlusDelimeted()
    {
        // GIVEN
        String targetFramework = "portable-net45+sl40+wp71+win80";
        // WHEN
        EnumSet<Framework> result = Framework.parse(targetFramework);
        // THEN
        assertThat(result, is(hasItems(Framework.net45, Framework.sl4, Framework.portable_net45, Framework.wp71)));
    }

    /**
     * Verifying the receipt of a complete set of net20 frameworks
     */
    @Test
    public void testGetFullSet()
    {
        // GIVEN
        Framework framework = Framework.net20;
        // WHEN
        EnumSet<Framework> result = framework.getFullCompatibilitySet();
        // THEN
        Framework[] expected = { Framework.net20 };
        assertArrayEquals(expected, result.toArray(new Framework[1]));
    }
}
