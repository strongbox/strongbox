package org.carlspring.strongbox.nuget.file;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;


@XmlAccessorType(XmlAccessType.NONE)
public class DependenciesGroup
        implements Serializable
{

    @XmlAttribute(
            name = "targetFramework"
    )
    @XmlJavaTypeAdapter(DependenciesGroup.TargetFrameworkAdapter.class)
    private Framework targetFramework;
    @XmlElement(
            name = "dependency",
            namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
    )
    private List<Dependency> dependencys;

    public DependenciesGroup()
    {
    }

    public List<Dependency> getDependencys()
    {
        if (this.dependencys == null)
        {
            this.dependencys = new ArrayList();
        }

        return this.dependencys;
    }

    public void setDependencys(List<Dependency> dependencys)
    {
        this.dependencys = dependencys;
    }

    public Framework getTargetFramework()
    {
        return this.targetFramework;
    }

    public void setTargetFramework(Framework targetFramework)
    {
        this.targetFramework = targetFramework;
    }

    public static class TargetFrameworkAdapter
            extends XmlAdapter<String, Framework>
    {

        public TargetFrameworkAdapter()
        {
        }

        public String marshal(Framework framework)
        {
            return framework == null ? null : framework.name();
        }

        public Framework unmarshal(String farmework)
        {
            if (farmework == null)
            {
                return null;
            }
            else
            {
                Framework result = Framework.getByShortName(farmework);
                return result == null ? Framework.getByFullName(farmework) : result;
            }
        }
    }
}