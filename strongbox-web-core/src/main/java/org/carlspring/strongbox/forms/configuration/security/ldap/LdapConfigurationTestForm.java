package org.carlspring.strongbox.forms.configuration.security.ldap;

/**
 * @author Przemyslaw Fusik
 */
public class LdapConfigurationTestForm
{

    private String username;

    private String password;

    private LdapConfigurationForm form;

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

    public LdapConfigurationForm getForm()
    {
        return form;
    }

    public void setForm(final LdapConfigurationForm form)
    {
        this.form = form;
    }
}
