package org.carlspring.strongbox.security.authentication;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class Http401AuthenticationEntryPoint
        implements AuthenticationEntryPoint
{

    private static final String IS_AJAX_REQUEST_HEADER_NAME = "X-Requested-With";

    private static final String IS_AJAX_REQUEST_HEADER_VALUE = "XMLHttpRequest";

    private final String headerValue;

    public Http401AuthenticationEntryPoint(String headerValue)
    {
        this.headerValue = headerValue;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException,
                   ServletException
    {
        if (!IS_AJAX_REQUEST_HEADER_VALUE.equals(request.getHeader(IS_AJAX_REQUEST_HEADER_NAME)))
        {
            response.setHeader("WWW-Authenticate", this.headerValue);
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }

}
