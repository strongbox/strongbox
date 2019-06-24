package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.artifact.coordinates.PypiWheelArtifactCoordinates;
import org.carlspring.strongbox.dependency.snippet.PypiWheelDependencyFormatter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.*;

/**
*  Class to test the functionality of PypiWheelDependencyFormatter
*/
public class PypiWheelDependencyFormatterTest
{
    private ArrayList<String> wheelArtifactcoordinates = new ArrayList<String>();
    private ArrayList<String> formattedWheels = new ArrayList<String>();

    public PypiWheelDependencyFormatterTest()
    {
        PypiWheelDependencyFormatter formatter = new PypiWheelDependencyFormatter();

        assertNotNull(formatter, "Failed to look up dependency synonym formatter!");

        PypiWheelArtifactCoordinates coordinates;

        // fake examples
        coordinates = PypiWheelArtifactCoordinates.parse("distribution-1.0-1-py27-none-any.whl");
        wheelArtifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        coordinates = PypiWheelArtifactCoordinates.parse("example_pkg_your_username-1.5.0-py3-none-any.whl");
        wheelArtifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        coordinates = PypiWheelArtifactCoordinates.parse("someproject-1.5.0-py2-py3-none.whl");
        wheelArtifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // six
        coordinates = PypiWheelArtifactCoordinates.parse("six-1.12.0-py2.py3-none-any.whl");
        wheelArtifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // futures
        coordinates = PypiWheelArtifactCoordinates.parse("futures-3.2.0-py2-none-any.whl");
        wheelArtifactcoordinates.add(formatter.getDependencySnippet(coordinates));
        // pytz
        coordinates = PypiWheelArtifactCoordinates.parse("pytz-2018.9-3-py2.py3-none-any.whl");
        wheelArtifactcoordinates.add(formatter.getDependencySnippet(coordinates));
        // numpy
        coordinates = PypiWheelArtifactCoordinates.parse("numpy-1.16.2-cp27-cp27m-macosx_10_6_intel.macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl");
        wheelArtifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // cryptography
        coordinates = PypiWheelArtifactCoordinates.parse("cryptography-2.6.1-cp27-cp27m-macosx_10_6_intel.whl");
        wheelArtifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        coordinates = PypiWheelArtifactCoordinates.parse("cryptography-2.6.1-0-cp27-cp27mu-manylinux1_x86_64.whl");
        wheelArtifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // protobuf
        coordinates = PypiWheelArtifactCoordinates.parse("protobuf-3.7.1-cp27-cp27m-macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl");
        wheelArtifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // virtualenv
        coordinates = PypiWheelArtifactCoordinates.parse("virtualenv-16.4.3-py2.py3-none-any.whl");
        wheelArtifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // coverage
        coordinates = PypiWheelArtifactCoordinates.parse("coverage-4.5.3-cp26-cp26m-macosx_10_12_x86_64.whl");
        wheelArtifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // docker
        coordinates = PypiWheelArtifactCoordinates.parse("docker-3.7.2-py2.py3-none-any.whl");
        wheelArtifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // bcrypt
        coordinates = PypiWheelArtifactCoordinates.parse("bcrypt-3.1.6-cp27-cp27m-macosx_10_6_intel.whl");
        wheelArtifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        coordinates = PypiWheelArtifactCoordinates.parse("bcrypt-3.1.6-cp34-cp34m-manylinux1_i686.whl");
        wheelArtifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // We are now populating formattedWheels with manually pre-parsed coordinate arrays
        constructCorrectSolution();
    }

    private void constructCorrectSolution()
    {
        // We are now populating formattedWheels with manually pre-parsed coordinate arrays
        //fake examples
        formattedWheels.add("distribution == 1.0");
        formattedWheels.add("example_pkg_your_username == 1.5.0");
        formattedWheels.add("someproject == 1.5.0");

        //six
        formattedWheels.add("six == 1.12.0");

        //futures
        formattedWheels.add("futures == 3.2.0");

        //pytz
        formattedWheels.add("pytz == 2018.9");

        //numpy
        formattedWheels.add("numpy == 1.16.2");

        //cryptography
        formattedWheels.add("cryptography == 2.6.1");
        formattedWheels.add("cryptography == 2.6.1");

        //protobuf
        formattedWheels.add("protobuf == 3.7.1");

        //virtualenv
        formattedWheels.add("virtualenv == 16.4.3");

        //coverage
        formattedWheels.add("coverage == 4.5.3");

        //docker
        formattedWheels.add("docker == 3.7.2");

        //bcyrpt
        formattedWheels.add("bcrypt == 3.1.6");
        formattedWheels.add("bcrypt == 3.1.6");
    }

    @Test
    public void testGetDependencySnippet()
    {
        for (int i = 0; i < formattedWheels.size(); ++i)
        {
            assertEquals(wheelArtifactcoordinates.get(i), formattedWheels.get(i));
        }
    }
}

