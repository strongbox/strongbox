package org.carlspring.strongbox.artifact.generation;

import java.util.ArrayList;

public class PackageJson
{
    private String name;
    private String version;
    private ArrayList<String> keywords;
    private ArrayList<License> licenses;
    private ArrayList<String> contributors;
    private ArrayList<String> maintainers;
    private ArrayList<String> files;
    private String man;
    private ArrayList<String> bundledDependencies;
    private String os;
    private String cpu;
    private String dist;
    
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getVersion()
    {
        return version;
    }
    public void setVersion(String version)
    {
        this.version = version;
    }
    public ArrayList<String> getKeywords()
    {
        return keywords;
    }
    public void setKeywords(ArrayList<String> keywords)
    {
        this.keywords = keywords;
    }
    public ArrayList<License> getLicenses()
    {
        return licenses;
    }
    public void setLicenses(ArrayList<License> licenses)
    {
        this.licenses = licenses;
    }
    public ArrayList<String> getContributors()
    {
        return contributors;
    }
    public void setContributors(ArrayList<String> contributors)
    {
        this.contributors = contributors;
    }
    public ArrayList<String> getMaintainers()
    {
        return maintainers;
    }
    public void setMaintainers(ArrayList<String> maintainers)
    {
        this.maintainers = maintainers;
    }
    public ArrayList<String> getFiles()
    {
        return files;
    }
    public void setFiles(ArrayList<String> files)
    {
        this.files = files;
    }
    public String getMan()
    {
        return man;
    }
    public void setMan(String man)
    {
        this.man = man;
    }
    public ArrayList<String> getBundledDependencies()
    {
        return bundledDependencies;
    }
    public void setBundledDependencies(ArrayList<String> bundledDependencies)
    {
        this.bundledDependencies = bundledDependencies;
    }
    public String getOs()
    {
        return os;
    }
    public void setOs(String os)
    {
        this.os = os;
    }
    public String getCpu()
    {
        return cpu;
    }
    public void setCpu(String cpu)
    {
        this.cpu = cpu;
    }
    public String getDist()
    {
        return dist;
    }
    public void setDist(String dist)
    {
        this.dist = dist;
    }
    
}
