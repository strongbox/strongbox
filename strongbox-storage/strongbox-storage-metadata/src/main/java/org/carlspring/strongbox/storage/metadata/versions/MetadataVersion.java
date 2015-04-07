package org.carlspring.strongbox.storage.metadata.versions;

import org.apache.maven.artifact.repository.metadata.SnapshotVersion;

import java.nio.file.attribute.FileTime;
import java.util.ArrayList;

/**
 * @author Steve Todorov <s.todorov@itnews-bg.com>
 */
public class MetadataVersion implements Comparable<MetadataVersion>
{

    private String version;

    private FileTime createdDate;

    private ArrayList<SnapshotVersion> snapshots = new ArrayList<SnapshotVersion>();

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public FileTime getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate(FileTime createdDate)
    {
        this.createdDate = createdDate;
    }

    public ArrayList<SnapshotVersion> getSnapshots()
    {
        return snapshots;
    }

    public void setSnapshots(ArrayList<SnapshotVersion> snapshots)
    {
        this.snapshots = snapshots;
    }

    @Override
    public int compareTo(MetadataVersion v1)
    {
        int diff = v1.getVersion().compareTo(this.getVersion());
        if (diff < 1 && this.createdDate.toMillis() > v1.getCreatedDate().toMillis())
        {
            diff = 1;
        }

        return diff;
    }
}
