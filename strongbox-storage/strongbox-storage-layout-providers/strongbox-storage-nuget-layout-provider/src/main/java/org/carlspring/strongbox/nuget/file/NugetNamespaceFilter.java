package org.carlspring.strongbox.nuget.file;


import java.util.Arrays;
import java.util.HashSet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

public class NugetNamespaceFilter
        extends XMLFilterImpl
{

    private final HashSet<String> sourceUris;
    private final String targetUri;

    public NugetNamespaceFilter()
    {
        this(new String[]{ "http://schemas.microsoft.com/packaging/2010/07/nuspec.xsd",
                           "",
                           "http://schemas.microsoft.com/packaging/2011/10/nuspec.xsd",
                           "http://schemas.microsoft.com/packaging/2012/06/nuspec.xsd",
                           "http://schemas.microsoft.com/packaging/2013/05/nuspec.xsd",
                           "http://schemas.microsoft.com/packaging/2015/06/nuspec.xsd",
                           "http://schemas.microsoft.com/packaging/2013/01/nuspec.xsd" },
             "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd");
    }

    public NugetNamespaceFilter(String[] sourceUris,
                                String targetUri)
    {
        this.sourceUris = new HashSet();
        this.sourceUris.addAll(Arrays.asList(sourceUris));
        this.targetUri = targetUri;
    }

    public void endElement(String uri,
                           String localName,
                           String qName)
            throws SAXException
    {
        if (this.sourceUris.contains(uri))
        {
            uri = this.targetUri;
        }

        super.endElement(uri, localName, qName);
    }

    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException
    {
        if (this.sourceUris.contains(uri))
        {
            uri = this.targetUri;
        }

        super.startElement(uri, localName, qName, atts);
    }
}