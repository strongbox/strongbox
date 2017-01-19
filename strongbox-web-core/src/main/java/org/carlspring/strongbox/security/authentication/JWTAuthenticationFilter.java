package org.carlspring.strongbox.security.authentication;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JWTAuthenticationFilter extends OncePerRequestFilter
{

    private AuthenticationManager authenticationManager;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager)
    {
        super();
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException,
        IOException
    {
        String tokenHeader = request.getHeader("Authorization");

        Pattern pattern = Pattern.compile("Bearer (.*)");
        Matcher matcher;
        if (tokenHeader == null || !(matcher = pattern.matcher(tokenHeader)).matches())
        {
            filterChain.doFilter(request, response);
            return;
        }

        String token = matcher.group(1);

        try
        {
            JWTAuthentication tokenAuthentication = new JWTAuthentication(token);
            Authentication authentication = authenticationManager.authenticate(tokenAuthentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        catch (AuthenticationException e)
        {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

}
