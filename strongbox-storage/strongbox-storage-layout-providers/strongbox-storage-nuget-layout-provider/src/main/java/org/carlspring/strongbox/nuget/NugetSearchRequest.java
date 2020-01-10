package org.carlspring.strongbox.nuget;

public class NugetSearchRequest
{

    private String filter;

    private String searchTerm;

    private String targetFramework;

    private boolean includePreRelease;

    public String getFilter()
    {
        return filter;
    }

    public void setFilter(String filter)
    {
        this.filter = filter;
    }

    public String getSearchTerm()
    {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm)
    {
        this.searchTerm = searchTerm;
    }

    public String getTargetFramework()
    {
        return targetFramework;
    }

    public void setTargetFramework(String targetFramework)
    {
        this.targetFramework = targetFramework;
    }

    public boolean isIncludePreRelease()
    {
        return includePreRelease;
    }

    public void setIncludePreRelease(boolean includePreRelease)
    {
        this.includePreRelease = includePreRelease;
    }

}
