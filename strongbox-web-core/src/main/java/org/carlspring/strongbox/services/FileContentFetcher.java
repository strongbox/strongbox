package org.carlspring.strongbox.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.carlspring.strongbox.domain.FileContent;
import org.springframework.stereotype.Component;

@Component
public class FileContentFetcher
{
    public FileContent fetchFileContent(Path path)
    {
        if(path == null)
            return null;
        
        Path fileName = path.getFileName();
        if(fileName == null)
        {
            return null;
        }
        
        FileContent fileContent = new FileContent();
        
        String name = fileName.toString();
        String size = FileUtils.byteCountToDisplaySize(path.toFile().length());
        String lastModified = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss")
                                .format(new Date(path.toFile().lastModified()));
        
        fileContent.setName(name);
        if(Files.isDirectory(path))
        {
            fileContent.setSize("-");
        }
        else
        {
            fileContent.setSize(size);
        }
        fileContent.setLastModified(lastModified);
        
        return fileContent;
    }
}
