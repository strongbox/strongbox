package org.carlspring.strongbox.domain;

import java.util.List;
import javax.inject.Inject;
import org.carlspring.strongbox.services.FileContentFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryContent
{       
    @Inject
    FileContentFetcher fileContentFetcher;
    
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
