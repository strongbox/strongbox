package org.carlspring.strongbox.io.filters;

import org.carlspring.commons.io.filters.DirectoryFilter;
import org.carlspring.maven.commons.io.filters.PomFilenameFilter;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author mtodorov
 */
public class ArtifactVersionDirectoryFilter
        extends DirectoryFilter
{

    private boolean excludeHiddenDirectories = true;

    private FilenameFilter filter;


    public ArtifactVersionDirectoryFilter()
    {
    }

    public ArtifactVersionDirectoryFilter(FilenameFilter filter)
    {
        this.filter = filter;
    }

    public ArtifactVersionDirectoryFilter(boolean excludeHiddenDirectories)
    {
        this.excludeHiddenDirectories = excludeHiddenDirectories;
    }

    public ArtifactVersionDirectoryFilter(FilenameFilter filter,
                                          boolean excludeHiddenDirectories)
    {
        this.filter = filter;
        this.excludeHiddenDirectories = excludeHiddenDirectories;
    }

    @Override
    public boolean accept(File file)
    {
        return super.accept(file) && containsPomFiles(file);
    }

    private boolean containsPomFiles(File file)
    {
        if (file.isDirectory())
        {
            filter = filter != null ? filter : new PomFilenameFilter();
            File[] directories = file.listFiles(filter);

            return directories != null && directories.length > 0;
        }
        else
        {
            return false;
        }
    }

    public boolean excludeHiddenDirectories()
    {
        return excludeHiddenDirectories;
    }

    public void setExcludeHiddenDirectories(boolean excludeHiddenDirectories)
    {
        this.excludeHiddenDirectories = excludeHiddenDirectories;
    }

    public FilenameFilter getFilenameFilter()
    {
        return filter;
    }

    public void setFilenameFilter(FilenameFilter filter)
    {
        this.filter = filter;
    }

}
