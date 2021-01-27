package org.carlspring.strongbox.artifact.coordinates;

import org.carlspring.strongbox.domain.RpmPackageType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * Test class for testing functionality of parsing, creating and validating Rpm artifact coordinates
 * @author Ilya Shatalov <ilya@alov.me>
 */
class RpmArtifactCoordinatesTest 
{

    private static List<List<String>> parsedPackages = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList("libqxt-qt5",                 "0.7.0",    "0.15.20130718giteaf6872f6ad4.fc30",            "x86_64",   "",     "rpm")),
            new ArrayList<>(Arrays.asList("atinject",                   "1",        "28.20100611svn86.module_f28+3939+dc18cd75",    "noarch",   "",     "rpm")),
            new ArrayList<>(Arrays.asList("apache-commons-lang3",       "3.7",      "3.module_f28+3939+dc18cd75",                   "noarch",   "",     "rpm")),
            new ArrayList<>(Arrays.asList("wingpanel-indicator-sound",  "2.1.3",    "1.fc30",                                       "x86_64",   "",     "rpm")),
            new ArrayList<>(Arrays.asList("libglvnd",                   "1.1.0",    "4.gitf92208b.fc30",                            "i686",     "",     "rpm")),
            new ArrayList<>(Arrays.asList("apache-commons-lang3",       "3.7",      "3.module_f28+3939+dc18cd75",                   "noarch",   "",     "rpm")),
            new ArrayList<>(Arrays.asList("libglvnd",                   "1.1.0",    "4.gitf92208b.fc30",                            "i686",     "",     "rpm")),
            new ArrayList<>(Arrays.asList("totem",                      "3.32.1",   "1.fc30",                                       "x86_64",   "",     "rpm")),
            new ArrayList<>(Arrays.asList("libsigc++20",                "2.10.2",   "1.fc30",                                       "x86_64",   "",     "rpm")),
            new ArrayList<>(Arrays.asList("egl-wayland",                "1.1.3",    "1.fc30",                                       "x86_64",   "",     "rpm")),
            new ArrayList<>(Arrays.asList("urw-base35-d050000l-fonts",  "20170801", "12.fc30",                                      "noarch",   "",     "rpm")),
            new ArrayList<>(Arrays.asList("libbabeltrace",              "1.5.6",    "2.fc30",                                       "x86_64",   "",     "rpm")),
            new ArrayList<>(Arrays.asList("redhat-lsb-submod-security", "4.1",      "47.fc30",                                      "x86_64",   "",     "rpm")),
            new ArrayList<>(Arrays.asList("cdi-api",                    "1.2",      "8.module_f28+3939+dc18cd75",                   "noarch",   "",     "rpm")),
            new ArrayList<>(Arrays.asList("libstdc++",                  "9.2.1",    "1.fc30",                                       null,       "src",  "rpm")),
            new ArrayList<>(Arrays.asList("gmp-c++",                    "6.1.2",    "10.fc30",                                      null,       "src",  "rpm"))));

    private static List<String> packageExamples = Arrays.asList(
            "libqxt-qt5-0.7.0-0.15.20130718giteaf6872f6ad4.fc30.x86_64.rpm",
            "atinject-1-28.20100611svn86.module_f28+3939+dc18cd75.noarch.rpm",
            "apache-commons-lang3-3.7-3.module_f28+3939+dc18cd75.noarch.rpm",
            "wingpanel-indicator-sound-2.1.3-1.fc30.x86_64.rpm",
            "libglvnd-1.1.0-4.gitf92208b.fc30.i686.rpm",
            "apache-commons-lang3-3.7-3.module_f28+3939+dc18cd75.noarch.rpm",
            "libglvnd-1.1.0-4.gitf92208b.fc30.i686.rpm",
            "totem-3.32.1-1.fc30.x86_64.rpm",
            "libsigc++20-2.10.2-1.fc30.x86_64.rpm",
            "egl-wayland-1.1.3-1.fc30.x86_64.rpm",
            "urw-base35-d050000l-fonts-20170801-12.fc30.noarch.rpm",
            "libbabeltrace-1.5.6-2.fc30.x86_64.rpm",
            "redhat-lsb-submod-security-4.1-47.fc30.x86_64.rpm",
            "cdi-api-1.2-8.module_f28+3939+dc18cd75.noarch.rpm",
            "libstdc++-9.2.1-1.fc30.src.rpm",
            "gmp-c++-6.1.2-10.fc30.src.rpm");


    @RepeatedTest(16)
    void testParsePackageFromPath(RepetitionInfo repetitionInfo)
    {
        RpmArtifactCoordinates actualCoord = RpmArtifactCoordinates.parse(packageExamples.get(repetitionInfo.getCurrentRepetition() - 1));
        List<String> expectedCoord = parsedPackages.get(repetitionInfo.getCurrentRepetition() - 1);


        assertEquals(expectedCoord.get(0), actualCoord.getId());
        assertEquals(expectedCoord.get(1), actualCoord.getVersion());
        assertEquals(expectedCoord.get(2), actualCoord.getRelease());
        assertEquals(expectedCoord.get(3), actualCoord.getArchitecture());
        assertEquals(expectedCoord.get(4), actualCoord.getPackageType());
    }

    @RepeatedTest(16)
    void testGettingPath(RepetitionInfo repetitionInfo)
    {
        RpmArtifactCoordinates actualCoord = RpmArtifactCoordinates.parse(packageExamples.get(repetitionInfo.getCurrentRepetition() - 1));

        assertEquals(packageExamples.get(repetitionInfo.getCurrentRepetition() - 1), actualCoord.buildPath());
    }

    @RepeatedTest(16)
    void testDropVersion(RepetitionInfo repetitionInfo)
    {
        RpmArtifactCoordinates actualCoord = RpmArtifactCoordinates.parse(packageExamples.get(repetitionInfo.getCurrentRepetition() - 1));

        assertFalse(actualCoord.getCoordinates().containsKey(actualCoord.getVersion()));
    }
}
