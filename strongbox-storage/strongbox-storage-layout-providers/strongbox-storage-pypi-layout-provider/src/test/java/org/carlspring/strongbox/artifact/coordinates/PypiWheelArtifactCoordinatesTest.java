package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.artifact.coordinates.PypiWheelArtifactCoordinates;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import static org.junit.jupiter.api.Assertions.*;


public class PypiWheelArtifactCoordinatesTest
{

    @Test
    public void testCreateArtifact()
    {
        // using distribution-1.0-1-py27-none-any.whl
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

        /**
         * More examples to use in later tests
         */
        // example_pkg_your_username-0.0.1-py3-none-any.whl
        // someproject-1.5.0-py2-py3-none.whl
        // six-1.12.0-py2.py3-none-any.whl 
        // futures-3.2.0-py2-none-any.whl
        // pytz-2018.9-py2.py3-none-any.whl
        // numpy-1.16.2-cp27-cp27m-macosx_10_6_intel.macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl
        // numpy-1.16.2-cp27-cp27m-manylinux1_i686.whl
        // numpy-1.16.2-cp36-cp36m-win_amd64.whl 
        // cryptography-2.6.1-cp27-cp27m-macosx_10_6_intel.whl
        // cryptography-2.6.1-cp27-cp27mu-manylinux1_x86_64.whl
        // cryptography-2.6.1-cp34-cp34m-win32.whl
        // protobuf-3.7.1-cp27-cp27m-macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl  
        // protobuf-3.7.1-cp35-cp35m-win32.whl
        // protobuf-3.7.1-cp37-cp37m-macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl
        // virtualenv-16.4.3-py2.py3-none-any.whl
        // coverage-4.5.3-cp26-cp26m-macosx_10_12_x86_64.whl
        // coverage-4.5.3-cp34-cp34m-win32.whl
        // coverage-4.5.3-cp36-cp36m-manylinux1_x86_64.whl
        // docker-3.7.2-py2.py3-none-any.whl 
        // bcrypt-3.1.6-cp27-cp27m-macosx_10_6_intel.whl
        // bcrypt-3.1.6-cp34-abi3-manylinux1_i686.whl
        // bcrypt-3.1.6-cp37-cp37m-win_amd64.whl
    }
    
    @Test
    public void testParseArtifact()
    {
        PypiWheelArtifactCoordinates coordsParsed = PypiWheelArtifactCoordinates.parse("distribution-1.0-1-py27-none-any.whl");

        assertEquals("distribution", coordsParsed.getId());
        assertEquals("1.0", coordsParsed.getVersion());
        assertEquals("1", coordsParsed.getBuild());
        assertEquals("py27", coordsParsed.getLang());
        assertEquals("none", coordsParsed.getAbi());
        assertEquals("any", coordsParsed.getPlatform());
    }
}
