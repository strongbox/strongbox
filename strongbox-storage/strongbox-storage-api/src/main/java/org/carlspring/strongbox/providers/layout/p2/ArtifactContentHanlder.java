package org.carlspring.strongbox.providers.layout.p2;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class ArtifactContentHanlder<V>
        implements ContentHandler
{

    private final P2Collector<V> collector;

    public ArtifactContentHanlder(P2Collector<V> collector)
    {
        this.collector = collector;
    }

    @Override
    public void setDocumentLocator(Locator locator)
    {

    }

    @Override
    public void startDocument()
            throws SAXException
    {

    }

    @Override
    public void endDocument()
            throws SAXException
    {

    }

    @Override
    public void startPrefixMapping(String prefix,
                                   String uri)
            throws SAXException
    {

    }

    @Override
    public void endPrefixMapping(String prefix)
            throws SAXException
    {

    }

    @Override
    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException
    {
        collector.accept(localName, atts);
    }

    @Override
    public void endElement(String uri,
                           String localName,
                           String qName)
            throws SAXException
    {

    }

    @Override
    public void characters(char[] ch,
                           int start,
                           int length)
            throws SAXException
    {

    }

    @Override
    public void ignorableWhitespace(char[] ch,
                                    int start,
                                    int length)
            throws SAXException
    {

    }

    @Override
    public void processingInstruction(String target,
                                      String data)
            throws SAXException
    {

    }

    @Override
    public void skippedEntity(String name)
            throws SAXException
    {

    }

    public V get()
    {
        return collector.get();
    }
}
