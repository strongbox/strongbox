package org.carlspring.repositoryunit.storage.repository;

/**
 * @author mtodorov
 */
public enum RepositoryPolicyEnum
{

    RELEASE("release"),

    SNAPSHOT("snapshot"),

    MIXED("mixed");

    private String policy;


    RepositoryPolicyEnum(String policy)
    {
        this.policy = policy;
    }

    public String getPolicy()
    {
        return policy;
    }

    public void setPolicy(String policy)
    {
        this.policy = policy;
    }

    @Override
    public String toString()
    {
        return policy;
    }

}
