package org.carlspring.strongbox.security.authorization;

/**
 * @author mtodorov
 */
public class AuthorizationGroup
{

    /**
     * The name of the group. This must match the name used in the respective provider.
     */
    private String name;

    /**
     * The path protected by this group.
     */
    private String basePath;

    private String description;


    public AuthorizationGroup()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getBasePath()
    {
        return basePath;
    }

    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

}
