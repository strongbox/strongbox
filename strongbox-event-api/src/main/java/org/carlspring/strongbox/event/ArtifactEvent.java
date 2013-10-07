package org.carlspring.strongbox.event;

/**
 * @author mtodorov
 */
public class ArtifactEvent
{

    public static final int EVENT_ARTIFACT_UPLOADED = 1;

    public static final int EVENT_ARTIFACT_CHECKSUM_UPLOADED = 2;

    public static final int EVENT_ARTIFACT_DOWNLOADED = 3;

    public static final int EVENT_ARTIFACT_CHANGED = 4;

    public static final int EVENT_ARTIFACT_MOVED = 5;

    public static final int EVENT_ARTIFACT_ARCHIVED = 6;

    public static final int EVENT_ARTIFACT_DELETED = 7;


    private int type;


    public ArtifactEvent()
    {
    }

    public ArtifactEvent(int type)
    {
        this.type = type;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

}
