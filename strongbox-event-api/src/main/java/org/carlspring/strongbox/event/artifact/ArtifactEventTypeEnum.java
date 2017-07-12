package org.carlspring.strongbox.event.artifact;

/**
 * @author carlspring
 */
public enum ArtifactEventTypeEnum
{

    EVENT_ARTIFACT_DIRECTORY_CREATED(1),

    EVENT_ARTIFACT_FILE_UPLOADING(2),

    EVENT_ARTIFACT_FILE_UPLOADED(3),

    EVENT_ARTIFACT_CHECKSUM_UPLOADED(4),

    EVENT_ARTIFACT_FILE_DOWNLOADING(5),

    EVENT_ARTIFACT_FILE_DOWNLOADED(6),

    EVENT_ARTIFACT_FILE_UPDATED(7),

    EVENT_ARTIFACT_MOVED(8),

    EVENT_ARTIFACT_ARCHIVED(9),

    EVENT_ARTIFACT_DELETED(10);


    private int type;


    ArtifactEventTypeEnum(int type)
    {
        this.type = type;
    }

    public int getType()
    {
        return type;
    }

}
