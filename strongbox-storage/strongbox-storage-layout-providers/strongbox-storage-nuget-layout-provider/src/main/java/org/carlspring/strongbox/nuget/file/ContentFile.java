package org.carlspring.strongbox.nuget.file;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(
        name = "files",
        namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
)
@XmlAccessorType(XmlAccessType.NONE)
public class ContentFile
{

    @XmlAttribute(
            name = "include"
    )
    private String include;
    @XmlAttribute(
            name = "exclude"
    )
    private String exclude;
    @XmlAttribute(
            name = "buildAction"
    )
    private String buildAction;
    @XmlAttribute(
            name = "copyToOutput"
    )
    private Boolean copyToOutput;
    @XmlAttribute(
            name = "flatten"
    )
    private Boolean flatten;

    public ContentFile()
    {
    }

    protected String getInclude()
    {
        return this.include;
    }

    protected void setInclude(String include)
    {
        this.include = include;
    }

    protected String getBuildAction()
    {
        return this.buildAction;
    }

    protected void setBuildAction(String buildAction)
    {
        this.buildAction = buildAction;
    }

    protected Boolean getCopyToOutput()
    {
        return this.copyToOutput;
    }

    protected void setCopyToOutput(Boolean copyToOutput)
    {
        this.copyToOutput = copyToOutput;
    }

    protected String getExclude()
    {
        return this.exclude;
    }

    protected void setExclude(String exclude)
    {
        this.exclude = exclude;
    }

    protected Boolean getFlatten()
    {
        return this.flatten;
    }

    protected void setFlatten(Boolean flatten)
    {
        this.flatten = flatten;
    }
}
