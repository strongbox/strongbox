package org.carlspring.strongbox.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class ArtifactSizeFilter implements Filter
{

    @Inject
    private MultipartConfigElement multipartConfigElement;


    @Override
    public void init(final FilterConfig filterConfig) throws ServletException
    {
        //do nothing
    }

    @Override
    public void doFilter(final ServletRequest request,
                         final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        String lengthHeader = req.getHeader("Content-Length");
        if(lengthHeader != null && !isSizeWithinBoundaries(lengthHeader))
        {
            throw new MaxUploadSizeExceededException(multipartConfigElement.getMaxFileSize());
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy()
    {
        // do nothing
    }

    private boolean isSizeWithinBoundaries(String lengthHeader)
    {
        long contentSize = Long.parseLong(lengthHeader);
        boolean sizeWithinBoundaries = contentSize <= multipartConfigElement.getMaxFileSize();
        boolean boundariesNotSet = multipartConfigElement.getMaxFileSize() == -1;
        return boundariesNotSet || sizeWithinBoundaries;
    }
}
