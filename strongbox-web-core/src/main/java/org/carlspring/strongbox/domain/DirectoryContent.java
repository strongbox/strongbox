package org.carlspring.strongbox.domain;

import java.util.List;

public class DirectoryContent
{    
    private List<FileContent> directories;
    
    private List<FileContent> files;
    
    public List<FileContent> getDirectories()
    {
        return directories;
    }
    
    public List<FileContent> getFiles()
    {
        return files;
    }

    public void setDirectories(List<FileContent> directories)
    {
        this.directories = directories;        
    }

    public void setFiles(List<FileContent> files)
    {
        this.files = files;        
    }
}
