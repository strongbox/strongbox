package org.carlspring.strongbox.storage.repository;

/**
 * @author mtodorov
 */
public enum RepositoryChecksumPolicyValidationTypeEnum
{

    STRICT("Strict"),

    LOG("Log"),

    WARN("Warn");

    private String policyType;


    RepositoryChecksumPolicyValidationTypeEnum(String policyType)
    {
        this.policyType = policyType;
    }

    public String getPolicyType()
    {
        return policyType;
    }

}
