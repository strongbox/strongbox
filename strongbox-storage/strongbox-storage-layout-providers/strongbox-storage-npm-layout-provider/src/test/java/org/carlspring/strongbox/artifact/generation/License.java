package org.carlspring.strongbox.artifact.generation;

public class License
{
    private String type;
    private String url;
    
    public License()
    {

    }
    
    public License(String type, String url)
    {
        this.setType(type);
        this.setUrl(url);
    }

    public String getType() 
    {
        return type;
    }

    public void setType(String type) 
    {
        this.type = type;
    }

    public String getUrl() 
    {
        return url;
    }

    public void setUrl(String url) 
    {
        this.url = url;
    }
    
}