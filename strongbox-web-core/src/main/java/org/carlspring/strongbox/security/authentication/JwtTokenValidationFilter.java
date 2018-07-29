package org.carlspring.strongbox.security.authentication;

import org.carlspring.strongbox.controllers.support.ErrorResponseEntityBody;
import org.carlspring.strongbox.security.exceptions.SecurityTokenException;
import org.carlspring.strongbox.security.exceptions.SecurityTokenExpiredException;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.carlspring.strongbox.web.ResponseAlreadyCommittedException;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.filter.GenericFilterBean;

/**
 * @author Przemyslaw Fusik
 */
public class JwtTokenValidationFilter
        extends GenericFilterBean
        implements JwtTokenFetcher
{

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenValidationFilter.class);

    @Inject
    private SecurityTokenProvider securityTokenProvider;

    @Inject
    private ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest req,
                         ServletResponse res,
                         FilterChain chain)
            throws IOException, ServletException
    {

        try
        {
            validateTokenIfExists((HttpServletRequest) req, (HttpServletResponse) res);
        }
        catch (ResponseAlreadyCommittedException ex)
        {
            return;
        }

        chain.doFilter(req, res);
    }


    private void validateTokenIfExists(HttpServletRequest request,
                                       HttpServletResponse response)
            throws IOException
    {
        Optional<String> optToken = getToken(request);
        if (!optToken.isPresent())
        {
            return;
        }

        String token = optToken.get();
        String username = null;

        try
        {
            username = securityTokenProvider.getSubject(token);
            securityTokenProvider.verifyToken(token, username, Collections.emptyMap());
        }
        catch (SecurityTokenExpiredException ex)
        {
            logger.debug("Token {} of user {} expired.", token, username);
            sendJsonErrorToResponse(response, "expired");
        }
        catch (SecurityTokenException ex)
        {
            sendJsonErrorToResponse(response, "invalid.token");
        }

        logger.debug("Token {} of user {} verified.", token, username);
    }

    private void sendJsonErrorToResponse(HttpServletResponse response,
                                         String body)
            throws IOException
    {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().println(objectMapper.writeValueAsString(new ErrorResponseEntityBody(body)));
        response.flushBuffer();
        throw new ResponseAlreadyCommittedException();
    }
}
