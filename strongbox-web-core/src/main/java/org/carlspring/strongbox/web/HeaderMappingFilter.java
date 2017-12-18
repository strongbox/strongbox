package org.carlspring.strongbox.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.controllers.maven.MavenArtifactController;
import org.carlspring.strongbox.controllers.nuget.NugetPackageController;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * This filter used to map HTTP header values from one to another.<br>
 * Mapping example:<br>
 * &emsp;'user-agent = NuGet Command Line/2.8.60717.93 (Unix 4.4.0.45)'->'NuGet/*'<br>
 * <p>
 * Such type of mapping is used in storage controllers to map requests according 'user-agent' header type.
 *
 * @author Sergey Bespalov
 * @see {@link MavenArtifactController} {@link NugetPackageController}
 */
public class HeaderMappingFilter
        implements Filter
{

    private static final String USER_AGENT_UNKNOWN = "unknown";
    private static final String USER_AGENT_NUGET = "NuGet";
    private static final String USER_AGENT_MAVEN = "Maven";
    private static final String USER_AGENT_NPM= "npm";

    private Map<String, String> userAgentMap = new HashMap<>();
    private Map<String, String> layoutMap = new HashMap<>();

    @Inject
    private ConfigurationManager configurationManager;

    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException
    {
        // Do nothing
    }

    @PostConstruct
    public void postConstruct()
    {
        // TODO: we need auto configuration for this, maybe thru `LayoutProviderRegistry`

        String format = "%s/*";
        userAgentMap.put(USER_AGENT_NUGET, String.format(format, USER_AGENT_NUGET));
        userAgentMap.put(USER_AGENT_MAVEN, String.format(format, USER_AGENT_MAVEN));

        layoutMap.put(NugetLayoutProvider.ALIAS, String.format(format, USER_AGENT_NUGET));
        layoutMap.put(Maven2LayoutProvider.ALIAS, String.format(format, USER_AGENT_MAVEN));
        layoutMap.put(NpmLayoutProvider.ALIAS, String.format(format, USER_AGENT_NPM));
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException,
                   ServletException
    {
        String layout;
        try
        {
            layout = getRequestedLayout(((HttpServletRequest) request).getPathInfo());
        }
        catch (IllegalArgumentException e)
        {
            ((HttpServletResponse)response).setStatus(404);
            ((HttpServletResponse)response).getWriter().append(e.getMessage());
            return;
        }

        ServletRequest targetRequest = request instanceof HttpServletRequest
                                       ? new ServletRequestDecorator((HttpServletRequest) request, layout)
                                       : request;
        chain.doFilter(targetRequest, response);
    }

    private String getRequestedLayout(String pathInfo)
    {
        if (!pathInfo.startsWith("/storages"))
        {
            return null;
        }
        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 4)
        {
            throw new IllegalArgumentException(String.format("Illegal format of `storages` request [%s].%nRequest path should be in the form of:%n%n\t'storages/{storageId}/{repositoryId}/...'", pathInfo));
        }
        String storageId = pathParts[2];
        String repositoryId = pathParts[3];

        Storage storage = configurationManager.getConfiguration()
                                              .getStorage(storageId);
        if (storage == null)
        {
            throw new IllegalArgumentException(String.format("Storage not found [%s]", storageId));
        }
        
        Repository repository = storage.getRepository(repositoryId);
        if (repository == null)
        {
            throw new IllegalArgumentException(String.format("Repository not found [%s]", repositoryId));
        }

        return repository.getLayout();
    }

    @Override
    public void destroy()
    {
    }

    private class ServletRequestDecorator
            implements HttpServletRequest
    {

        private static final String HEADER_NAME_USER_AGENT = "user-agent";
        private HttpServletRequest target;
        private String layout;

        public ServletRequestDecorator(HttpServletRequest target,
                                       String layout)
        {
            super();
            this.target = target;
            this.layout = layout;
        }

        @Override
        public String getHeader(String name)
        {
            String headerValue = target.getHeader(name);
            if (!HEADER_NAME_USER_AGENT.equals(name))
            {
                return headerValue;
            }

            Optional<String> targetUserAgent = userAgentMap.keySet()
                                                           .stream()
                                                           .filter((k) ->
                                                                   {
                                                                       return headerValue.toUpperCase()
                                                                                         .contains(k.toUpperCase());
                                                                   })
                                                           .findFirst();

            String result = targetUserAgent.map((k) ->
                                                {
                                                    return userAgentMap.get(k);
                                                })
                                           .orElseGet(() -> layoutMap.get(layout));

            return Optional.ofNullable(result)
                           .orElse(String.format("%s/*", USER_AGENT_UNKNOWN));
        }

        public Object getAttribute(String name)
        {
            return target.getAttribute(name);
        }

        public String getAuthType()
        {
            return target.getAuthType();
        }

        public Cookie[] getCookies()
        {
            return target.getCookies();
        }

        public Enumeration<String> getAttributeNames()
        {
            return target.getAttributeNames();
        }

        public long getDateHeader(String name)
        {
            return target.getDateHeader(name);
        }

        public String getCharacterEncoding()
        {
            return target.getCharacterEncoding();
        }

        public void setCharacterEncoding(String env)
                throws UnsupportedEncodingException
        {
            target.setCharacterEncoding(env);
        }

        public int getContentLength()
        {
            return target.getContentLength();
        }

        public long getContentLengthLong()
        {
            return target.getContentLengthLong();
        }

        public Enumeration<String> getHeaders(String name)
        {
            if (!HEADER_NAME_USER_AGENT.equals(name))
            {
                return target.getHeaders(name);
            }
            Enumeration<String> result = Collections.enumeration(Arrays.asList(new String[]{ getHeader(name) }));
            return result;
        }

        public String getContentType()
        {
            return target.getContentType();
        }

        public ServletInputStream getInputStream()
                throws IOException
        {
            return target.getInputStream();
        }

        public String getParameter(String name)
        {
            return target.getParameter(name);
        }

        public Enumeration<String> getHeaderNames()
        {
            return target.getHeaderNames();
        }

        public int getIntHeader(String name)
        {
            return target.getIntHeader(name);
        }

        public Enumeration<String> getParameterNames()
        {
            return target.getParameterNames();
        }

        public String getMethod()
        {
            return target.getMethod();
        }

        public String[] getParameterValues(String name)
        {
            return target.getParameterValues(name);
        }

        public String getPathInfo()
        {
            return target.getPathInfo();
        }

        public Map<String, String[]> getParameterMap()
        {
            return target.getParameterMap();
        }

        public String getPathTranslated()
        {
            return target.getPathTranslated();
        }

        public String getProtocol()
        {
            return target.getProtocol();
        }

        public String getScheme()
        {
            return target.getScheme();
        }

        public String getContextPath()
        {
            return target.getContextPath();
        }

        public String getServerName()
        {
            return target.getServerName();
        }

        public int getServerPort()
        {
            return target.getServerPort();
        }

        public BufferedReader getReader()
                throws IOException
        {
            return target.getReader();
        }

        public String getQueryString()
        {
            return target.getQueryString();
        }

        public String getRemoteUser()
        {
            return target.getRemoteUser();
        }

        public String getRemoteAddr()
        {
            return target.getRemoteAddr();
        }

        public String getRemoteHost()
        {
            return target.getRemoteHost();
        }

        public boolean isUserInRole(String role)
        {
            return target.isUserInRole(role);
        }

        public void setAttribute(String name,
                                 Object o)
        {
            target.setAttribute(name, o);
        }

        public Principal getUserPrincipal()
        {
            return target.getUserPrincipal();
        }

        public void removeAttribute(String name)
        {
            target.removeAttribute(name);
        }

        public String getRequestedSessionId()
        {
            return target.getRequestedSessionId();
        }

        public Locale getLocale()
        {
            return target.getLocale();
        }

        public String getRequestURI()
        {
            return target.getRequestURI();
        }

        public Enumeration<Locale> getLocales()
        {
            return target.getLocales();
        }

        public boolean isSecure()
        {
            return target.isSecure();
        }

        public StringBuffer getRequestURL()
        {
            return target.getRequestURL();
        }

        public RequestDispatcher getRequestDispatcher(String path)
        {
            return target.getRequestDispatcher(path);
        }

        public String getServletPath()
        {
            return target.getServletPath();
        }

        public String getRealPath(String path)
        {
            return target.getRealPath(path);
        }

        public HttpSession getSession(boolean create)
        {
            return target.getSession(create);
        }

        public int getRemotePort()
        {
            return target.getRemotePort();
        }

        public String getLocalName()
        {
            return target.getLocalName();
        }

        public String getLocalAddr()
        {
            return target.getLocalAddr();
        }

        public int getLocalPort()
        {
            return target.getLocalPort();
        }

        public ServletContext getServletContext()
        {
            return target.getServletContext();
        }

        public HttpSession getSession()
        {
            return target.getSession();
        }

        public AsyncContext startAsync()
        {
            return target.startAsync();
        }

        public String changeSessionId()
        {
            return target.changeSessionId();
        }

        public boolean isRequestedSessionIdValid()
        {
            return target.isRequestedSessionIdValid();
        }

        public boolean isRequestedSessionIdFromCookie()
        {
            return target.isRequestedSessionIdFromCookie();
        }

        public boolean isRequestedSessionIdFromURL()
        {
            return target.isRequestedSessionIdFromURL();
        }

        public boolean isRequestedSessionIdFromUrl()
        {
            return target.isRequestedSessionIdFromUrl();
        }

        public boolean authenticate(HttpServletResponse response)
                throws IOException,
                       ServletException
        {
            return target.authenticate(response);
        }

        public AsyncContext startAsync(ServletRequest servletRequest,
                                       ServletResponse servletResponse)
        {
            return target.startAsync(servletRequest, servletResponse);
        }

        public void login(String username,
                          String password)
                throws ServletException
        {
            target.login(username, password);
        }

        public void logout()
                throws ServletException
        {
            target.logout();
        }

        public Collection<Part> getParts()
                throws IOException,
                       ServletException
        {
            return target.getParts();
        }

        public boolean isAsyncStarted()
        {
            return target.isAsyncStarted();
        }

        public boolean isAsyncSupported()
        {
            return target.isAsyncSupported();
        }

        public Part getPart(String name)
                throws IOException,
                       ServletException
        {
            return target.getPart(name);
        }

        public AsyncContext getAsyncContext()
        {
            return target.getAsyncContext();
        }

        public DispatcherType getDispatcherType()
        {
            return target.getDispatcherType();
        }

        public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass)
                throws IOException,
                       ServletException
        {
            return target.upgrade(handlerClass);
        }

    }

}
