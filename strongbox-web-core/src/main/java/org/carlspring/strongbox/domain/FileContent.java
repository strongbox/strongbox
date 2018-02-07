package org.carlspring.strongbox.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.FileUtils;

public class FileContent 
{

    private String name;

    private String size;

    private String lastModified;
    
    @JsonCreator
    public FileContent( @JsonProperty("name") String name,
                        @JsonProperty("size") String size,
                        @JsonProperty("lastModified") String lastModified)
    {
        this.name = name;
        this.size = size;
        this.lastModified = lastModified;
    }
    
    public FileContent(Path p)
    {   
        if(p == null)
            return;
        
        Path name = p.getFileName();
        if(name == null)
            return;
        
        this.name = name.toString();
        this.size = FileUtils.byteCountToDisplaySize(p.toFile().length());
        this.lastModified = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss")
                                .format(new Date(p.toFile().lastModified()));
                
    }

    public String getName()
    {
        return name;
    }

    public String getSize()
    {
        return size;
    }

    public String getLastModified()
    {
        return lastModified;
    }
   
}

