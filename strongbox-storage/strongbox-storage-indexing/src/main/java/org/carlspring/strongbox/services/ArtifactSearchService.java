package org.carlspring.strongbox.services;

import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.indexing.SearchResults;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;

/**
 * @author mtodorov
 */
public interface ArtifactSearchService
{

    SearchResults search(SearchRequest searchRequest)
            throws IOException, ParseException;

    boolean contains(SearchRequest searchRequest)
            throws IOException, ParseException;

}
