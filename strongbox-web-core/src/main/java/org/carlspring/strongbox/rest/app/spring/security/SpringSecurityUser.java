package org.carlspring.strongbox.rest.app.spring.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A wrapper of {@link StrongboxUser} that is used by Spring Security
 */
class SpringSecurityUser implements UserDetails {
	private final StrongboxUser user;

	SpringSecurityUser(final StrongboxUser user) {
		this.user = user;
	}

	public void setSalt(final String salt) {
		user.setSalt(salt);
	}

	public String getSalt() {
		return user.getSalt();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return isEnabled();
	}

	@Override
	public boolean isAccountNonLocked() {
		return isEnabled();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return isEnabled();
	}

	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}
}
