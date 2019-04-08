package org.carlspring.strongbox.artifact.coordinates;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import static org.junit.jupiter.api.Assertions.*;

public class PypiWheelArtifactCoordinatesTest
{
	
	@Test
    public void testCreateArtifact()
    {
		
		// using example distribution-1.0-1-py27-none-any.whl
		String distribution = "distribution";
		String version = "1.0";
		String build_tag = "1";
		String lang_impl_version_tag = "py27";
		String abi_tag = "none";
		String platform_tag = "any";
		
		PypiWheelArtifactCoordinates testCoords = new PypiWheelArtifactCoordinates(distribution, version, build_tag, lang_impl_version_tag, abi_tag, platform_tag);
		
        assertEquals("distribution", testCoords.getId());
        assertEquals("1.0", testCoords.getVersion());
        assertEquals("1", testCoords.getBuild());
        assertEquals("py27", testCoords.getLang());
        assertEquals("none", testCoords.getAbi());
        assertEquals("any", testCoords.getPlatform());
        
    }
    
    
}