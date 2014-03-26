package org.carlspring.strongbox.storage.indexing;

import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Test;

import java.io.IOException;

public class IndexingTest
{
    @Test
    public void testIndexing() throws PlexusContainerException, ComponentLookupException, IOException
    {
        Indexing.index();
    }
}
