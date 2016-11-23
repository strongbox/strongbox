package org.carlspring.strongbox.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.carlspring.strongbox.controller.ArtifactController;
import org.carlspring.strongbox.controller.NugetPackageController;

/**
 * This filter used to map HTTP header values from one to another.<br>
 * Mapping example:<br>
 * &emsp;'user-agent = NuGet Command Line/2.8.60717.93 (Unix
 * 4.4.0.45)'->'NuGet/*'<br>
 * 
 * Such type of mapping is used in storage controllers to map requests according
 * 'user-agent' header type.
 * 
 * @see {@link ArtifactController} {@link NugetPackageController}
 * 
 * @author Sergey Bespalov
 *
 */
@WebFilter(filterName = "headerMappingFilter", urlPatterns = "/storages/*")
public class HeaderMappingFilter implements Filter
{

    private static final String USER_AGENT_UNKNOWN = "unknown";
    private static final String USER_AGENT_NUGET = "NuGet";
    private static final String USER_AGENT_MAVEN = "Maven";

    private Map<String, String> userAgentMap = new HashMap<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        String format = "%s/*";
        userAgentMap.put(USER_AGENT_NUGET, String.format(format, USER_AGENT_NUGET));
        userAgentMap.put(USER_AGENT_MAVEN, String.format(format, USER_AGENT_MAVEN));
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException
    {
        ServletRequest targetRequest = request instanceof HttpServletRequest
                ? new ServletRequestDecorator((HttpServletRequest) request)
                : request;
        chain.doFilter(targetRequest, response);
    }

    @Override
    public void destroy()
    {
    }

    private class ServletRequestDecorator extends HttpServletRequestWrapper
    {

        public ServletRequestDecorator(HttpServletRequest request)
        {
            super(request);
        }

        @Override
        public String getHeader(
                                String name)
        {
            String headerValue = super.getHeader(name);
            if (!name.equals("user-agent"))
            {
                return headerValue;
            }

            Optional<String> targetUserAgent = userAgentMap.keySet()
                    .stream()
                    .filter((k) -> {
                        return headerValue.toUpperCase()
                                .contains(k.toUpperCase());
                    })
                    .findFirst();

            return targetUserAgent.map((k) -> {
                return userAgentMap.get(k);
            }).orElse(String.format("%s/*", USER_AGENT_UNKNOWN));
        }

    }

}
