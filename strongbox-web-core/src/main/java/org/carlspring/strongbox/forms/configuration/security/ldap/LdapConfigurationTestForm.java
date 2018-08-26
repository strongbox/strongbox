package org.carlspring.strongbox.forms.configuration.security.ldap;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author Przemyslaw Fusik
 */
public class LdapConfigurationTestForm
{

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    @NotNull
    @Valid
    private LdapConfigurationForm configuration;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(final String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(final String password)
    {
        this.password = password;
    }

    public LdapConfigurationForm getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(final LdapConfigurationForm configuration)
    {
        this.configuration = configuration;
    }
}
