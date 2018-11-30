package org.carlspring.strongbox.authentication.external.ldap;

/**
 * @author Przemyslaw Fusik
 */
public class LdapRoleMapping
{

    private String ldapRole;

    private String strongboxRole;

    public LdapRoleMapping()
    {
        super();
    }

    public LdapRoleMapping(String ldapRole,
                           String strongboxRole)
    {
        super();
        this.ldapRole = ldapRole;
        this.strongboxRole = strongboxRole;
    }

    public String getLdapRole()
    {
        return ldapRole;
    }

    public void setLdapRole(final String ldapRole)
    {
        this.ldapRole = ldapRole;
    }

    public String getStrongboxRole()
    {
        return strongboxRole;
    }

    public void setStrongboxRole(final String strongboxRole)
    {
        this.strongboxRole = strongboxRole;
    }
}
