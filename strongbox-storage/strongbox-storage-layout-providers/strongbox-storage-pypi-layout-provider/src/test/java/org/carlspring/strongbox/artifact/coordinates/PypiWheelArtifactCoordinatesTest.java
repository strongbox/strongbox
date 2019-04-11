package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.artifact.coordinates.PypiWheelArtifactCoordinates;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.*; 

/**
 * Class to test the functionality of PypiWheelArtifactCoordinates and PypiWheelArtifactCoordinatesUtils
 * 
 * @author alecg956
 */
public class PypiWheelArtifactCoordinatesTest
{
    private ArrayList<ArrayList<String>> parsedWheels = new ArrayList<ArrayList<String>>();
    
    private ArrayList<String> wheelExamples = new ArrayList<String>();

    public PypiWheelArtifactCoordinatesTest()
    {
        // We are populating wheelExamples with the real Python Wheel filenames

        // fake examples
        wheelExamples.add("distribution-1.0-1-py27-none-any.whl");
        wheelExamples.add("example_pkg_your_username-1.5.0-py3-none-any.whl");
        wheelExamples.add("someproject-1.5.0-py2-py3-none.whl");

        // six
        wheelExamples.add("six-1.12.0-py2.py3-none-any.whl");

        // futures
        wheelExamples.add("futures-3.2.0-py2-none-any.whl");

        // pytz
        wheelExamples.add("pytz-2018.9-3-py2.py3-none-any.whl");

        // numpy
        wheelExamples.add("numpy-1.16.2-cp27-cp27m-macosx_10_6_intel.macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl");
        wheelExamples.add("numpy-1.16.2-cp27-cp27m-manylinux1_i686.whl");
        wheelExamples.add("numpy-1.16.2-19-cp36-cp36m-win_amd64.whl");

        // cryptography
        wheelExamples.add("cryptography-2.6.1-cp27-cp27m-macosx_10_6_intel.whl");
        wheelExamples.add("cryptography-2.6.1-0-cp27-cp27mu-manylinux1_x86_64.whl");
        wheelExamples.add("cryptography-2.6.1-cp34-cp34m-win32.whl");

        // protobuf
        wheelExamples.add("protobuf-3.7.1-cp27-cp27m-macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl");
        wheelExamples.add("protobuf-3.7.1-1-cp35-cp35m-win32.whl");
        wheelExamples.add("protobuf-3.7.1-cp37-cp37m-macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl");

        // virtualenv
        wheelExamples.add("virtualenv-16.4.3-py2.py3-none-any.whl");

        // coverage
        wheelExamples.add("coverage-4.5.3-cp26-cp26m-macosx_10_12_x86_64.whl");
        wheelExamples.add("coverage-4.5.3-5-cp34-cp34m-win32.whl");
        wheelExamples.add("coverage-4.5.3-2-cp36-cp36m-manylinux1_x86_64.whl");

        // docker
        wheelExamples.add("docker-3.7.2-py2.py3-none-any.whl");

        // bcrypt
        wheelExamples.add("bcrypt-3.1.6-cp27-cp27m-macosx_10_6_intel.whl");
        wheelExamples.add("bcrypt-3.1.6-cp34-cp34m-manylinux1_i686.whl");
        wheelExamples.add("bcrypt-3.1.6-cp37-cp37m-win_amd64.whl");

        // We are now populating parseWheels with manually pre-parsed coordinate arrays 

        // fake examples
        parsedWheels.add(new ArrayList<String>(Arrays.asList("distribution", "1.0", "1", "py27", "none", "any")));
        parsedWheels.add(new ArrayList<String>(Arrays.asList("example_pkg_your_username", "1.5.0", "", "py3", "none", "any")));
        parsedWheels.add(new ArrayList<String>(Arrays.asList("someproject", "1.5.0", "", "py2", "py3", "none")));

        // six
        parsedWheels.add(new ArrayList<String>(Arrays.asList("six", "1.12.0", "", "py2.py3", "none", "any")));

        // futures
        parsedWheels.add(new ArrayList<String>(Arrays.asList("futures", "3.2.0", "", "py2", "none", "any")));

        // pytz
        parsedWheels.add(new ArrayList<String>(Arrays.asList("pytz", "2018.9", "3", "py2.py3", "none", "any")));

        // numpy
        parsedWheels.add(new ArrayList<String>(Arrays.asList("numpy", "1.16.2", "", "cp27", "cp27m", "macosx_10_6_intel.macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64")));
        parsedWheels.add(new ArrayList<String>(Arrays.asList("numpy", "1.16.2", "", "cp27", "cp27m", "manylinux1_i686")));
        parsedWheels.add(new ArrayList<String>(Arrays.asList("numpy", "1.16.2", "19", "cp36", "cp36m", "win_amd64")));

        // cryptography
        parsedWheels.add(new ArrayList<String>(Arrays.asList("cryptography", "2.6.1", "", "cp27", "cp27m", "macosx_10_6_intel")));
        parsedWheels.add(new ArrayList<String>(Arrays.asList("cryptography", "2.6.1", "0", "cp27", "cp27mu", "manylinux1_x86_64")));
        parsedWheels.add(new ArrayList<String>(Arrays.asList("cryptography", "2.6.1", "", "cp34", "cp34m", "win32")));

        // protobuf
        parsedWheels.add(new ArrayList<String>(Arrays.asList("protobuf", "3.7.1", "", "cp27", "cp27m", "macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64")));
        parsedWheels.add(new ArrayList<String>(Arrays.asList("protobuf", "3.7.1", "1", "cp35", "cp35m", "win32")));
        parsedWheels.add(new ArrayList<String>(Arrays.asList("protobuf", "3.7.1", "", "cp37", "cp37m", "macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64")));

        // virtualenv
        parsedWheels.add(new ArrayList<String>(Arrays.asList("virtualenv", "16.4.3", "", "py2.py3", "none", "any")));

        // coverage
        parsedWheels.add(new ArrayList<String>(Arrays.asList("coverage", "4.5.3", "", "cp26", "cp26m", "macosx_10_12_x86_64")));
        parsedWheels.add(new ArrayList<String>(Arrays.asList("coverage", "4.5.3", "5", "cp34", "cp34m", "win32")));
        parsedWheels.add(new ArrayList<String>(Arrays.asList("coverage", "4.5.3", "2", "cp36", "cp36m", "manylinux1_x86_64")));

        // docker
        parsedWheels.add(new ArrayList<String>(Arrays.asList("docker", "3.7.2", "", "py2.py3", "none", "any")));

        // bcrypt
        parsedWheels.add(new ArrayList<String>(Arrays.asList("bcrypt", "3.1.6", "", "cp27", "cp27m", "macosx_10_6_intel")));
        parsedWheels.add(new ArrayList<String>(Arrays.asList("bcrypt", "3.1.6", "", "cp34", "cp34m", "manylinux1_i686")));
        parsedWheels.add(new ArrayList<String>(Arrays.asList("bcrypt", "3.1.6", "", "cp37", "cp37m", "win_amd64")));
    }

    /**
     * Tests the constructor of PypiWheelArtifactCoordinates with real Python Wheel package names.  
     * Simply add more pre-parsed filenames to parsedWheels array to test additional filenames.
     */
    @Test
    public void testManualCreateArtifact()
    {      
        for (ArrayList<String> wheel: parsedWheels)
        {
            PypiWheelArtifactCoordinates testCoords = new PypiWheelArtifactCoordinates(wheel.get(0), wheel.get(1), wheel.get(2), wheel.get(3), wheel.get(4),wheel.get(5));

            assertEquals(wheel.get(0), testCoords.getId());
            assertEquals(wheel.get(1), testCoords.getVersion());
            assertEquals(wheel.get(2), testCoords.getBuild());
            assertEquals(wheel.get(3), testCoords.getLanguageImplementationVersion());
            assertEquals(wheel.get(4), testCoords.getAbi());
            assertEquals(wheel.get(5), testCoords.getPlatform());
        }
    }

    /**
     * Tests the constructor of PypiWheelArtifactCoordinates with illegal argument values
     */
    @Test
    public void testCreateArtifactExceptions()
    {      
        // no distribution tag
        assertThrows(IllegalArgumentException.class, () -> {
            PypiWheelArtifactCoordinates testCoords = new PypiWheelArtifactCoordinates("", "3.1.6", "", "cp37", "cp37m", "win_amd64");
            testCoords.toString();
        });

        // no version tag
        assertThrows(IllegalArgumentException.class, () -> {
            PypiWheelArtifactCoordinates testCoords = new PypiWheelArtifactCoordinates("bcrypt", "", "", "cp37", "cp37m", "win_amd64");
            testCoords.toString();
        });

        // illegal build tag
        assertThrows(IllegalArgumentException.class, () -> {
            PypiWheelArtifactCoordinates testCoords = new PypiWheelArtifactCoordinates("bcrypt", "3.1.6", "c", "cp37", "cp37m", "");
            testCoords.toString();
        });

        // no lang_impl_version tag
        assertThrows(IllegalArgumentException.class, () -> {
            PypiWheelArtifactCoordinates testCoords = new PypiWheelArtifactCoordinates("bcrypt", "3.1.6", "", "", "cp37m", "win_amd64");
            testCoords.toString();
        });

        // no abi tag
        assertThrows(IllegalArgumentException.class, () -> {
            PypiWheelArtifactCoordinates testCoords = new PypiWheelArtifactCoordinates("bcrypt", "3.1.6", "", "cp37", "", "win_amd64");
            testCoords.toString();
        });

        // no platform tag
        assertThrows(IllegalArgumentException.class, () -> {
            PypiWheelArtifactCoordinates testCoords = new PypiWheelArtifactCoordinates("bcrypt", "3.1.6", "", "cp37", "cp37m", "");
            testCoords.toString();
        });
    }

    /**
     * Tests the functionality of the PypiWheelArtifactCoordinates parse function which relies on
     * the PypiWheelArtifactCoordinatesUtils parse function
     */
    @Test
    public void testParseCreateArtifact()
    {
        // compare the parsed wheels to the known oracles that we manually parsed
        for (int i = 0; i < wheelExamples.size(); ++i)
        {
            PypiWheelArtifactCoordinates coordsParsed = PypiWheelArtifactCoordinates.parse(wheelExamples.get(i));

            assertEquals(parsedWheels.get(i).get(0), coordsParsed.getId());
            assertEquals(parsedWheels.get(i).get(1), coordsParsed.getVersion());
            assertEquals(parsedWheels.get(i).get(2), coordsParsed.getBuild());
            assertEquals(parsedWheels.get(i).get(3), coordsParsed.getLanguageImplementationVersion());
            assertEquals(parsedWheels.get(i).get(4), coordsParsed.getAbi());
            assertEquals(parsedWheels.get(i).get(5), coordsParsed.getPlatform());
        }
    }

    /**
     * Test that the parser handles the case that too many arguments included in the Wheel filename
     */
    @Test
    public void testTooManyArgumentsException()
    {
        // too many arguments (7), error is at end
        assertThrows(IllegalArgumentException.class, () -> {
            PypiWheelArtifactCoordinates.parse("bcrypt-3.1.6-1-cp27-cp27m-macosx_10_6_intel-thiswillerror.whl");
        });

        // too many arguments (7), error in middle
        assertThrows(IllegalArgumentException.class, () -> {
            PypiWheelArtifactCoordinates.parse("bcrypt-3.1.6-1-thiswillerror-cp27-cp27m-macosx_10_6_intel.whl");
        });
    }

    /**
     * Test that the parser handles the case that too few arguments included in the Wheel filename
     */
    @Test
    public void testTooFewArgumentsException()
    {
        // too few arguments (1)
        assertThrows(IllegalArgumentException.class, () -> {
            PypiWheelArtifactCoordinates.parse("bcrypt.whl");
        });

        // too few arguments (2)
        assertThrows(IllegalArgumentException.class, () -> {
            PypiWheelArtifactCoordinates.parse("bcrypt-3.1.6.whl");
        });

        // too few arguments (3)
        assertThrows(IllegalArgumentException.class, () -> {
            PypiWheelArtifactCoordinates.parse("bcrypt-3.1.6-cp27.whl");
        });

        // too few arguments (4)
        assertThrows(IllegalArgumentException.class, () -> {
            PypiWheelArtifactCoordinates.parse("bcrypt-3.1.6-cp27-cp27m.whl");
        });
    }

    /**
     * Test that the parser handles the case that build tag is specified but empty and if the build tag is 
     * specified but does not start with a number
     */
    @Test
    public void buildTagException()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            PypiWheelArtifactCoordinates.parse("bcrypt-3.1.6-1--cp27-cp27m-macosx_10_6_intel.whl");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            PypiWheelArtifactCoordinates.parse("bcrypt-3.1.6-1-test1-cp27-cp27m-macosx_10_6_intel.whl");
        });
    }

    /**
     * Test that the PypiWheelArtifactCoordinate method toString returns the correct original filename
     */
    @Test
    public void testToString()
    {
        for (int i = 0; i < wheelExamples.size(); ++i)
        {
            PypiWheelArtifactCoordinates coordsParsed = PypiWheelArtifactCoordinates.parse(wheelExamples.get(i));
            String filename = coordsParsed.toString();

            assertEquals(wheelExamples.get(i), filename);
        }
    }
}
