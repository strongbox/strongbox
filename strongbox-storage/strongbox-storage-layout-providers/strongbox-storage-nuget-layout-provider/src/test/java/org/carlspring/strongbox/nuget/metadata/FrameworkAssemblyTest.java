package org.carlspring.strongbox.nuget.metadata;

import java.util.EnumSet;

import org.carlspring.strongbox.nuget.metadata.AssemblyTargetFrameworkAdapter;
import org.carlspring.strongbox.nuget.metadata.Framework;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests collection of assemblies included in the delivery of Frameworks
 */
public class FrameworkAssemblyTest {

    /**
     * Check for string conversion
     *
     * @throws Exception conversion error
     */
    @Test
    public void testUnmarshalEmptyValue() throws Exception {
        //GIVEN
        AssemblyTargetFrameworkAdapter adapter = new AssemblyTargetFrameworkAdapter();
        //WHEN
        EnumSet<Framework> result = adapter.unmarshal("");
        //THEN
        assertNull(result);
    }

    /**
     * Check for null value conversion
     *
     * @throws Exception conversion error
     */
    @Test
    public void testMarshalNullValue() throws Exception {
        //GIVEN
        AssemblyTargetFrameworkAdapter adapter = new AssemblyTargetFrameworkAdapter();
        //WHEN
        String result = adapter.marshal(null);
        //THEN
        assertNull(result);
    }
}