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

import org.carlspring.strongbox.artifact.coordinates.versioning.SemanticVersion;
import org.carlspring.strongbox.storage.metadata.nuget.NugetTestResourceUtil;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * Filter test that carries the declaration of the specified namespace prefixes
 * in
 * root element of the XML document
 *
 * @author sviridov
 */
@Execution(CONCURRENT)
public class NugetPrefixFilterTest
{

    /**
     * Artificial XML Test
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testFilterClass()
        throws Exception
    {

        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("customxml/test.prefix.xml");
        InputSource inputSource = new InputSource(inputStream);
        XMLReader reader = XMLReaderFactory.createXMLReader();
        Map<String, String> uriToPrefix = new HashMap<>();
        uriToPrefix.put("element_namespace1", "m1");
        uriToPrefix.put("element_namespace2", "m2");
        uriToPrefix.put("attribute_namespace1", "m3");
        NugetPrefixFilter filter = new NugetPrefixFilter(uriToPrefix);
        filter.setParent(reader);
        SAXSource source = new SAXSource(filter, inputSource);

        // WHEN
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter stringWriter = new StringWriter();
        StreamResult result = new StreamResult(stringWriter);
        transformer.transform(source, result);
        String xmlString = stringWriter.toString();

        // THEN
        assertThat(xmlString.contains("<m2:l/>")).isTrue();
        assertThat(xmlString.contains("<m2:k m3:val=\"value\">")).isTrue();
        assertThat(xmlString.contains("</m1:root>")).isTrue();
    }

    /**
     * Test for real rss feeds
     *
     * @throws Exception
     *             error during the test
     */
    @Test
    public void testClearRealRss()
        throws Exception
    {
        // GIVEN
        InputStream inputStream = NugetTestResourceUtil.getAsStream("customxml/real.data.xml");
        InputSource inputSource = new InputSource(inputStream);
        XMLReader reader = XMLReaderFactory.createXMLReader();
        Map<String, String> uriToPrefix = new HashMap<>();
        uriToPrefix.put("http://www.w3.org/2005/Atom", "atom");
        uriToPrefix.put("http://schemas.microsoft.com/ado/2007/08/dataservices/metadata", "m");
        uriToPrefix.put("http://schemas.microsoft.com/ado/2007/08/dataservices/scheme", "ds");
        uriToPrefix.put("http://schemas.microsoft.com/ado/2007/08/dataservices", "d");
        NugetPrefixFilter filter = new NugetPrefixFilter(uriToPrefix);
        filter.setParent(reader);
        SAXSource source = new SAXSource(filter, inputSource);

        // WHEN
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(byteArrayOutputStream);
        transformer.transform(source, result);
        byteArrayOutputStream.flush();

        // THEN
        PackageFeed feed = PackageFeed.parse(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        assertThat(feed.getEntries()).as("Number of packages").hasSize(1);
        PackageEntry entry = feed.getEntries().get(0);
        assertThat(entry.getContent().getSrc())
                .as("Content")
                .isEqualTo("http://ws209.neolant.loc:8084/nuget/download/FluentAssertions/1.6.0");
        assertThat(entry.getProperties().getDownloadCount())
                .as("Download Count")
                .isEqualTo(Integer.valueOf(-1));
        assertThat(entry.getProperties().getVersion())
                .as("Package Version")
                .isEqualTo(SemanticVersion.parse("1.6.0"));
    }
}
