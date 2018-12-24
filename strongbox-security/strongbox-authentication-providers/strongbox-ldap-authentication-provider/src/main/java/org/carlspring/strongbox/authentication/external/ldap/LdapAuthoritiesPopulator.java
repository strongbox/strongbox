package org.carlspring.strongbox.authentication.external.ldap;

import java.util.Set;

import javax.inject.Inject;

import org.springframework.ldap.core.ContextSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

public class LdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator
{

    @Inject
    private GrantedAuthoritiesMapper authoritiesMapper;

    public LdapAuthoritiesPopulator(ContextSource contextSource,
                                    String groupSearchBase)
    {
        super(contextSource, groupSearchBase);
    }

    @Override
    public Set<GrantedAuthority> getGroupMembershipRoles(String userDn,
                                                         String username)
    {
        return (Set<GrantedAuthority>) authoritiesMapper.mapAuthorities(super.getGroupMembershipRoles(userDn, username));
    }

}
