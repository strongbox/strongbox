package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.PypiWheelArtifactCoordinates;
import org.carlspring.strongbox.dependency.snippet.PypiWheelDependencyFormatter;
import org.carlspring.strongbox.providers.layout.PypiLayoutProvider;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import javax.inject.Inject;
import java.util.*;

/**
*  Class to test the functionality of PypiWheelDependencyFormatter
*/
public class PypiWheelDependencyFormatterTest
{
  private ArrayList<String> wheelartifactcoordinates = new ArrayList<String>();
  private ArrayList<String> formattedwheels = new ArrayList<String>();
  @Inject
  private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;
  
  public PypiWheelDependencyFormatterTest()
  {
        PypiWheelDependencyFormatter formatter = new PypiWheelDependencyFormatter();
        assertNotNull(formatter, "Failed to look up dependency synonym formatter!");
        PypiWheelArtifactCoordinates coordinates;
    
        // test register
        formatter.register();
    
        // fake examples

        coordinates = PypiWheelArtifactCoordinates.parse("distribution-1.0-1-py27-none-any.whl");
        wheelartifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        coordinates = PypiWheelArtifactCoordinates.parse("example_pkg_your_username-1.5.0-py3-none-any.whl");
        wheelartifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        coordinates = PypiWheelArtifactCoordinates.parse("someproject-1.5.0-py2-py3-none.whl");
        wheelartifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // six
        coordinates = PypiWheelArtifactCoordinates.parse("six-1.12.0-py2.py3-none-any.whl");
        wheelartifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // futures
        coordinates = PypiWheelArtifactCoordinates.parse("futures-3.2.0-py2-none-any.whl");
        wheelartifactcoordinates.add(formatter.getDependencySnippet(coordinates));
        // pytz
        coordinates = PypiWheelArtifactCoordinates.parse("pytz-2018.9-3-py2.py3-none-any.whl");
        wheelartifactcoordinates.add(formatter.getDependencySnippet(coordinates));
        // numpy
        coordinates = PypiWheelArtifactCoordinates.parse("numpy-1.16.2-cp27-cp27m-macosx_10_6_intel.macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl");
        wheelartifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // cryptography
        coordinates = PypiWheelArtifactCoordinates.parse("cryptography-2.6.1-cp27-cp27m-macosx_10_6_intel.whl");
        wheelartifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        coordinates = PypiWheelArtifactCoordinates.parse("cryptography-2.6.1-0-cp27-cp27mu-manylinux1_x86_64.whl");
        wheelartifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // protobuf
        coordinates = PypiWheelArtifactCoordinates.parse("protobuf-3.7.1-cp27-cp27m-macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl");
        wheelartifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // virtualenv
        coordinates = PypiWheelArtifactCoordinates.parse("virtualenv-16.4.3-py2.py3-none-any.whl");
        wheelartifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // coverage
        coordinates = PypiWheelArtifactCoordinates.parse("coverage-4.5.3-cp26-cp26m-macosx_10_12_x86_64.whl");
        wheelartifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // docker
        coordinates = PypiWheelArtifactCoordinates.parse("docker-3.7.2-py2.py3-none-any.whl");
        wheelartifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        // bcrypt
        coordinates = PypiWheelArtifactCoordinates.parse("bcrypt-3.1.6-cp27-cp27m-macosx_10_6_intel.whl");
        wheelartifactcoordinates.add(formatter.getDependencySnippet(coordinates));

        coordinates = PypiWheelArtifactCoordinates.parse("bcrypt-3.1.6-cp34-cp34m-manylinux1_i686.whl");
        wheelartifactcoordinates.add(formatter.getDependencySnippet(coordinates));


        // We are now populating formattedwheels with manually pre-parsed coordinate arrays
        //fake examples
        formattedwheels.add("distribution == 1.0");
        formattedwheels.add("example_pkg_your_username == 1.5.0");
        formattedwheels.add("someproject == 1.5.0");

        //six
        formattedwheels.add("six == 1.12.0");

        //futures
        formattedwheels.add("futures == 3.2.0");

        //pytz
        formattedwheels.add("pytz == 2018.9");

        //numpy
        formattedwheels.add("numpy == 1.16.2");

        //cryptography
        formattedwheels.add("cryptography == 2.6.1");
        formattedwheels.add("cryptography == 2.6.1");

        //protobuf
        formattedwheels.add("protobuf == 3.7.1");

        //virtualenv
        formattedwheels.add("virtualenv == 16.4.3");

        //coverage
        formattedwheels.add("coverage == 4.5.3");

        //docker
        formattedwheels.add("docker == 3.7.2");

        //bcyrpt
        formattedwheels.add("bcrypt == 3.1.6");
        formattedwheels.add("bcrypt == 3.1.6");

        for (int i = 0; i < formattedwheels.size(); ++i)
        {
          assertEquals(wheelartifactcoordinates.get(i), formattedwheels.get(i));
        }
  }
}
