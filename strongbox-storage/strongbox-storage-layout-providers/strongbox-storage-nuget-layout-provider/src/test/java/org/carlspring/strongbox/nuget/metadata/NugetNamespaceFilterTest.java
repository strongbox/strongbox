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

public class NugetNamespaceFilterTest {

    /**
     * Verifying that the old namespace is replaced with a new one
     *
     * @throws Exception error in test process
     */
    @Test
    public void testChangeUri() throws Exception {
        //GIVEN
        InputStream inputStream = NugetTestResources.getAsStream("nuspec/NLog.nuspec.xml");
        InputSource inputSource = new InputSource(inputStream);

        XMLReader reader = XMLReaderFactory.createXMLReader();
        NugetNamespaceFilter inFilter = new NugetNamespaceFilter();
        inFilter.setParent(reader);

        //WHEN
        SAXSource source = new SAXSource(inFilter, inputSource);

        //THEN
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
