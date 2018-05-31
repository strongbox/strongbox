package org.carlspring.strongbox.services;

import java.io.IOException;

import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.storage.search.SearchResults;

public interface AqlSearchService
{

    public SearchResults search(Selector<ArtifactEntry> selector)
        throws IOException;

}
