package org.carlspring.strongbox.nuget.file;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;

public class Dependencies
        implements Serializable
{

    @XmlElement(
            name = "dependency",
            namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
    )
    public List<Dependency> dependencies;
    @XmlElement(
            name = "group",
            namespace = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd"
    )
    private List<DependenciesGroup> groups;

    public Dependencies()
    {
    }

    public Dependencies(List<Dependency> dependencies,
                        List<DependenciesGroup> groups)
    {
        this.dependencies = dependencies;
        this.groups = groups;
    }

    public List<Dependency> getDependencies()
    {
        if (this.dependencies == null)
        {
            this.dependencies = new ArrayList();
        }

        List<Dependency> result = new ArrayList();
        result.addAll(this.dependencies);
        if (this.groups != null)
        {
            Iterator groupIterator = this.groups.iterator();

            while (groupIterator.hasNext())
            {
                DependenciesGroup group = (DependenciesGroup) groupIterator.next();
                result.addAll(group.getDependencys());
            }
        }

        return result;
    }

    public List<DependenciesGroup> getGroups()
    {
        if (this.groups == null)
        {
            this.groups = new ArrayList();
        }

        if (this.dependencies != null && !this.dependencies.isEmpty())
        {
            DependenciesGroup rootGroup = new DependenciesGroup();
            rootGroup.setDependencys(this.dependencies);
            ArrayList<DependenciesGroup> result = new ArrayList(this.groups.size() + 1);
            result.addAll(this.groups);
            result.add(rootGroup);
            return result;
        }
        else
        {
            return this.groups;
        }
    }
}
