package org.carlspring.strongbox.nuget.file;

import java.io.Serializable;
import java.util.EnumSet;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(
        name = "frameworkAssembly",
        namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
)
@XmlAccessorType(XmlAccessType.NONE)
public class FrameworkAssembly
        implements Serializable
{

    @XmlAttribute(
            name = "assemblyName"
    )
    private String assemblyName;
    @XmlAttribute(
            name = "targetFramework"
    )
    @XmlJavaTypeAdapter(AssemblyTargetFrameworkAdapter.class)
    private EnumSet<Framework> targetFrameworks;

    public FrameworkAssembly()
    {
    }

    public String getAssemblyName()
    {
        return this.assemblyName;
    }

    public void setAssemblyName(String assemblyName)
    {
        this.assemblyName = assemblyName;
    }

    public EnumSet<Framework> getTargetFrameworks()
    {
        if (this.targetFrameworks == null)
        {
            this.targetFrameworks = EnumSet.allOf(Framework.class);
        }

        return this.targetFrameworks;
    }

    public void setTargetFrameworks(EnumSet<Framework> targetFrameworks)
    {
        this.targetFrameworks = targetFrameworks;
    }
}
