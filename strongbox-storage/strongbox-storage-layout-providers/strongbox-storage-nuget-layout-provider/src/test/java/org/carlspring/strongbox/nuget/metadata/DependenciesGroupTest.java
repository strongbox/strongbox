package org.carlspring.strongbox.nuget.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.carlspring.strongbox.nuget.metadata.DependenciesGroup;
import org.carlspring.strongbox.nuget.metadata.Framework;
import org.junit.jupiter.api.Test;

public class DependenciesGroupTest {

    /**
     * Test get framework by short name
     */
    @Test
    public void testUnmarshalShortName() {
        //GIVEN
        DependenciesGroup.TargetFrameworkAdapter adapter = new DependenciesGroup.TargetFrameworkAdapter();
        //WHEN
        Framework result = adapter.unmarshal("net40");
        //THEN
        assertEquals(Framework.net40, result);
    }

    /**
     * Test get framework by full name
     */
    @Test
    public void testUnmarshalFullName() {
        //GIVEN
        DependenciesGroup.TargetFrameworkAdapter adapter = new DependenciesGroup.TargetFrameworkAdapter();
        //WHEN
        Framework result = adapter.unmarshal(".NETFramework4.5");
        //THEN
        assertEquals(Framework.net45, result);
    }

}
