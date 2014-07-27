package org.carlspring.strongbox.rest.serialization.search;

import org.carlspring.strongbox.storage.indexing.SearchResults;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author mtodorov
 */
public interface ArtifactSearchSerializer
{

    void write(SearchResults searchResults, OutputStream os, boolean indent)
            throws XMLStreamException, IOException;

}
