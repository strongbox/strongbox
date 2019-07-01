package org.carlspring.strongbox.locator.handlers;

import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.StorageData;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.util.IndexContextHelper;

/**
 * @author Przemyslaw Fusik
 */
public class MavenGroupRepositoryIndexerManagementOperation
        extends MavenIndexerManagementOperation
{

    private final RepositoryIndexManager repositoryIndexManager;

    private final RepositoryData groupRepository;

    public MavenGroupRepositoryIndexerManagementOperation(final ArtifactIndexesService artifactIndexesService,
                                                          final RepositoryIndexManager repositoryIndexManager,
                                                          final RepositoryData groupRepository)
    {
        super(artifactIndexesService);
        this.repositoryIndexManager = repositoryIndexManager;
        this.groupRepository = groupRepository;
    }

    @Override
    protected RepositoryIndexer getRepositoryIndexer()
    {
        StorageData storage = groupRepository.getStorage();
        String contextId = IndexContextHelper.getContextId(storage.getId(),
                                                           groupRepository.getId(),
                                                           IndexTypeEnum.LOCAL.getType());
        return repositoryIndexManager.getRepositoryIndexer(contextId);
    }

}
