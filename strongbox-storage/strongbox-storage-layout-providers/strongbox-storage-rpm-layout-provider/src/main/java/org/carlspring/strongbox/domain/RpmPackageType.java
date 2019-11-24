package org.carlspring.strongbox.domain;

import static org.springframework.util.StringUtils.isEmpty;

public enum RpmPackageType
{
    SOURCE("src"),
    BINARY("");

    private String postfix;

    public String getPostfix()
    {
        return postfix;
    }

    RpmPackageType(String postfix)
    {
        this.postfix = postfix;
    }

    public static RpmPackageType fromValue(String name)
    {
        return isEmpty(name) ? BINARY : valueOf(name.toUpperCase());
    }

}
