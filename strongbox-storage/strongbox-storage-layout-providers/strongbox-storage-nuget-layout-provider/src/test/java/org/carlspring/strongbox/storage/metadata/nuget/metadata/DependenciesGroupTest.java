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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.carlspring.strongbox.storage.metadata.nuget.metadata.DependenciesGroup;
import org.carlspring.strongbox.storage.metadata.nuget.metadata.Framework;
import org.junit.jupiter.api.Test;

/**
 * @author Dmitry Sviridov
 */
public class DependenciesGroupTest
{

    /**
     * Test get framework by short name
     */
    @Test
    public void testUnmarshalShortName()
    {
        // GIVEN
        DependenciesGroup.TargetFrameworkAdapter adapter = new DependenciesGroup.TargetFrameworkAdapter();
        // WHEN
        Framework result = adapter.unmarshal("net40");
        // THEN
        assertEquals(Framework.net40, result);
    }

    /**
     * Test get framework by full name
     */
    @Test
    public void testUnmarshalFullName()
    {
        // GIVEN
        DependenciesGroup.TargetFrameworkAdapter adapter = new DependenciesGroup.TargetFrameworkAdapter();
        // WHEN
        Framework result = adapter.unmarshal(".NETFramework4.5");
        // THEN
        assertEquals(Framework.net45, result);
    }

}
