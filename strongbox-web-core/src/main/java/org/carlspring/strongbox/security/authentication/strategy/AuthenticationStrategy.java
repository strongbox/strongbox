package org.carlspring.strongbox.security.authentication.strategy;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.AuthenticationConverter;

/**
 * @author Przemyslaw Fusik
 */
public interface AuthenticationStrategy extends AuthenticationConverter
{
    boolean supports(@Nonnull HttpServletRequest request);
}
