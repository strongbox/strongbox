package org.carlspring.strongbox.security.authentication;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Przemyslaw Fusik
 */
public interface JwtTokenFetcher
{

    Logger logger = LoggerFactory.getLogger(JwtTokenFetcher.class);

    String AUTHORIZATION_HEADER = "Authorization";
    
    String BEARER_AUTHORIZATION_PREFIX = "Bearer";

    Pattern BEARER_PATTERN = Pattern.compile("Bearer (.*)");

    default Optional<String> getToken(HttpServletRequest request)
    {
        String tokenHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (tokenHeader == null)
        {
            return Optional.empty();
        }

        Matcher matcher = BEARER_PATTERN.matcher(tokenHeader);
        if (!matcher.matches())
        {
            return Optional.empty();
        }

        String token = matcher.group(1);
        logger.debug("Bearer Authorization header found with token {}", token);
        return Optional.of(token);
    }

}
