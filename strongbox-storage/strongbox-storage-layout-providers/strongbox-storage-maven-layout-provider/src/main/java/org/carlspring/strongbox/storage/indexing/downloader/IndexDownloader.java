package org.carlspring.strongbox.storage.indexing.downloader;

import org.carlspring.strongbox.config.MavenIndexerEnabledCondition;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;

import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdateResult;
import org.apache.maven.index.updater.IndexUpdater;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("indexDownloader")
@Conditional(MavenIndexerEnabledCondition.class)
public class IndexDownloader
{

    private static final Logger logger = LoggerFactory.getLogger(IndexDownloader.class);

    @Inject
    private IndexUpdater indexUpdater;

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    @Inject
    private ResourceFetcherFactory resourceFetcherFactory;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    public IndexDownloader()
    {
    }

    public void download(IndexDownloadRequest request)
            throws IOException, ComponentLookupException
    {
        IndexingContext indexingContext = repositoryIndexManager.getRepositoryIndexer(
                                                                             request.getStorageId() + ":" +
                                                                             request.getRepositoryId() + ":" + "remote")
                                                                .getIndexingContext();

        // Update the index (an incremental update will be performed,
        // if this is not the first run and the files are not deleted.

        logger.debug("Updating remote index for " + request.getRepositoryId() + "...");
        logger.debug("(This might take a while on first run, so please be patient)!");

        IndexUpdateRequest updateRequest = new IndexUpdateRequest(indexingContext,
                                                                  resourceFetcherFactory.createIndexResourceFetcher(
                                                                          request.getRemoteRepositoryURL(),
                                                                          proxyRepositoryConnectionPoolConfigurationService.getHttpClient()));

        updateRequest.setIndexTempDir(RepositoryFiles.temporary(repositoryPathResolver.resolve(request.getRepository())).toFile());

        IndexUpdateResult updateResult = indexUpdater.fetchAndUpdateIndex(updateRequest);

        Date contextCurrentTimestamp = indexingContext.getTimestamp();
        if (updateResult.isFullUpdate())
        {
            logger.debug("Performed a full index update for " + request.getStorageId() + ":" +
                         request.getRepositoryId() + ".");
        }
        else if (updateResult.getTimestamp().equals(contextCurrentTimestamp))
        {
            logger.debug("No update required for remote index " +
                         request.getStorageId() + ":" + request.getRepositoryId() + "," +
                         " as the index is up to date!");
        }
        else
        {
            logger.debug("Performed an incremental update, with changes covering the period between " +
                         contextCurrentTimestamp + " - " + updateResult.getTimestamp() + ".");
        }
    }

}
