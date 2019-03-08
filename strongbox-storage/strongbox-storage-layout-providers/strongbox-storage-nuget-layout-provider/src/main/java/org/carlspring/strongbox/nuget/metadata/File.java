package org.carlspring.strongbox.nuget.metadata;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.carlspring.strongbox.nuget.Nuspec;

@XmlRootElement(name = "file", namespace = Nuspec.NUSPEC_XML_NAMESPACE_2011)
@XmlAccessorType(XmlAccessType.NONE)
public class File implements Serializable
{

    @XmlAttribute(name = "src")
    private String src;
    @XmlAttribute(name = "target")
    private String target;
    @XmlAttribute(name = "exclude")
    private String exclude;

    protected String getSrc()
    {
        return src;
    }

    protected void setSrc(String src)
    {
        this.src = src;
    }

    protected String getTarget()
    {
        return target;
    }

    protected void setTarget(String target)
    {
        this.target = target;
    }

    protected String getExclude()
    {
        return exclude;
    }

    protected void setExclude(String exclude)
    {
        this.exclude = exclude;
    }

}
