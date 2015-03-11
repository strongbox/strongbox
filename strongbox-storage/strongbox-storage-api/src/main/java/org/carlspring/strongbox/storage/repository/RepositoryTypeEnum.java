package org.carlspring.strongbox.storage.repository;

/**
 * @author mtodorov
 */
public enum RepositoryTypeEnum
{

    HOSTED("hosted"),

    PROXY("proxy"),     // Unsupported

    GROUP("group"),

    VIRTUAL("virtual"); // Unsupported


    private String type;


    RepositoryTypeEnum(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

}
