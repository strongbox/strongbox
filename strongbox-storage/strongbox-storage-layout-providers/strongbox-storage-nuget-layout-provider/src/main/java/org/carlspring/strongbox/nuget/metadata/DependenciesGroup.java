package org.carlspring.strongbox.nuget.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.carlspring.strongbox.nuget.Nuspec;

@XmlAccessorType(XmlAccessType.NONE)
public class DependenciesGroup implements Serializable {

    /**
     * The framework for which the dependency group is intended
     */
    @XmlAttribute(name = "targetFramework")
    @XmlJavaTypeAdapter(TargetFrameworkAdapter.class)
    private Framework targetFramework;
    
    /**
     * Package dependencies
     */
    @XmlElement(name = "dependency", namespace = Nuspec.NUSPEC_XML_NAMESPACE_2011)
    private List<Dependency> dependencies;

    /**
     * @return package dependencies
     */
    public List<Dependency> getDependencies() {
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        return dependencies;
    }

    /**
     * @param dependencies package dependencies
     */
    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * @return the framework for which the dependency group is intended
     */
    public Framework getTargetFramework() {
        return targetFramework;
    }

    /**
     * @param targetFramework the framework for which the dependency group is intended
     */
    public void setTargetFramework(Framework targetFramework) {
        this.targetFramework = targetFramework;
    }

    /**
     * Convert list assembly to delimited string
     */
    public static class TargetFrameworkAdapter extends XmlAdapter<String, Framework> {

        @Override
        public String marshal(Framework framework) {
            if (framework == null) {
                return null;
            }
            return framework.name();
        }

        @Override
        public Framework unmarshal(String farmework) {
            if (farmework == null) {
                return null;
            }
            Framework result = Framework.getByShortName(farmework);
            if (result == null) {
                return Framework.getByFullName(farmework);
            } else {
                return result;
            }
        }
    }
}
