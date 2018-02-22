package org.carlspring.strongbox.storage.repository;

/**
 * @author mtodorov
 */
public enum RepositoryLayoutEnum
{

    NUGET("NuGet");

    private String layout;


    RepositoryLayoutEnum(String layout)
    {
        this.layout = layout;
    }

    public String getLayout()
    {
        return layout;
    }

    public void setLayout(String layout)
    {
        this.layout = layout;
    }

}
