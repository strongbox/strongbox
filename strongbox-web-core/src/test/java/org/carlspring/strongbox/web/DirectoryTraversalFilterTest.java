package org.carlspring.strongbox.web;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Przemyslaw Fusik
 */
public class DirectoryTraversalFilterTest
{

    DirectoryTraversalFilter filter = new DirectoryTraversalFilter();

    @Test
    public void shouldDisallowTraversalPaths()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("get",
                                                                    "http://localhost:48080/storages/storage-common-proxies/maven-central/../../storage-common-proxies/maven-central");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void shouldDisallowTraversalPathsWithEncodedDots()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("get",
                                                                    "http://localhost:48080/storages/storage-common-proxies/maven-central/%2e%2e/storage-common-proxies/maven-central");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void shouldDisallowTraversalPathsWithEncodedDotsAndSlash()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("get",
                                                                    "http://localhost:48080/storages/storage-common-proxies/maven-central/%2e%2e%2fstorage-common-proxies/maven-central");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void shouldDisallowTraversalPathsWithEncodedSlash()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("get",
                                                                    "http://localhost:48080/storages/storage-common-proxies/maven-central/..%2fstorage-common-proxies/maven-central");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void shouldAllowNormalizedPath()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("get",
                                                                    "http://localhost:48080/storages/storage-common-proxies/maven-central/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isNotEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

}