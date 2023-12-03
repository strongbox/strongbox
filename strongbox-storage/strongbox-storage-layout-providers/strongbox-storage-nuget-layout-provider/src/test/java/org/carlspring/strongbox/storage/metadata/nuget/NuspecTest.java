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

import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Dmitry Sviridov
 */
@Execution(CONCURRENT)
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
        assertThat(result.getId()).as("Package ID").isEqualTo("Neolant.ProjectWise.IsolationLevel.Implementation");
        assertThat(result.getVersion()).as("Package Version").isEqualTo(SemanticVersion.parse("1.4.7.550"));
        assertThat(result.getTitle()).as("Short description").isEqualTo("Implementing the ProjecWise API isolation level");
        assertThat(result.getOwners()).as("Authors").isEqualTo("NEOLANT");
        assertThat(result.getOwners()).as("Owners").isEqualTo("NEOLANT");
        assertThat(result.isRequireLicenseAcceptance()).as("License Verification Required").isFalse();
        assertThat(result.getDescription()).as("Description").isEqualTo("Implementing ProjecWise API isolation level contracts");
        assertThat(result.getCopyright()).as("Rights").isEqualTo("NEOLANT");
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
        assertThat(result.getId()).as("Package ID").isEqualTo("NUnit");
        assertThat(result.getVersion()).as("Package Version").isEqualTo(SemanticVersion.parse("2.5.9.10348"));
        assertThat(result.getAuthors()).as("Authors").isEqualTo("NUnit");
        assertThat(result.getOwners()).as("Owners").isEqualTo("NUnit");
        assertThat(result.isRequireLicenseAcceptance()).as("License Verification Required").isFalse();
        assertThat(result.getDescription()).as("Description").isEqualTo("Unit Testing Package");
        assertThat(result.getCopyright()).as("Rights").isEqualTo("Copyright 2011");
        assertThat(result.getTags()).as("Number of tags").hasSize(tags.length);
        assertThat(result.getTags().toArray()).as("Tags").isEqualTo(tags);
        assertThat(result.getReferences()).as("Number of links").hasSize(references.length);
        assertThat(result.getReferences().toArray()).as("Links").isEqualTo(references);
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
        assertThat(result.getId()).as("Package ID").isEqualTo("NHibernate");
        assertThat(result.getVersion()).as("Package Version").isEqualTo(SemanticVersion.parse("3.2.0.4000"));
        assertThat("NHibernate community, Hibernate community").as("Authors").isEqualTo(result.getAuthors());
        assertThat("NHibernate community, Hibernate community").as("Owners").isEqualTo(result.getOwners());
        assertThat(result.isRequireLicenseAcceptance()).as("License Verification Required").isFalse();
        assertThat("NHibernate is a mature, open source object-relational mapper for the .NET framework. It's actively developed , fully featured and used in thousands of successful projects.")
                .as("Description")
                .isEqualTo(result.getDescription());
        assertThat("NHibernate is a mature, open source object-relational mapper for the .NET framework. It's actively developed , fully featured and used in thousands of successful projects.")
                .as("Short Description")
                .isEqualTo(result.getSummary());
        assertThat(result.getTags()).as("Number of tags").hasSize(tags.length);
        assertThat(result.getTags().toArray()).as("Tags").isEqualTo(tags);
        assertThat(result.getDependencies()).as("Number of dependencies").hasSize(dependencies.length);
        assertThat(result.getDependencies().toArray()).as("Dependencies").isEqualTo(dependencies);
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
        assertThat("And() extension method to "
                             + "TimeSpanConversionExtensions to support 4.Hours()."
                             + "And(30.Minutes())")
                .as("Release Notes")
                .isEqualTo(nuspecFile.getReleaseNotes());
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
        assertThat(nuspecFile.getId()).as("Package ID").isEqualTo("NLog");
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
        assertThat(nuspecFile.getId()).as("Package ID").isEqualTo("PostSharp");
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
        List<Dependency> dependencies = nuspecFile.getDependencies();
        // THEN
        assertThat(dependencies).as("Number of dependencies").hasSize(1);
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
        assertThatExceptionOfType(NugetFormatException.class)
                .isThrownBy(() -> Nuspec.parse(inputStream));
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
        assertThat(result.getFrameworkAssembly()).hasSize(5);
        assertThat(result.getFrameworkAssembly().get(0).getAssemblyName()).isEqualTo("PresentationCore");
        assertThat(EnumSet.of(Framework.net35, Framework.net40))
                .isEqualTo(result.getFrameworkAssembly().get(0).getTargetFrameworks());

        assertThat(result.getFrameworkAssembly().get(1).getAssemblyName()).isEqualTo("PresentationFramework");
        assertThat(EnumSet.of(Framework.net35, Framework.net40))
                .isEqualTo(result.getFrameworkAssembly().get(1).getTargetFrameworks());

        assertThat(result.getFrameworkAssembly().get(2).getAssemblyName()).isEqualTo("WindowsBase");
        assertThat(EnumSet.of(Framework.net35, Framework.net40))
                .isEqualTo(result.getFrameworkAssembly().get(2).getTargetFrameworks());

        assertThat(result.getFrameworkAssembly().get(3).getAssemblyName()).isEqualTo("System");
        assertThat(EnumSet.of(Framework.net35, Framework.net40))
                .isEqualTo(result.getFrameworkAssembly().get(3).getTargetFrameworks());

        assertThat(result.getFrameworkAssembly().get(4).getAssemblyName()).isEqualTo("System.Xaml");
        assertThat(result.getFrameworkAssembly().get(4).getTargetFrameworks()).isEqualTo(EnumSet.of(Framework.net40));
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
        assertThat(result).isNotNull();
        assertThat(result.getDependencies()).isNotNull();
        assertThat(result.getDependencies()).hasSize(3);
        assertThat(result.getDependenciesGroups()).hasSize(3);
        assertThat(result.getDependenciesGroups().get(0).getTargetFramework()).isNull();
        assertThat(result.getDependenciesGroups().get(1).getTargetFramework()).isEqualTo(Framework.net40);
        assertThat(result.getDependenciesGroups().get(2).getTargetFramework()).isEqualTo(Framework.sl30);
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
        assertThat(result).isNotNull();
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
        assertThat(result).isNotNull();
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
        assertThat(result).isEqualTo("Test release notes");
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
        assertThat(nuspecFile).isNotNull();
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
        assertThat(nuspecFile).isNotNull();
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
        assertThat(nuspecFile).isNotNull();
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
