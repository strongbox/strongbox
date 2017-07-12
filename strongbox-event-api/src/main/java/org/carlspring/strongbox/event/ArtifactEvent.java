package org.carlspring.strongbox.event;

/**
 * @author mtodorov
 */
public class ArtifactEvent extends Event
{

    public static final int EVENT_ARTIFACT_UPLOADED = 1;

    public static final int EVENT_ARTIFACT_CHECKSUM_UPLOADED = 2;

    public static final int EVENT_ARTIFACT_DOWNLOADED = 3;

    public static final int EVENT_ARTIFACT_CHANGED = 4;

    public static final int EVENT_ARTIFACT_MOVED = 5;

    public static final int EVENT_ARTIFACT_ARCHIVED = 6;

    public static final int EVENT_ARTIFACT_DELETED = 7;


    public ArtifactEvent(int type)
    {
        setType(type);
    }

}
