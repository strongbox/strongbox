package org.carlspring.strongbox.nuget.file;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(
        name = "file",
        namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
)
@XmlAccessorType(XmlAccessType.NONE)
public class File
{

    @XmlAttribute(
            name = "src"
    )
    private String src;
    @XmlAttribute(
            name = "target"
    )
    private String target;
    @XmlAttribute(
            name = "exclude"
    )
    private String exclude;

    public File()
    {
    }

    protected String getSrc()
    {
        return this.src;
    }

    protected void setSrc(String src)
    {
        this.src = src;
    }

    protected String getTarget()
    {
        return this.target;
    }

    protected void setTarget(String target)
    {
        this.target = target;
    }

    protected String getExclude()
    {
        return this.exclude;
    }

    protected void setExclude(String exclude)
    {
        this.exclude = exclude;
    }
}
