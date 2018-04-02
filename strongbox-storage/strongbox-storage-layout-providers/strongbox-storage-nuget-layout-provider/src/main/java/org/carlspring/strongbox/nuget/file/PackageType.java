package org.carlspring.strongbox.nuget.file;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(
        name = "packageType",
        namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
)
@XmlAccessorType(XmlAccessType.NONE)
public class PackageType
        implements Serializable
{

    @XmlAttribute(
            name = "packageType"
    )
    private String packageType;

    public PackageType()
    {
    }

    protected String getPackageType()
    {
        return this.packageType;
    }

    protected void setPackageType(String packageType)
    {
        this.packageType = packageType;
    }
}
