package org.carlspring.strongbox.event.repository;

/**
 * @author carlspring
 */
public enum RepositoryEventTypeEnum
{

    EVENT_REPOSITORY_CREATED(1),

    EVENT_REPOSITORY_CHANGED(2),

    EVENT_REPOSITORY_DELETED(3),

    EVENT_REPOSITORY_PUT_IN_SERVICE(4),

    EVENT_REPOSITORY_PUT_OUT_OF_SERVICE(5),

    EVENT_REPOSITORY_REMOTE_UNAVAILABLE(6),

    EVENT_REPOSITORY_REMOTE_AVAILABLE(7);

    private int type;


    RepositoryEventTypeEnum(int type)
    {
        this.type = type;
    }

    public int getType()
    {
        return type;
    }

}
