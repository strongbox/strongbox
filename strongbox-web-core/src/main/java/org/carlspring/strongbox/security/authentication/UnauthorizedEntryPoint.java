package org.carlspring.strongbox.security.authentication;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

/**
 * An entry point used when an unauthorized access is attempted
 */
public class UnauthorizedEntryPoint extends BasicAuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException
    {

        // Use the type of authException to decide what to present to the user.
        // This simple impl just redirects to /login but it could be that the
        // user is already logged in and (s)he has not enough rights
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // response.sendRedirect("/login");
    }

}
