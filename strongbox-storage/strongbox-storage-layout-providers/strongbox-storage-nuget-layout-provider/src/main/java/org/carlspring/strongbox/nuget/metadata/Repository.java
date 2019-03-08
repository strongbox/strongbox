package org.carlspring.strongbox.nuget.metadata;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.carlspring.strongbox.nuget.Nuspec;

@XmlRootElement(name = "repository", namespace = Nuspec.NUSPEC_XML_NAMESPACE_2011)
@XmlAccessorType(XmlAccessType.NONE)
public class Repository implements Serializable
{

    @XmlAttribute(name = "type")
    private String type;
    @XmlAttribute(name = "url")
    private String url;

    protected String getType()
    {
        return type;
    }

    protected void setType(String type)
    {
        this.type = type;
    }

    protected String getUrl()
    {
        return url;
    }

    protected void setUrl(String url)
    {
        this.url = url;
    }

}
