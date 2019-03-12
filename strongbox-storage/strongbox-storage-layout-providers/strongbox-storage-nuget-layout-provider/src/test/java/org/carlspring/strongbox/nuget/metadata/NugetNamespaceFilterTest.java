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

package org.carlspring.strongbox.nuget.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.carlspring.strongbox.nuget.NugetNamespaceFilter;
import org.carlspring.strongbox.nuget.NugetTestResources;
import org.carlspring.strongbox.nuget.Nuspec;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author sviridov
 */
public class NugetNamespaceFilterTest
{

    /**
     * Verifying that the old namespace is replaced with a new one
     *
     * @throws Exception
     *             error in test process
     */
    @Test
    public void testChangeUri()
        throws Exception
    {
        // GIVEN
        InputStream inputStream = NugetTestResources.getAsStream("nuspec/NLog.nuspec.xml");
        InputSource inputSource = new InputSource(inputStream);

        XMLReader reader = XMLReaderFactory.createXMLReader();
        NugetNamespaceFilter inFilter = new NugetNamespaceFilter();
        inFilter.setParent(reader);

        // WHEN
        SAXSource source = new SAXSource(inFilter, inputSource);

        // THEN
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
        DOMResult domResult = new DOMResult(documentBuilder.newDocument());
        transformer.transform(source, domResult);
        String namespace = domResult.getNode().getFirstChild().getNamespaceURI();

        assertEquals(Nuspec.NUSPEC_XML_NAMESPACE_2011, namespace, "Namespace");
    }
}
