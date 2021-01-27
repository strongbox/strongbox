package org.carlspring.strongbox.artifact.coordinates;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.*;

/**
 * Class to test the functionality of PypiArtifactCoordinates and PypiArtifactCoordinatesUtils
 * 
 * @author alecg956
 */
public class PypiArtifactCoordinatesTest
{
    private ArrayList<ArrayList<String>> parsedPackages = new ArrayList<>();

    private ArrayList<String> packageExamples = new ArrayList<>();

    public PypiArtifactCoordinatesTest()
    {
        // We are populating wheel examples with the real Python Wheel filenames
        packageExamples.add("distribution-1.0-1-py27-none-any.whl");
        packageExamples.add("example_pkg_your_username-1.5.0-py3-none-any.whl");
        packageExamples.add("someproject-1.5.0-py2-py3-none.whl");
        packageExamples.add("six-1.12.0-py2.py3-none-any.whl");
        packageExamples.add("futures-3.2.0-py2-none-any.whl");
        packageExamples.add("pytz-2018.9-3-py2.py3-none-any.whl");
        packageExamples.add("numpy-1.16.2-cp27-cp27m-macosx_10_6_intel.macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl");
        packageExamples.add("numpy-1.16.2-cp27-cp27m-manylinux1_i686.whl");
        packageExamples.add("numpy-1.16.2-19-cp36-cp36m-win_amd64.whl");
        packageExamples.add("cryptography-2.6.1-cp27-cp27m-macosx_10_6_intel.whl");
        packageExamples.add("cryptography-2.6.1-0-cp27-cp27mu-manylinux1_x86_64.whl");
        packageExamples.add("cryptography-2.6.1-cp34-cp34m-win32.whl");
        packageExamples.add("protobuf-3.7.1-cp27-cp27m-macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl");
        packageExamples.add("protobuf-3.7.1-1-cp35-cp35m-win32.whl");
        packageExamples.add("protobuf-3.7.1-cp37-cp37m-macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl");
        packageExamples.add("virtualenv-16.4.3-py2.py3-none-any.whl");
        packageExamples.add("coverage-4.5.3-cp26-cp26m-macosx_10_12_x86_64.whl");
        packageExamples.add("coverage-4.5.3-5-cp34-cp34m-win32.whl");
        packageExamples.add("coverage-4.5.3-2-cp36-cp36m-manylinux1_x86_64.whl");
        packageExamples.add("docker-3.7.2-py2.py3-none-any.whl");
        packageExamples.add("bcrypt-3.1.6-cp27-cp27m-macosx_10_6_intel.whl");
        packageExamples.add("bcrypt-3.1.6-cp34-cp34m-manylinux1_i686.whl");
        packageExamples.add("bcrypt-3.1.6-cp37-cp37m-win_amd64.whl");

        // We are populating source examples with the real Python Wheel filenames
        packageExamples.add("distribution-1.0.tar.gz");
        packageExamples.add("example_pkg_your_username-1.5.0.tar.gz");
        packageExamples.add("someproject-1.5.0.tar.gz");
        packageExamples.add("six-1.12.0.tar.gz");
        packageExamples.add("futures-3.2.0.tar.gz");
        packageExamples.add("pytz-2018.9.tar.gz");
        packageExamples.add("numpy-1.16.2.tar.gz");
        packageExamples.add("bcrypt-3.1.6.tar.gz");


        parsedPackages.add(new ArrayList<>(Arrays.asList("distribution", "1.0", "1", "py27", "none", "any", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("example_pkg_your_username", "1.5.0", null, "py3", "none", "any", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("someproject", "1.5.0", null, "py2", "py3", "none", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("six", "1.12.0", null, "py2.py3", "none", "any", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("futures", "3.2.0", null, "py2", "none", "any", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("pytz", "2018.9", "3", "py2.py3", "none", "any", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("numpy", "1.16.2", null, "cp27", "cp27m", "macosx_10_6_intel.macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("numpy", "1.16.2", null, "cp27", "cp27m", "manylinux1_i686", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("numpy", "1.16.2", "19", "cp36", "cp36m", "win_amd64", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("cryptography", "2.6.1", null, "cp27", "cp27m", "macosx_10_6_intel", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("cryptography", "2.6.1", "0", "cp27", "cp27mu", "manylinux1_x86_64", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("cryptography", "2.6.1", null, "cp34", "cp34m", "win32", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("protobuf", "3.7.1", null, "cp27", "cp27m", "macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("protobuf", "3.7.1", "1", "cp35", "cp35m", "win32", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("protobuf", "3.7.1", null, "cp37", "cp37m", "macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("virtualenv", "16.4.3", null, "py2.py3", "none", "any", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("coverage", "4.5.3", null, "cp26", "cp26m", "macosx_10_12_x86_64", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("coverage", "4.5.3", "5", "cp34", "cp34m", "win32", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("coverage", "4.5.3", "2", "cp36", "cp36m", "manylinux1_x86_64", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("docker", "3.7.2", null, "py2.py3", "none", "any", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("bcrypt", "3.1.6", null, "cp27", "cp27m", "macosx_10_6_intel", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("bcrypt", "3.1.6", null, "cp34", "cp34m", "manylinux1_i686", "whl")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("bcrypt", "3.1.6", null, "cp37", "cp37m", "win_amd64", "whl")));

        parsedPackages.add(new ArrayList<>(Arrays.asList("distribution", "1.0", null, null, null, null, "tar.gz")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("example_pkg_your_username", "1.5.0", null, null, null, null, "tar.gz")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("someproject", "1.5.0", null, null, null, null, "tar.gz")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("six", "1.12.0", null, null, null, null, "tar.gz")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("futures", "3.2.0", null, null, null, null, "tar.gz")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("pytz", "2018.9", null, null, null, null, "tar.gz")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("numpy", "1.16.2", null, null, null, null, "tar.gz")));
        parsedPackages.add(new ArrayList<>(Arrays.asList("bcrypt", "3.1.6", null, null, null, null, "tar.gz")));



    }

    /**
     * Tests the constructor of PypiArtifactCoordinates with real Python package names.
     * Simply add more pre-parsed filenames to parsedPackages array to test additional filenames.
     */
    @RepeatedTest(31)
    public void testManualCreateArtifact(RepetitionInfo repetitionInfo)
    {
        List<String> coords = parsedPackages.get(repetitionInfo.getCurrentRepetition() - 1);
        PypiArtifactCoordinates testCoords = new PypiArtifactCoordinates(coords.get(0),
                                                                         coords.get(1),
                                                                         coords.get(2),
                                                                         coords.get(3),
                                                                         coords.get(4),
                                                                         coords.get(5),
                                                                         coords.get(6));
        assertThat(testCoords.getId()).isEqualTo(coords.get(0));
        assertThat(testCoords.getVersion()).isEqualTo(coords.get(1));
        assertThat(testCoords.getBuild()).isEqualTo(coords.get(2));
        assertThat(testCoords.getLanguageImplementationVersion()).isEqualTo(coords.get(3));
        assertThat(testCoords.getAbi()).isEqualTo(coords.get(4));
        assertThat(testCoords.getPlatform()).isEqualTo(coords.get(5));
    }

    /**
     * Tests the constructor of PypiArtifactCoordinates with illegal argument values
     */
    @Test
    public void testCreateArtifactExceptions()
    {
        //incorrect extension
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PypiArtifactCoordinates("", "3.1.6", "", "cp37", "cp37m", "win_amd64", "tar"));

        // no distribution tag
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PypiArtifactCoordinates("", "3.1.6", "", "cp37", "cp37m", "win_amd64", "whl"));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PypiArtifactCoordinates("", "3.1.6", "tar.gz"));

        // no version tag
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PypiArtifactCoordinates("bcrypt", "", "", "cp37", "cp37m", "win_amd64", "whl"));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PypiArtifactCoordinates("bcrypt", null, "tar.gz"));

        // illegal build tag
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PypiArtifactCoordinates("bcrypt", "3.1.6", "c", "cp37", "cp37m", "", "whl"));

        // no lang_impl_version tag
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PypiArtifactCoordinates("bcrypt", "3.1.6", "", "", "cp37m", "win_amd64", "whl"));

        // no abi tag
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PypiArtifactCoordinates("bcrypt", "3.1.6", "", "cp37", "", "win_amd64", "whl"));

        // no platform tag
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new PypiArtifactCoordinates("bcrypt", "3.1.6", "", "cp37", "cp37m", "", "whl"));
    }

    /**
     * Tests the functionality of the PypiArtifactCoordinates parse function which relies on
     * the PypiArtifactCoordinatesUtils parse function
     */
    @RepeatedTest(31)
    public void testParseCreateArtifact(RepetitionInfo repetitionInfo)
    {
        // compare the parsed packages to the known oracles that we manually parsed
        int currentIndex = repetitionInfo.getCurrentRepetition() - 1;

        PypiArtifactCoordinates coordsParsed = PypiArtifactCoordinates.parse(packageExamples.get(currentIndex));
        assertThat(coordsParsed.getId()).isEqualTo(parsedPackages.get(currentIndex).get(0));
        assertThat(coordsParsed.getVersion()).isEqualTo(parsedPackages.get(currentIndex).get(1));
        assertThat(coordsParsed.getBuild()).isEqualTo(parsedPackages.get(currentIndex).get(2));
        assertThat(coordsParsed.getLanguageImplementationVersion()).isEqualTo(parsedPackages.get(currentIndex).get(3));
        assertThat(coordsParsed.getAbi()).isEqualTo(parsedPackages.get(currentIndex).get(4));
        assertThat(coordsParsed.getPlatform()).isEqualTo(parsedPackages.get(currentIndex).get(5));
    }

    /**
     * Test that the parser handles the case that too many arguments included in the filename
     */
    @Test
    public void testTooManyArgumentsException()
    {
        // too many arguments (7), error is at end
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PypiArtifactCoordinates.parse("bcrypt-3.1.6-1-cp27-cp27m-macosx_10_6_intel-thiswillerror.whl"));

        // too many arguments (7), error in middle
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PypiArtifactCoordinates.parse("bcrypt-3.1.6-1-thiswillerror-cp27-cp27m-macosx_10_6_intel.whl"));

        // too many arguments (7), error in middle
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PypiArtifactCoordinates.parse("bcrypt-3.1.6-1-thiswillerror-cp27-cp27m-macosx_10_6_intel.tar.gz"));
    }

    /**
     * Test that the parser handles the case that too few arguments included in the filename
     */
    @Test
    public void testTooFewArgumentsException()
    {
        // too few arguments (1)
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PypiArtifactCoordinates.parse("bcrypt.whl"));

        // too few arguments (1)
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PypiArtifactCoordinates.parse("bcrypt.tar.gz"));

        // too few arguments (2)
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PypiArtifactCoordinates.parse("bcrypt-3.1.6.whl"));

        // too few arguments (3)
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PypiArtifactCoordinates.parse("bcrypt-3.1.6-cp27.whl"));

        // too few arguments (4)
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PypiArtifactCoordinates.parse("bcrypt-3.1.6-cp27-cp27m.whl"));
    }

    /**
     * Test that the parser handles the case that build tag is specified but empty and if the build tag is 
     * specified but does not start with a number
     */
    @Test
    public void buildTagException()
    {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PypiArtifactCoordinates.parse("bcrypt-3.1.6-1--cp27-cp27m-macosx_10_6_intel.whl"));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PypiArtifactCoordinates.parse("bcrypt-3.1.6-1-test1-cp27-cp27m-macosx_10_6_intel.whl"));
    }

    /**
     * Test that the PypiArtifactCoordinate method toString returns the correct original filename
     */
    @RepeatedTest(31)
    public void testToString(RepetitionInfo repetitionInfo)
    {
        int currentIndex = repetitionInfo.getCurrentRepetition() - 1;

        PypiArtifactCoordinates coordsParsed = PypiArtifactCoordinates.parse(packageExamples.get(currentIndex));

        List<String> parsedCoordinates = parsedPackages.get(currentIndex);

        String expectedPackagePath = String.format("%s/%s/%s",
                                                   parsedCoordinates.get(0),
                                                   parsedCoordinates.get(1),
                                                   packageExamples.get(currentIndex));

        assertThat(coordsParsed.buildPath()).isEqualTo(expectedPackagePath);
    }
}
