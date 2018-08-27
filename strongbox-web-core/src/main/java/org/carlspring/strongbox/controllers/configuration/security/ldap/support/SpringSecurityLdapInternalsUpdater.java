package org.carlspring.strongbox.controllers.configuration.security.ldap.support;

import org.carlspring.strongbox.authentication.support.AuthoritiesExternalToInternalMapper;
import org.carlspring.strongbox.forms.configuration.security.ldap.LdapConfigurationForm;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import static org.carlspring.strongbox.controllers.configuration.security.ldap.support.SpringSecurityLdapInternalsUpdater.LdapConfigurationUpdateStage.GROUP_SEARCH_FILER;
import static org.carlspring.strongbox.controllers.configuration.security.ldap.support.SpringSecurityLdapInternalsUpdater.LdapConfigurationUpdateStage.ROLES_MAPPING;
import static org.carlspring.strongbox.controllers.configuration.security.ldap.support.SpringSecurityLdapInternalsUpdater.LdapConfigurationUpdateStage.USER_DN_PATTERNS;
import static org.carlspring.strongbox.controllers.configuration.security.ldap.support.SpringSecurityLdapInternalsUpdater.LdapConfigurationUpdateStage.USER_SEARCH_FILTER;

/**
 * Use this class if reflection on some other mechanisms is needed to affect LDAP configuration.
 *
 * @author Przemyslaw Fusik
 */
@Component
public class SpringSecurityLdapInternalsUpdater
{

    private static final Logger logger = LoggerFactory.getLogger(SpringSecurityLdapInternalsUpdater.class);

    @Inject
    private SpringSecurityLdapInternalsSupplier springSecurityLdapInternalsSupplier;

    public void updateGroupSearchFilter(DefaultLdapAuthoritiesPopulator ldapAuthoritiesPopulator,
                                        String searchBase,
                                        String searchFilter)
    {
        ldapAuthoritiesPopulator.setGroupSearchFilter(searchFilter);
        Field groupSearchBase = ReflectionUtils.findField(DefaultLdapAuthoritiesPopulator.class, "groupSearchBase");
        ReflectionUtils.makeAccessible(groupSearchBase);
        ReflectionUtils.setField(groupSearchBase, ldapAuthoritiesPopulator, searchBase);
    }

    public void updateUserDnPatterns(List<String> userDnPatterns)
    {
        final AbstractLdapAuthenticator authenticator = springSecurityLdapInternalsSupplier.getAuthenticator();
        authenticator.setUserDnPatterns(userDnPatterns.toArray(new String[0]));
    }

    public void updateUserSearchFilter(final AbstractLdapAuthenticator abstractLdapAuthenticator,
                                       final String searchBase,
                                       final String searchFilter)
    {
        abstractLdapAuthenticator.setUserSearch(new FilterBasedLdapUserSearch(searchBase, searchFilter,
                                                                              (BaseLdapPathContextSource) springSecurityLdapInternalsSupplier.getContextSource()));
    }

    public void updateLdapConfigurationSettings(LdapConfigurationForm form)
    {
        // backup configuration
        LdapAuthoritiesPopulator ldapAuthoritiesPopulator = springSecurityLdapInternalsSupplier.getAuthoritiesPopulator();
        LdapGroupSearchResponseEntityBody backupGroupSearchFilter =
                (ldapAuthoritiesPopulator instanceof DefaultLdapAuthoritiesPopulator) ?
                springSecurityLdapInternalsSupplier.ldapGroupSearchHolder(
                        (DefaultLdapAuthoritiesPopulator) ldapAuthoritiesPopulator) : null;

        AuthoritiesExternalToInternalMapper backupRolesMapping = springSecurityLdapInternalsSupplier.getAuthoritiesMapper();

        List<String> userDnPatternsContent = springSecurityLdapInternalsSupplier.getUserDnPatterns();
        LdapUserDnPatternsResponseEntityBody backupUserDnPatterns =
                userDnPatternsContent != null ? new LdapUserDnPatternsResponseEntityBody(userDnPatternsContent) : null;

        LdapUserSearch ldapUserSearchContent = springSecurityLdapInternalsSupplier.getUserSearch();
        LdapUserSearchResponseEntityBody backupUserSearchFilter =
                ldapUserSearchContent != null ? springSecurityLdapInternalsSupplier.getUserSearchXmlHolder(
                        (FilterBasedLdapUserSearch) ldapUserSearchContent) : null;

        LdapConfigurationUpdateStage updateStage = null;

        // replace configuration
        try
        {
            if (ldapAuthoritiesPopulator instanceof DefaultLdapAuthoritiesPopulator)
            {
                updateGroupSearchFilter((DefaultLdapAuthoritiesPopulator) ldapAuthoritiesPopulator,
                                        form.getGroupSearch().getSearchBase(),
                                        form.getGroupSearch().getSearchFilter());
                updateStage = GROUP_SEARCH_FILER;
            }

            springSecurityLdapInternalsSupplier.getAuthoritiesMapper()
                                               .setRolesMapping(form.getRolesMapping());
            updateStage = ROLES_MAPPING;

            updateUserDnPatterns(form.getUserDnPatterns());
            updateStage = USER_DN_PATTERNS;

            AbstractLdapAuthenticator abstractLdapAuthenticator = springSecurityLdapInternalsSupplier.getAuthenticator();
            if (abstractLdapAuthenticator != null)
            {
                updateUserSearchFilter(abstractLdapAuthenticator,
                                       form.getUserSearch().getSearchBase(),
                                       form.getUserSearch().getSearchFilter());
            }
            updateStage = USER_SEARCH_FILTER;
        }
        // rollback if needed
        catch (Exception ex)
        {
            logger.error("Unable to update Ldap Configuration", ex);

            rollbackUpdateLdapConfigurationSettings(updateStage,
                                                    backupGroupSearchFilter,
                                                    backupRolesMapping,
                                                    backupUserDnPatterns,
                                                    backupUserSearchFilter);
            throw ex;
        }
    }

    @SuppressFBWarnings(value = "ASF_SWITCH_FALLTHROUGH", justification = "intentional")
    private void rollbackUpdateLdapConfigurationSettings(LdapConfigurationUpdateStage updateStage,
                                                         LdapGroupSearchResponseEntityBody backupGroupSearchFilter,
                                                         AuthoritiesExternalToInternalMapper backupRolesMapping,
                                                         LdapUserDnPatternsResponseEntityBody backupUserDnPatterns,
                                                         LdapUserSearchResponseEntityBody backupUserSearchFilter)
    {
        if (updateStage == null)
        {
            return;
        }

        switch (updateStage)
        {
            case USER_SEARCH_FILTER:
                AbstractLdapAuthenticator ldapAuthenticator = springSecurityLdapInternalsSupplier.getAuthenticator();
                if (ldapAuthenticator != null)
                {
                    updateUserSearchFilter(ldapAuthenticator,
                                           backupUserSearchFilter.getSearchBase(),
                                           backupUserSearchFilter.getSearchFilter());
                }
                // fall through
            case USER_DN_PATTERNS:
                updateUserDnPatterns(backupUserDnPatterns.getUserDnPatterns());
                // fall through
            case ROLES_MAPPING:
                springSecurityLdapInternalsSupplier.getAuthoritiesMapper()
                                                   .setRolesMapping(backupRolesMapping.getRolesMapping());
                // fall through
            case GROUP_SEARCH_FILER:
                LdapAuthoritiesPopulator ldapAuthoritiesPopulator = springSecurityLdapInternalsSupplier.getAuthoritiesPopulator();
                if (ldapAuthoritiesPopulator instanceof DefaultLdapAuthoritiesPopulator)
                {
                    updateGroupSearchFilter((DefaultLdapAuthoritiesPopulator) ldapAuthoritiesPopulator,
                                            backupGroupSearchFilter.getSearchBase(),
                                            backupGroupSearchFilter.getSearchFilter());
                }
        }
    }

    enum LdapConfigurationUpdateStage
    {
        GROUP_SEARCH_FILER, ROLES_MAPPING, USER_DN_PATTERNS, USER_SEARCH_FILTER;
    }
}
