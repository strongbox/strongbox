package org.carlspring.strongbox.storage.metadata.versions;

import java.nio.file.attribute.FileTime;

/**
 * @author Steve Todorov <s.todorov@itnews-bg.com>
 */
public class MetadataVersion implements Comparable<MetadataVersion>
{

    private String version;

    private FileTime createdDate;

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

    @Override
    public int compareTo(MetadataVersion v1)
    {
        int diff = v1.getVersion().compareTo(this.getVersion());

        if(diff < 1 && this.createdDate.toMillis() > v1.getCreatedDate().toMillis())
        {
            diff = 1;
        }

        return diff;
    }
}
