package org.carlspring.strongbox.controllers.configuration.security.ldap.support;

import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationForm;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationTestForm;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class SpringSecurityLdapConfigurationTester
        extends LdapAuthenticationProviderCreator
{

    public boolean test(final LdapConfigurationTestForm form)
    {
        LdapConfigurationForm configuration = form.getConfiguration();
        LdapAuthenticationProvider provider = createProvider(configuration);

        try
        {
            Authentication authentication = provider.authenticate(
                    new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword()));
            return authentication != null && authentication.isAuthenticated();
        }
        catch (Exception ex)
        {
            return false;
        }
    }
}
