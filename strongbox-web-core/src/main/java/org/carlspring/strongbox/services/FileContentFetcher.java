package org.carlspring.strongbox.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
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
        if (path == null)
        {
            return null;
        }
        
        BasicFileAttributes attributes;
        try
        {
            attributes = Files.readAttributes(path, BasicFileAttributes.class);
        }
        catch (IOException e)
        {
            return null;
        }
        
        FileContent fileContent = new FileContent();
        fileContent.setName(path.getFileName().toString());
        
        if (attributes.isDirectory()) {
            return fileContent;
        }
        
        fileContent.setLastModified(new Date(attributes.lastModifiedTime().toMillis()));
        fileContent.setSize(attributes.size());
        
        return fileContent;
    }
}
