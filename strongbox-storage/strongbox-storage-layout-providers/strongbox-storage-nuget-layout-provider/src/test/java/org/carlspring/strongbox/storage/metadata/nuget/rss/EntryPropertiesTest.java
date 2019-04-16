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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;

import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;
import org.carlspring.strongbox.storage.metadata.nuget.Dependency;
import org.carlspring.strongbox.storage.metadata.nuget.NugetFormatException;
import org.carlspring.strongbox.storage.metadata.nuget.NugetTestResourceUtil;
import org.carlspring.strongbox.storage.metadata.nuget.Nuspec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsArrayContainingInAnyOrder.arrayContainingInAnyOrder;

/**
 *
 * @author sviridov
 */
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
        assertEquals(new SemanticVersion(2, 5, 9, ".", "10348"), properties.getVersion());
        assertEquals("", properties.getTitle());
        assertEquals("", properties.getIconUrl());
        assertEquals("", properties.getLicenseUrl());
        assertEquals("", properties.getProjectUrl());
        assertEquals("", properties.getReportAbuseUrl());
        assertEquals(false, properties.getRequireLicenseAcceptance());
        assertEquals("Unit Testing Package", properties.getDescription());
        assertEquals("", properties.getReleaseNotes());
        assertEquals("", properties.getLanguage());
        assertEquals(Double.valueOf(0), properties.getPrice());
        assertEquals("", properties.getDependencies());
        assertEquals("", properties.getExternalPackageUri());
        assertEquals("", properties.getCategories());
        assertEquals("Copyright 2011", properties.getCopyright());
        assertEquals("", properties.getPackageType());
        assertThat(properties.getTags().toArray(), arrayContainingInAnyOrder("Unit", "test"));
        assertEquals("", properties.getSummary());
    }

    /**
     * Checking the generation of package information with dependencies
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
        assertEquals("NLog:2.0.0.2000", properties.getDependencies());
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
        assertEquals(new SemanticVersion(2, 5, 9, ".", "10348"), result.getVersion());
        assertEquals("", result.getTitle());
        assertEquals("", result.getIconUrl());
        assertEquals("", result.getLicenseUrl());
        assertEquals("", result.getProjectUrl());
        assertEquals("", result.getReportAbuseUrl());
        assertEquals(Integer.valueOf(-1), result.getDownloadCount());
        assertEquals(Integer.valueOf(-1), result.getVersionDownloadCount());
        assertEquals(Integer.valueOf(0), result.getRatingsCount());
        assertEquals(Integer.valueOf(-1), result.getVersionRatingsCount());
        assertEquals(Double.valueOf(-1d), result.getRating());
        assertEquals(Double.valueOf(-1d), result.getVersionRating());
        assertEquals(false, result.getRequireLicenseAcceptance());
        assertEquals("Unit Testing Package", result.getDescription());
        assertEquals("", result.getReleaseNotes());
        assertEquals("", result.getLanguage());
        assertEquals(DatatypeConverter.parseDateTime("2011-09-23T05:18:55.5327281Z").getTime(),
                     result.getPublished());
        assertEquals(Double.valueOf(0d), result.getPrice());
        assertEquals("", result.getDependencies());
        assertEquals("CoknSJBGJ7kao2P6y9E9BuL1IkhP5LLhZ+ImtsgdxzFDpjs0QtRVOV8kxysakJu3cvw5O0hImcnVloCaQ9+Nmg==",
                     result.getPackageHash());
        assertEquals(Long.valueOf(214905l), result.getPackageSize());
        assertEquals("", result.getExternalPackageUri());
        assertEquals("", result.getCategories());
        assertEquals("", result.getCopyright());
        assertEquals("", result.getPackageType());
        assertThat(result.getTags().toArray(new String[0]), arrayContainingInAnyOrder("Unit", "test"));
        assertEquals(true, result.getIsLatestVersion());
        assertEquals("", result.getSummary());
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
        assertThat(result.toArray(new Dependency[0]), arrayContainingInAnyOrder(Dependency.parseString("A:1.2.3.4")));
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
        assertThat(result.toArray(new Dependency[0]), arrayContainingInAnyOrder(Dependency.parseString("A:1.2.3.4"),
                                                                                Dependency.parseString("B:1.2.3.4")));
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
        assertThat(result.toArray(new Dependency[0]), arrayContainingInAnyOrder(
                                                                                Dependency.parseString("adjunct-System.DataStructures.SparsePascalSet:2.2.0"),
                                                                                Dependency.parseString("adjunct-XUnit.Should.BooleanExtensions:2.0.0"),
                                                                                Dependency.parseString("adjunct-XUnit.Should.ObjectExtensions:2.0.0"),
                                                                                Dependency.parseString("xunit:1.8.0.1549")));

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
        assertThrows(
                     NugetFormatException.class,
                     () -> {
                         properties.getDependenciesList();
                     });
        ;
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
        ArrayList<Dependency> dependencys = new ArrayList<>();
        dependencys.add(Dependency.parseString("package1:1.2.3"));
        dependencys.add(Dependency.parseString("package2:3.2.1"));
        EntryProperties properties = new EntryProperties();
        
        // WHEN
        properties.setDependenciesList(dependencys);
        
        // THEN
        assertEquals("package1:1.2.3,package2:3.2.1", properties.getDependencies());
    }
}
