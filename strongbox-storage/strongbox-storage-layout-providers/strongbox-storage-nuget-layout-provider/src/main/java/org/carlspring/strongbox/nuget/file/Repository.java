package org.carlspring.strongbox.nuget.file;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(
        name = "repository",
        namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
)
@XmlAccessorType(XmlAccessType.NONE)
public class Repository
{

    @XmlAttribute(
            name = "type"
    )
    private String type;
    @XmlAttribute(
            name = "url"
    )
    private String url;

    public Repository()
    {
    }

    protected String getType()
    {
        return this.type;
    }

    protected void setType(String type)
    {
        this.type = type;
    }

    protected String getUrl()
    {
        return this.url;
    }

    protected void setUrl(String url)
    {
        this.url = url;
    }
}
