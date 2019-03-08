package org.carlspring.strongbox.nuget.metadata;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.carlspring.strongbox.nuget.Nuspec;

@XmlRootElement(name = "packageType", namespace = Nuspec.NUSPEC_XML_NAMESPACE_2011)
@XmlAccessorType(XmlAccessType.NONE)
public class PackageType implements Serializable
{

    @XmlAttribute(name = "packageType")
    private String packageType;

    protected String getPackageType()
    {
        return packageType;
    }

    protected void setPackageType(String packageType)
    {
        this.packageType = packageType;
    }
    
}
