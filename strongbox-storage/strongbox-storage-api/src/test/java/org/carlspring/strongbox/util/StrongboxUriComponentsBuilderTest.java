package org.carlspring.strongbox.util;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.web.Constants.ARTIFACT_ROOT_PATH;
import static org.carlspring.strongbox.web.Constants.BROWSE_ROOT_PATH;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StrongboxUriComponentsBuilderTest
{

    private static final Logger logger = LoggerFactory.getLogger(StrongboxUriComponentsBuilderTest.class);

    @BeforeEach
    public void init()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetCurrentRequestURI()
    {
        final String requestURI = "/api/path/to/keep/without/trailing/slash/";
        final String queryString = "query=string&should=be&kept=false";

        MockHttpServletRequest request = this.mockRequest();
        request.setQueryString(queryString);
        request.setRequestURI(requestURI);
        request.setContextPath("/");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try
        {
            StrongboxUriComponentsBuilder builder = mockBuilder();
            String result = builder.getCurrentRequestURI();
            String expectedRequestURI = StringUtils.removeEnd(requestURI, "/");

            logger.debug(result);

            assertThat(result).isEqualTo(expectedRequestURI);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testGetCurrentRequestURL()
            throws MalformedURLException
    {
        final String requestURI = "/api/path/to/keep/without/trailing/slash/";
        final String queryString = "query=string&should=be&kept=false";

        MockHttpServletRequest request = this.mockRequest();
        request.setQueryString(queryString);
        request.setRequestURI(requestURI);
        request.setContextPath("/");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try
        {
            StrongboxUriComponentsBuilder builder = mockBuilder();
            String result = builder.getCurrentRequestURL().toString();
            String expectedRequestURI = "http://localhost:48080" + StringUtils.removeEnd(requestURI, "/");

            logger.debug(result);

            assertThat(result).isEqualTo(expectedRequestURI);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testGetBuilderShouldKeepRequestURIAndQuery()
    {
        final String uri = "/api/path/to/keep";
        final String queryString = "query=string&should=be&kept=true";

        MockHttpServletRequest request = this.mockRequest();
        request.setQueryString(queryString);
        request.setRequestURI(uri);
        request.setContextPath("/");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try
        {
            StrongboxUriComponentsBuilder builder = mockBuilder();
            String result = builder.getBuilder(false, false, null).toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080" + uri + "?" + queryString);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testGetBuilderShouldKeepRequestURIButRemoveQuery()
    {
        final String uri = "/api/path/to/keep";
        final String queryString = "query=string&should=be&kept=false";

        MockHttpServletRequest request = this.mockRequest();
        request.setQueryString(queryString);
        request.setRequestURI(uri);
        request.setContextPath("/");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try
        {
            StrongboxUriComponentsBuilder builder = mockBuilder();
            String result = builder.getBuilder(false, true, null).toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080" + uri);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testGetBuilderShouldRemoveRequestURIAndQuery()
    {
        final String uri = "/api/path/to/remove";
        final String queryString = "query=string&should=be&kept=false";

        MockHttpServletRequest request = this.mockRequest();
        request.setQueryString(queryString);
        request.setRequestURI(uri);
        request.setContextPath("/");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try
        {
            String result = mockBuilder().getBuilder().toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080/");
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testGetBuilderShouldRemovePathButKeepContextPath()
    {
        final String uri = "/api/path/to/remove";
        final String context = "/strongbox";

        MockHttpServletRequest request = this.mockRequest();
        request.setRequestURI(uri);
        request.setContextPath(context);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try
        {
            String result = mockBuilder().getBuilder().toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080" + context);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testGetBuilderShouldRemoveRequestURIAndQueryByDefault()
    {
        MockHttpServletRequest request = this.mockRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try
        {
            String result = mockBuilder().getBuilder().toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080/");
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testGetBuilderShouldForceBaseURLWhenConfigured()
    {
        MockHttpServletRequest request = this.mockRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try
        {
            final String forcedBaseURL = "https://1.2.3.4:4321/path";
            String result = mockBuilder(forcedBaseURL).getBuilder().toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo(forcedBaseURL);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testGetBuilderShouldAppendPath()
    {
        MockHttpServletRequest request = this.mockRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try
        {
            final String additionalPath = "/additional/path";
            String result = mockBuilder().getBuilder(additionalPath).toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080" + additionalPath);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testGetBuilderShouldAppendPathAndKeepContextPath()
    {
        MockHttpServletRequest request = this.mockRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        final String context = "/strongbox";
        request.setContextPath(context);

        try
        {
            final String additionalPath = "/additional/path";
            String result = mockBuilder().getBuilder(additionalPath).toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080" + context + additionalPath);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testGetBuilderProxyForwardSupport()
            throws Exception
    {
        MockHttpServletRequest request = this.mockProxyForwardRequest("https", "carlspring.org", 8443, "/strongbox");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(adaptFromForwardedHeaders(request)));

        try
        {
            String result = mockBuilder(null).getBuilder().toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("https://carlspring.org:8443/strongbox");
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testBrowseUriBuilderWithNull()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            mockBuilder().browseUriBuilder(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            mockBuilder().browseUriBuilder(null, null);
        });
    }

    @Test
    public void testBrowseUriBuilder()
    {
        MockHttpServletRequest request = this.mockRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try
        {
            String result = mockBuilder().browseUriBuilder().toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080%s", BROWSE_ROOT_PATH);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testBrowseUriBuilderWithStorageId()
    {
        MockHttpServletRequest request = this.mockRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String storageId = "my-storage-id";

        try
        {
            UriComponentsBuilder uri = mockBuilder().browseUriBuilder(storageId);
            String result = uri.toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080%s/%s", BROWSE_ROOT_PATH, storageId);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testBrowseUriBuilderWithStorageIdRepositoryId()
    {
        MockHttpServletRequest request = this.mockRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String storageId = "my-storage-id";
        String repositoryId = "my-repository-id";

        try
        {
            UriComponentsBuilder uri = mockBuilder().browseUriBuilder(storageId, repositoryId, (String) null);
            String result = uri.toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080%s/%s/%s",
                                         BROWSE_ROOT_PATH,
                                         storageId,
                                         repositoryId);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testBrowseUriBuilderWithStorageIdRepositoryIdAndArtifactPath()
    {
        MockHttpServletRequest request = this.mockRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String storageId = "my-storage-id";
        String repositoryId = "my-repository-id";
        String artifactPath = "org/carlspring/strongbox/strongbox-distribution/maven-metadata.xml";

        try
        {
            UriComponentsBuilder uri = mockBuilder().browseUriBuilder(storageId, repositoryId, artifactPath);
            String result = uri.toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080%s/%s/%s/%s",
                                         BROWSE_ROOT_PATH,
                                         storageId,
                                         repositoryId,
                                         artifactPath);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testBrowseUriBuilderWithStorageIdRepositoryIdAndArtifactResourceURI()
            throws URISyntaxException
    {
        MockHttpServletRequest request = this.mockRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String storageId = "my-storage-id";
        String repositoryId = "my-repository-id";
        URI artifactResourceURI = new URI("/org/carlspring/strongbox/strongbox-distribution/maven-metadata.xml");

        try
        {
            UriComponentsBuilder uri = mockBuilder().browseUriBuilder(storageId,
                                                                       repositoryId,
                                                                       artifactResourceURI);
            String result = uri.toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080%s/%s/%s/%s",
                                         BROWSE_ROOT_PATH,
                                         storageId,
                                         repositoryId,
                                         StringUtils.removeStart(artifactResourceURI.getPath(), "/"));
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testStorageUriBuilderWithNull()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            mockBuilder().storageUriBuilder(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            mockBuilder().storageUriBuilder(null, null);
        });
    }

    @Test
    public void testStorageUriBuilder()
    {
        MockHttpServletRequest request = this.mockRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try
        {
            String result = mockBuilder().storageUriBuilder().toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080%s", ARTIFACT_ROOT_PATH);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testStorageUriBuilderWithStorageId()
    {
        MockHttpServletRequest request = this.mockRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String storageId = "my-storage-id";

        try
        {
            UriComponentsBuilder uri = mockBuilder().storageUriBuilder(storageId);
            String result = uri.toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080%s/%s", ARTIFACT_ROOT_PATH, storageId);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testStorageUriBuilderWithStorageIdRepositoryId()
    {
        MockHttpServletRequest request = this.mockRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String storageId = "my-storage-id";
        String repositoryId = "my-repository-id";

        try
        {
            UriComponentsBuilder uri = mockBuilder().storageUriBuilder(storageId, repositoryId, (String) null);
            String result = uri.toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080%s/%s/%s",
                                         ARTIFACT_ROOT_PATH,
                                         storageId,
                                         repositoryId);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testStorageUriBuilderWithStorageIdRepositoryIdAndArtifactPath()
    {
        MockHttpServletRequest request = this.mockRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String storageId = "my-storage-id";
        String repositoryId = "my-repository-id";
        String artifactPath = "org/carlspring/strongbox/strongbox-distribution/maven-metadata.xml";

        try
        {
            UriComponentsBuilder uri = mockBuilder().storageUriBuilder(storageId, repositoryId, artifactPath);
            String result = uri.toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080%s/%s/%s/%s",
                                         ARTIFACT_ROOT_PATH,
                                         storageId,
                                         repositoryId,
                                         artifactPath);
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    public void testStorageUriBuilderWithStorageIdRepositoryIdAndArtifactResourceURI()
            throws URISyntaxException
    {
        MockHttpServletRequest request = this.mockRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String storageId = "my-storage-id";
        String repositoryId = "my-repository-id";
        URI artifactResourceURI = new URI("/org/carlspring/strongbox/strongbox-distribution/maven-metadata.xml");

        try
        {
            UriComponentsBuilder uri = mockBuilder().storageUriBuilder(storageId,
                                                                       repositoryId,
                                                                       artifactResourceURI);
            String result = uri.toUriString();
            logger.debug(result);
            assertThat(result).isEqualTo("http://localhost:48080%s/%s/%s%s",
                                         ARTIFACT_ROOT_PATH,
                                         storageId,
                                         repositoryId,
                                         artifactResourceURI.getPath());
        }
        finally
        {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    private StrongboxUriComponentsBuilder mockBuilder()
    {
        return mockBuilder("http://localhost:48080");
    }

    private StrongboxUriComponentsBuilder mockBuilder(String baseUrl)
    {
        UriComponents components = baseUrl != null ? UriComponentsBuilder.fromUriString(baseUrl).build() : null;

        StrongboxUriComponentsBuilder mock = Mockito.mock(StrongboxUriComponentsBuilder.class,
                                                          Mockito.CALLS_REAL_METHODS);
        Mockito.doReturn(components).when(mock).getConfigurationBaseUriComponents();

        Assertions.assertEquals(components, mock.getConfigurationBaseUriComponents());

        return mock;
    }

    private MockHttpServletRequest mockRequest()
    {
        return mockRequest("http", "localhost", 48080, "/", "/");
    }

    private MockHttpServletRequest mockRequest(String scheme,
                                               String serverName,
                                               int serverPort,
                                               String requestURI,
                                               String contextPath)
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme(scheme);
        request.setServerName(serverName);
        request.setServerPort(serverPort);
        request.setRequestURI(requestURI);
        request.setContextPath(contextPath);

        return request;
    }

    private MockHttpServletRequest mockProxyForwardRequest(String proto,
                                                           String host,
                                                           int port,
                                                           String prefix)
    {
        MockHttpServletRequest request = this.mockRequest();

        request.addHeader("X-Forwarded-Proto", proto);
        request.addHeader("X-Forwarded-Host", host);
        request.addHeader("X-Forwarded-Port", port);

        if (prefix != null)
        {
            request.setContextPath(prefix);
            request.addHeader("X-Forwarded-Prefix", prefix);
        }

        return request;
    }

    // SPR-16668: this method is required because the it properly handles any `x-forwarded-*` logic.
    private HttpServletRequest adaptFromForwardedHeaders(HttpServletRequest request)
            throws Exception
    {
        MockFilterChain chain = new MockFilterChain();
        new ForwardedHeaderFilter().doFilter(request, new MockHttpServletResponse(), chain);
        return (HttpServletRequest) chain.getRequest();
    }
}
