package org.carlspring.strongbox.domain;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryContent 

{   
    private Path dirPath;
       
    private List<File> subDirectories, files;
    
    private static Logger logger = LoggerFactory.getLogger(DirectoryContent.class);
    
    public DirectoryContent(Path dirPath) throws IOException
    {
        this.dirPath = dirPath;
        this.subDirectories = new ArrayList<File>();
        this.files = new ArrayList<File>();
               
        setSubDirectories();
        setFiles();
    }

    public List<File> getSubDirectories()
    {
        return subDirectories;
    }
      
    public void setSubDirectories() throws IOException
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
             .forEach(p -> subDirectories.add(p.toFile()));        
    }
    
    public List<File> getFiles()
    {
        return files;
    }
       
    public void setFiles() throws IOException
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
             .forEach(p -> files.add(p.toFile()));
    }
       
    public  Map<String, List<File>> getContents()
    {
        Map<String, List<File>> contents = new HashMap<>();
        
        contents.put("directories", subDirectories);
        contents.put("files", files);
        
        return contents;
    }
}
