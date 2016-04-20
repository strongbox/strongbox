package org.carlspring.strongbox.rest.app.spring.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * An entry point used when an unauthorized access is attempted
 */
public class UnauthorizedEntryPoint implements AuthenticationEntryPoint
{

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException
    {

        // Use the type of authException to decide what to present to the user.
        // This simple impl just redirects to /login but it could be that the
        // user is already logged in and (s)he has not enough rights
        response.sendRedirect("/login");
    }

}
