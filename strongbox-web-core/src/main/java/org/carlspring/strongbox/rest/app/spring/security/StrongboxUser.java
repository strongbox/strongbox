package org.carlspring.strongbox.rest.app.spring.security;

import java.util.ArrayList;
import java.util.List;

/**
 * An application user
 */
public class StrongboxUser {
	private String userName;
	private String password;
	private boolean enabled;
	private String salt;
	private List<String> roles = new ArrayList<>();

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(final String salt) {
		this.salt = salt;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(final List<String> roles) {
		this.roles = roles;
	}
}
