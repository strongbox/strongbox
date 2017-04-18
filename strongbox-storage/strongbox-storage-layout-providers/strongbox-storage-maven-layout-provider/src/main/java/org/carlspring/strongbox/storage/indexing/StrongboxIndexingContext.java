package org.carlspring.strongbox.storage.indexing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.index.context.DefaultIndexingContext;
import org.apache.maven.index.context.ExistingLuceneIndexMismatchException;
import org.apache.maven.index.context.IndexCreator;

public class StrongboxIndexingContext extends DefaultIndexingContext
{

    public StrongboxIndexingContext(String id, String repositoryId, File repository, File indexDirectoryFile,
            String repositoryUrl, String indexUpdateUrl, List<? extends IndexCreator> indexCreators,
            boolean reclaimIndex) throws IOException, ExistingLuceneIndexMismatchException
    {
        super(id, repositoryId, repository, indexDirectoryFile, repositoryUrl, indexUpdateUrl, indexCreators,
                reclaimIndex);
    }

    @Override
    protected void setIndexDirectoryFile(File dir)
                                                   throws IOException
    {
        if (dir == null)
        {
            return;
        }
        super.setIndexDirectoryFile(dir);
    }

}
