package org.carlspring.strongbox.security.authentication;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.carlspring.strongbox.controllers.support.ErrorResponseEntityBody;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Http401AuthenticationEntryPoint implements AuthenticationEntryPoint
{

    private static final String IS_AJAX_REQUEST_HEADER_NAME = "X-Requested-With";

    private static final String IS_AJAX_REQUEST_HEADER_VALUE = "XMLHttpRequest";

    private static final String STRONGBOX_REALM = "Strongbox Repository Manager";

    @Inject
    private ObjectMapper objectMapper;
    
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException,
                   ServletException
    {        
        if (!IS_AJAX_REQUEST_HEADER_VALUE.equals(request.getHeader(IS_AJAX_REQUEST_HEADER_NAME)))
        {
            response.setHeader("WWW-Authenticate", "Basic realm=\"" + STRONGBOX_REALM + "\"");
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().println(objectMapper.writeValueAsString(new ErrorResponseEntityBody("unauthorized")));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);      

        response.flushBuffer();
    }
    
}
