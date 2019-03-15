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

package org.carlspring.strongbox.storage.metadata.nuget;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.carlspring.strongbox.storage.metadata.nuget.NugetFormatException;
import org.carlspring.strongbox.storage.metadata.nuget.Nuspec;
import org.carlspring.strongbox.storage.metadata.nuget.Reference;
import org.carlspring.strongbox.storage.metadata.nuget.metadata.Dependency;
import org.carlspring.strongbox.storage.metadata.nuget.metadata.Framework;
import org.carlspring.strongbox.storage.metadata.nuget.metadata.VersionRange;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.semver.Version;

/**
 * @author Dmitry Sviridov
 */
public class NuspecTest
{

    /**
     * Extracts data from an XML document
     *
     * @param xmlData
     *            XML document
     * @param xPath
     *            xPath expression
     * @return string representation of data
     * @throws XPathExpressionException
     *             incorrect xPath expression
     * @throws ParserConfigurationException
     *             XML parser creation error
     * @throws SAXException
     *             invalid XML
     * @throws IOException
     *             data reading error
     */
    private String getStringValue(byte[] xmlData,
                                  String xPath)
        throws XPathExpressionException,
               SAXException,
               IOException,
               ParserConfigurationException
    {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xmlData));
        NamespaceContext context = new CustomContext();
        XPathFactory pathFactory = XPathFactory.newInstance();
        XPath path = pathFactory.newXPath();
        path.setNamespaceContext(context);
        XPathExpression expr = path.compile(xPath);
        return (String) expr.evaluate(doc, XPathConstants.STRING);
    }

    /**
     * Test parsing file specifications from XML
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseMethod()
        throws Exception
    {
        final String fileName = "nuspec/test.nuspec.xml";
        Nuspec result = Nuspec.parse(NugetTestResourceUtil.getAsStream(fileName));
        assertEquals("Neolant.ProjectWise.IsolationLevel.Implementation", result.getId(), "Package ID");
        assertEquals(Version.parse("1.4.7.550"), result.getVersion(), "Package Version");
        assertEquals("Реализация уровня изоляции ProjecWise API", result.getTitle(), "Short description");
        assertEquals("НЕОЛАНТ", result.getOwners(), "Authors");
        assertEquals("НЕОЛАНТ", result.getOwners(), "Owners");
        assertEquals(false, result.isRequireLicenseAcceptance(), "License Verification Required");
        assertEquals("Реализация контрактов уровня изоляции ProjecWise API", result.getDescription(), "Description");
        assertEquals("НЕОЛАНТ", result.getCopyright(), "Rights");
    }

    /**
     * Parsing specification file from XML if file references are provided
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseWithReferences()
        throws Exception
    {
        // GIVEN
        final String fileName = "nuspec/NUnit.nuspec.xml";
        Reference dll = new Reference().setFile("nunit.framework.dll");
        Reference xml = new Reference().setFile("nunit.framework.xml");
        Reference[] references = new Reference[] { dll, xml };
        String[] tags = new String[] { "Unit", "test" };

        // WHEN
        Nuspec result = Nuspec.parse(NugetTestResourceUtil.getAsStream(fileName));

        // THEN
        assertEquals("NUnit", result.getId(), "Package ID");
        assertEquals(Version.parse("2.5.9.10348"), result.getVersion(), "Package Version");
        assertEquals("NUnit", result.getAuthors(), "Authors");
        assertEquals("NUnit", result.getOwners(), "Owners");
        assertEquals(false, result.isRequireLicenseAcceptance(), "License Verification Required");
        assertEquals("Пакет модульного тестирования", result.getDescription(), "Description");
        assertEquals("Copyright 2011", result.getCopyright(), "Rights");
        assertEquals(tags.length, result.getTags().size(), "Number of tags");
        assertArrayEquals(tags, result.getTags().toArray(), "Tags");
        assertEquals(references.length, result.getReferences().size(), "Number of links");
        assertArrayEquals(references, result.getReferences().toArray(), "Links");
    }

    /**
     * Parsing specification file from XML if package dependencies are specified
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseWithDependencies()
        throws Exception
    {
        // GIVEN
        final String fileName = "nuspec/NHibernate.nuspec.xml";
        Dependency dep = new Dependency();
        dep.setId("Iesi.Collections");
        dep.versionRange = VersionRange.parse("3.2.0.4000");
        Dependency[] dependencies = new Dependency[] { dep };
        String[] tags = new String[] { "ORM", "DataBase", "DAL", "ObjectRelationalMapping" };

        // WHEN
        Nuspec result = Nuspec.parse(NugetTestResourceUtil.getAsStream(fileName));

        // THEN
        assertEquals("NHibernate", result.getId(), "Package ID");
        assertEquals(Version.parse("3.2.0.4000"), result.getVersion(), "Package Version");
        assertEquals("NHibernate community, Hibernate community", result.getAuthors(), "Authors");
        assertEquals("NHibernate community, Hibernate community", result.getOwners(), "Owners");
        assertEquals(false, result.isRequireLicenseAcceptance(), "License Verification Required");
        assertEquals(
                     "NHibernate is a mature, open source object-relational mapper for the .NET framework. It's actively developed , fully featured and used in thousands of successful projects.",
                     result.getDescription(),
                     "Description");
        assertEquals(
                     "NHibernate is a mature, open source object-relational mapper for the .NET framework. It's actively developed , fully featured and used in thousands of successful projects.",
                     result.getSummary(),
                     "Short Description");
        assertEquals(tags.length, result.getTags().size(), "Number of tags");
        assertArrayEquals(tags, result.getTags().toArray(), "Tags");
        assertEquals(dependencies.length, result.getDependencies().size(), "Number of dependencies");
        assertArrayEquals(dependencies, result.getDependencies().toArray(), "Dependencies");
    }

    /**
     * Verifying receipt of release notes
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseReleaseNotes()
        throws Exception
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("nuspec/FluentAssertions.nuspec.xml");
        // WHEN
        Nuspec nuspecFile = Nuspec.parse(inputStream);
        // THEN
        assertEquals(
                     "And() extension method to "
                             + "TimeSpanConversionExtensions to support 4.Hours()."
                             + "And(30.Minutes())",
                     nuspecFile.getReleaseNotes(),
                     "Release Notes");
    }

    /**
     * Check for compliance with the old scheme
     * http://schemas.microsoft.com/packaging/2010/07/nuspec.xsd
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseOldScheme()
        throws Exception
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("nuspec/NLog.nuspec.xml");
        // WHEN
        Nuspec nuspecFile = Nuspec.parse(inputStream);
        // THEN
        assertEquals("NLog", nuspecFile.getId(), "Package ID");
    }

    /**
     * Verification of extracting information from a specification whose root
     * element has no namespace
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testParseWithNoNamespaceRootElement()
        throws Exception
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("nuspec/PostSharp.nuspec.xml");
        // WHEN
        Nuspec nuspecFile = Nuspec.parse(inputStream);
        // THEN
        assertEquals("PostSharp", nuspecFile.getId(), "Package ID");
    }

    /**
     * Test of creating a package specification with a fixed dependency version
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testCreateWithFixedDependencyVersion()
        throws Exception
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("nuspec/fixed.dependency.nuspec.xml");
        // WHEN
        Nuspec nuspecFile = Nuspec.parse(inputStream);
        List<Dependency> dependencys = nuspecFile.getDependencies();
        // THEN
        assertEquals(dependencys.size(), 1, "Number of dependencies");
    }

    /**
     * Test of creating a package specification with an incorrect version
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testCreateWithIncorrectVersion()
        throws Exception
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("nuspec/incorrect.version.nuspec.xml");
        // WHEN
        assertThrows(
                     NugetFormatException.class,
                     () -> {
                         Nuspec.parse(inputStream);
                     });
    }

    /**
     * Analysis of the file containing dependencies on the built-in framework of
     * the assemblies
     *
     * @throws NugetFormatException
     *             test XML does not conform to the NuGet specification
     */
    @Test
    public void testParseWithFrameworkAssemblies()
        throws NugetFormatException
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("nuspec/Extended.Wpf.Toolkit.nuspec.xml");
        // WHEN
        Nuspec result = Nuspec.parse(inputStream);
        // THEN
        assertEquals(5, result.getFrameworkAssembly().size());
        assertEquals("PresentationCore", result.getFrameworkAssembly().get(0).getAssemblyName());
        assertEquals(EnumSet.of(Framework.net35, Framework.net40),
                     result.getFrameworkAssembly().get(0).getTargetFrameworks());

        assertEquals("PresentationFramework", result.getFrameworkAssembly().get(1).getAssemblyName());
        assertEquals(EnumSet.of(Framework.net35, Framework.net40),
                     result.getFrameworkAssembly().get(1).getTargetFrameworks());

        assertEquals("WindowsBase", result.getFrameworkAssembly().get(2).getAssemblyName());
        assertEquals(EnumSet.of(Framework.net35, Framework.net40),
                     result.getFrameworkAssembly().get(2).getTargetFrameworks());

        assertEquals("System", result.getFrameworkAssembly().get(3).getAssemblyName());
        assertEquals(EnumSet.of(Framework.net35, Framework.net40),
                     result.getFrameworkAssembly().get(3).getTargetFrameworks());

        assertEquals("System.Xaml", result.getFrameworkAssembly().get(4).getAssemblyName());
        assertEquals(EnumSet.of(Framework.net40), result.getFrameworkAssembly().get(4).getTargetFrameworks());
    }

    /**
     * Analysis of XML containing group dependencies (from version NuGet 2.0)
     *
     * @throws NugetFormatException
     *             test XML does not conform to the NuGet specification
     */
    @Test
    public void testParseWithGroupDependencies()
        throws NugetFormatException
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("nuspec/group.dependencies.nuspec.xml");
        // WHEN
        Nuspec result = Nuspec.parse(inputStream);
        // THEN
        assertNotNull(result);
        assertNotNull(result.getDependencies());
        assertEquals(3, result.getDependencies().size());
        assertEquals(3, result.getDependenciesGroups().size());
        assertNull(result.getDependenciesGroups().get(0).getTargetFramework());
        assertEquals(Framework.net40, result.getDependenciesGroups().get(1).getTargetFramework());
        assertEquals(Framework.sl30, result.getDependenciesGroups().get(2).getTargetFramework());
    }

    /**
     * Check for correctness of work with the new namespace
     *
     * @throws NugetFormatException
     *             test XML does not conform to the NuGet specification
     */
    @Test
    public void testParseNewNamespace()
        throws NugetFormatException
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("nuspec/Spring.Data.nuspec.xml");
        // WHEN
        Nuspec result = Nuspec.parse(inputStream);
        // THEN
        assertNotNull(result);
    }

    /**
     * Test read nuspec with files
     *
     * @throws NugetFormatException
     *             illegal Nuspec XML
     */
    @Test
    public void testParsePackageWithFiles()
        throws NugetFormatException
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("nuspec/v3/Package.with.files.nuspec.xml");
        // WHEN
        Nuspec result = Nuspec.parse(inputStream);
        // THEN
        assertNotNull(result);
    }

    /**
     * Test for correct serialization of release notes
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testReleaseNotesMarshaliing()
        throws Exception
    {
        // GIVEN
        Nuspec nuspecFile = new Nuspec();
        nuspecFile.setReleaseNotes("Test release notes");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // WHEN
        nuspecFile.saveTo(outputStream);
        outputStream.close();
        String result = getStringValue(outputStream.toByteArray(), "/a:package/a:metadata/a:releaseNotes/text ()");
        // THEN
        assertEquals("Test release notes", result);
    }

    /**
     * Verification of recognition specifications, in which the target assembly
     * framework is specified as an empty string
     *
     * @throws NugetFormatException
     *             incorrect format of the test specification file
     */
    @Test
    public void testEmptyStringAssemblyFrameworks()
        throws NugetFormatException
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("nuspec/DockPanelSuite.nuspec.xml");
        // WHEN
        Nuspec nuspecFile = Nuspec.parse(inputStream);
        // THEN
        assertNotNull(nuspecFile);
    }

    /**
     * Verification of reading the package from XSD from 06.2012
     *
     * @throws NugetFormatException
     *             incorrect format of the test specification file
     */
    @Test
    public void testNamespace2013()
        throws NugetFormatException
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("nuspec/NLog.nuspec.2.0.1.xml");
        // WHEN
        Nuspec nuspecFile = Nuspec.parse(inputStream);
        // THEN
        assertNotNull(nuspecFile);
    }

    /**
     * Test package v3
     *
     * @throws NugetFormatException
     *             invalid spec format
     */
    @Test
    public void testReadNuspecV3()
        throws NugetFormatException
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("nuspec/v3/Package.v3.nuspec.xml");
        // WHEN
        Nuspec nuspecFile = Nuspec.parse(inputStream);
        // THEN
        assertNotNull(nuspecFile);
    }

    /**
     * Context for defining namespaces
     */
    private class CustomContext implements NamespaceContext
    {

        @Override
        public String getNamespaceURI(String prefix)
        {
            return "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd";
        }

        @Override
        public String getPrefix(String namespaceURI)
        {
            return null;
        }

        @Override
        public Iterator getPrefixes(String namespaceURI)
        {
            return null;
        }
    }
}
