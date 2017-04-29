package org.carlspring.strongbox.security.vote;

import org.springframework.security.core.Authentication;

public interface AuthenticationProvider
{

    Authentication getAuthentication();
}
