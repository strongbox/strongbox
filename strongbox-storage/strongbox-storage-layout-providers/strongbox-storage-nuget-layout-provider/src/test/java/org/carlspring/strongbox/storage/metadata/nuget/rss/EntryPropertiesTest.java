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

package org.carlspring.strongbox.storage.metadata.nuget.rss;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThat;

import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;
import org.carlspring.strongbox.storage.metadata.nuget.Dependency;
import org.carlspring.strongbox.storage.metadata.nuget.NugetFormatException;
import org.carlspring.strongbox.storage.metadata.nuget.NugetTestResourceUtil;
import org.carlspring.strongbox.storage.metadata.nuget.Nuspec;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 *
 * @author sviridov
 */
@Execution(CONCURRENT)
public class EntryPropertiesTest
{

    /**
     * Check the conversion of the specification file to the properties of
     * attachments in the RSS message
     *
     * @throws NugetFormatException
     *             the data in the resource does not conform to the NuGet format
     */
    @Test
    public void testConvertNuspecToEntryProperties()
        throws NugetFormatException
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("nuspec/NUnit.nuspec.xml");
        Nuspec nuspecFile = Nuspec.parse(inputStream);
        EntryProperties properties = new EntryProperties();
        
        // WHEN
        properties.setNuspec(nuspecFile);
        
        // THEN
        assertThat(new SemanticVersion(2, 5, 9, ".", "10348")).isEqualTo(properties.getVersion());
        assertThat(properties.getTitle()).isEmpty();
        assertThat(properties.getIconUrl()).isEmpty();
        assertThat(properties.getLicenseUrl()).isEmpty();
        assertThat(properties.getProjectUrl()).isEmpty();
        assertThat(properties.getReportAbuseUrl()).isEmpty();
        assertThat(properties.getRequireLicenseAcceptance()).isFalse();
        assertThat(properties.getDescription()).isEqualTo("Unit Testing Package");
        assertThat(properties.getReleaseNotes()).isEmpty();
        assertThat(properties.getLanguage()).isEmpty();
        assertThat(properties.getPrice()).isEqualTo(Double.valueOf(0));
        assertThat(properties.getDependencies()).isEmpty();
        assertThat(properties.getExternalPackageUri()).isEmpty();
        assertThat(properties.getCategories()).isEmpty();
        assertThat(properties.getCopyright()).isEqualTo("Copyright 2011");
        assertThat(properties.getPackageType()).isEmpty();
        assertThat(properties.getTags().toArray()).containsOnly("Unit", "test");
        assertThat(properties.getSummary()).isEmpty();
    }

    /**
     * Checking the generation of package information with dependencies

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.Assertions.assertThat;
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testConvertNuspecWithDependencies()
        throws Exception
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("nuspec/Dependencies.nuspec.xml");
        Nuspec nuspecFile = Nuspec.parse(inputStream);
        EntryProperties properties = new EntryProperties();
        
        // WHEN
        properties.setNuspec(nuspecFile);
        
        // THEN
        assertThat(properties.getDependencies()).isEqualTo("NLog:2.0.0.2000");
    }

    /**
     * Package Property Recognition (RSS) Test from XML
     *
     * @throws JAXBException
     *             illegal RSS data
     */
    @Test
    public void testParseProperties()
        throws JAXBException
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("rss/entry/properties/NUnit.properties.xml");
        
        // WHEN
        EntryProperties result = EntryProperties.parse(inputStream);
        
        // THEN
        assertThat(new SemanticVersion(2, 5, 9, ".", "10348")).isEqualTo(result.getVersion());
        assertThat(result.getTitle()).isEmpty();
        assertThat(result.getIconUrl()).isEmpty();
        assertThat(result.getLicenseUrl()).isEmpty();
        assertThat(result.getProjectUrl()).isEmpty();
        assertThat(result.getReportAbuseUrl()).isEmpty();
        assertThat(result.getDownloadCount()).isEqualTo(Integer.valueOf(-1));
        assertThat(result.getVersionDownloadCount()).isEqualTo(Integer.valueOf(-1));
        assertThat(result.getRatingsCount()).isEqualTo(Integer.valueOf(0));
        assertThat(result.getVersionRatingsCount()).isEqualTo(Integer.valueOf(-1));
        assertThat(result.getRating()).isEqualTo(Double.valueOf(-1d));
        assertThat(result.getVersionRating()).isEqualTo(Double.valueOf(-1d));
        assertThat(result.getRequireLicenseAcceptance()).isFalse();
        assertThat(result.getDescription()).isEqualTo("Unit Testing Package");
        assertThat(result.getReleaseNotes()).isEmpty();
        assertThat(result.getLanguage()).isEmpty();
        assertThat(DatatypeConverter.parseDateTime("2011-09-23T05:18:55.5327281Z").getTime())
                .isEqualTo(result.getPublished());
        assertThat(result.getPrice()).isEqualTo(Double.valueOf(0d));
        assertThat(result.getDependencies()).isEmpty();
        assertThat("CoknSJBGJ7kao2P6y9E9BuL1IkhP5LLhZ+ImtsgdxzFDpjs0QtRVOV8kxysakJu3cvw5O0hImcnVloCaQ9+Nmg==")
                .isEqualTo(result.getPackageHash());
        assertThat(result.getPackageSize()).isEqualTo(Long.valueOf(214905L));
        assertThat(result.getExternalPackageUri()).isEmpty();
        assertThat(result.getCategories()).isEmpty();
        assertThat(result.getCopyright()).isEmpty();
        assertThat(result.getPackageType()).isEmpty();
        assertThat(result.getTags().toArray(new String[0])).containsOnly("Unit", "test");
        assertThat(result.getIsLatestVersion()).isTrue();
        assertThat(result.getSummary()).isEmpty();
    }

    /**
     * Test of getting a list of dependencies consisting of one element
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testGetDependenciesListFromOneElement()
        throws Exception
    {
        // GIVEN
        EntryProperties properties = new EntryProperties();
        properties.setDependencies("A:1.2.3.4");
        
        // WHEN
        List<Dependency> result = properties.getDependenciesList();
        
        // THEN
        assertThat(result.toArray(new Dependency[0])).containsOnly(Dependency.parseString("A:1.2.3.4"));
    }

    /**
     * Test of getting a list of dependencies consisting of several elements
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testGetDependenciesList()
        throws Exception
    {
        // GIVEN
        EntryProperties properties = new EntryProperties();
        properties.setDependencies("A:1.2.3.4|B:1.2.3.4");
        
        // WHEN
        List<Dependency> result = properties.getDependenciesList();
        
        // THEN
        assertThat(result.toArray(new Dependency[0])).containsOnly(Dependency.parseString("A:1.2.3.4"),
                                                                                Dependency.parseString("B:1.2.3.4"));
    }

    /**
     * A test for obtaining a list of dependencies consisting of several
     * elements separated by a vertical bar.
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testGetDependenciesListPipeLinrSeparated()
        throws Exception
    {
        // GIVEN
        EntryProperties properties = new EntryProperties();
        properties.setDependencies("adjunct-System.DataStructures.SparsePascalSet:2.2.0|"
                + "adjunct-XUnit.Should.BooleanExtensions:2.0.0|"
                + "adjunct-XUnit.Should.ObjectExtensions:2.0.0|"
                + "xunit:1.8.0.1549");
        
        // WHEN
        List<Dependency> result = properties.getDependenciesList();
        
        // THEN
        assertThat(result.toArray(new Dependency[0])).containsOnly(
                                                                                Dependency.parseString("adjunct-System.DataStructures.SparsePascalSet:2.2.0"),
                                                                                Dependency.parseString("adjunct-XUnit.Should.BooleanExtensions:2.0.0"),
                                                                                Dependency.parseString("adjunct-XUnit.Should.ObjectExtensions:2.0.0"),
                                                                                Dependency.parseString("xunit:1.8.0.1549"));

    }

    /**
     * Test of getting a list of dependencies for an incorrect list of
     * dependencies
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testGetDependenciesListFromEmptyString()
        throws Exception
    {
        // GIVEN
        EntryProperties properties = new EntryProperties();
        properties.setDependencies("eres");
        
        // WHEN
        assertThatExceptionOfType(NugetFormatException.class)
                .isThrownBy(properties::getDependenciesList);
    }

    /**
     * Checking build dependency list
     *
     * @throws NugetFormatException
     *             dependency string or dependency version have incorrect format
     */
    @Test
    public void testSetDependenciesList()
        throws NugetFormatException
    {
        // GIVEN
        ArrayList<Dependency> dependencies = new ArrayList<>();
        dependencies.add(Dependency.parseString("package1:1.2.3"));
        dependencies.add(Dependency.parseString("package2:3.2.1"));
        EntryProperties properties = new EntryProperties();
        
        // WHEN
        properties.setDependenciesList(dependencies);
        
        // THEN
        assertThat(properties.getDependencies()).isEqualTo("package1:1.2.3,package2:3.2.1");
    }
}
