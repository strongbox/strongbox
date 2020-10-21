package org.carlspring.strongbox.event.repository;

import org.carlspring.strongbox.event.AbstractEventListenerRegistry;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class RepositoryEventListenerRegistry
        extends AbstractEventListenerRegistry
{


    public void dispatchEmptyTrashEvent(String storageId,
                                        String repositoryId)
    {
        RepositoryEvent event = new RepositoryEvent(storageId,
                                                    repositoryId,
                                                    RepositoryEventTypeEnum.EVENT_REPOSITORY_EMTPY_TRASH.getType());

        dispatchEvent(event);
    }

    public void dispatchEmptyTrashForAllRepositoriesEvent()
    {
        RepositoryEvent event = new RepositoryEvent(null,
                                                    null,
                                                    RepositoryEventTypeEnum.EVENT_REPOSITORY_EMTPY_TRASH_FOR_ALL_REPOSITORIES.getType());

        dispatchEvent(event);
    }

    public void dispatchUndeleteTrashEvent(String storageId,
                                           String repositoryId)
    {
        RepositoryEvent event = new RepositoryEvent(storageId,
                                                    repositoryId,
                                                    RepositoryEventTypeEnum.EVENT_REPOSITORY_UNDELETE_TRASH.getType());

        dispatchEvent(event);
    }

    public void dispatchUndeleteTrashForAllRepositoriesEvent()
    {
        RepositoryEvent event = new RepositoryEvent(null,
                                                    null,
                                                    RepositoryEventTypeEnum.EVENT_REPOSITORY_UNDELETE_TRASH_FOR_ALL_REPOSITORIES.getType());

        dispatchEvent(event);
    }

}
