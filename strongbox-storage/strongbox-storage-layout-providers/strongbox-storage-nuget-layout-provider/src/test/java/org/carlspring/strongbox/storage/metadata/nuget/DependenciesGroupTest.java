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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Dmitry Sviridov
 */
@Execution(CONCURRENT)
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
        assertThat(result).isEqualTo(Framework.net40);
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
        assertThat(result).isEqualTo(Framework.net45);
    }

}
