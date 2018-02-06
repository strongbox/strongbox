package org.carlspring.strongbox.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryContent
{   
    Logger logger = LoggerFactory.getLogger(DirectoryContent.class);
    
    private List<FileContent> directories;
    
    private List<FileContent> files;
    
    @JsonCreator
    public DirectoryContent( @JsonProperty("directories")  List<FileContent> directories,
                             @JsonProperty("files") List<FileContent> files) 
    {
        this.directories = directories;
        this.files = files;
    }
    
    public DirectoryContent(Path dirPath) throws IOException
    {
        this.directories = new ArrayList<FileContent>();
        this.files = new ArrayList<FileContent>();
               
        setDirectories(dirPath);
        setFiles(dirPath);
    }
       
    public void setDirectories(Path dirPath) throws IOException
    {   
        Files.list(dirPath)
             .filter(p -> !p.getFileName().toString().startsWith("."))
             .filter(p -> {
                try
                {
                    return !Files.isHidden(p);
                }
                catch (IOException e)
                {   
                    logger.debug("Error accessing file");
                    return false;
                }
            })
             .filter(p -> Files.isDirectory(p))
             .sorted()
             .forEach(p -> directories.add(new FileContent(p)));        
    }
    
  
       
    public void setFiles(Path dirPath) throws IOException
    {
        Files.list(dirPath)
             .filter(p -> !p.getFileName().toString().startsWith("."))
             .filter(p -> {
                try
                {
                    return !Files.isHidden(p);
                }
                catch (IOException e)
                {
                    logger.debug("Error accessing file");
                    return false;
                }
            })
             .filter(p -> !Files.isDirectory(p))
             .sorted()
             .forEach(p -> files.add(new FileContent(p)));
    }
    
    public List<FileContent> getDirectories()
    {
        return directories;
    }
    
    public List<FileContent> getFiles()
    {
        return files;
    }
}
