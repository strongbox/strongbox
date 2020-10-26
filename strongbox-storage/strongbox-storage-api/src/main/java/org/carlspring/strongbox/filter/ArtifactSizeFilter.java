package org.carlspring.strongbox.filter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;

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
        String lengthHeader = req.getHeader(CONTENT_LENGTH);
        
        if(StringUtils.isNotEmpty(lengthHeader))
        {
            checkIfSizeInBoundaries(lengthHeader);
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy()
    {
        // do nothing
    }

    private void checkIfSizeInBoundaries(String lengthHeader) throws MaxUploadSizeExceededException
    {
        long contentSize = Long.parseLong(lengthHeader);
        
        boolean sizeWithinBoundaries = contentSize <= multipartConfigElement.getMaxFileSize();
        boolean boundariesSet = multipartConfigElement.getMaxFileSize() != -1;
        
        if(boundariesSet && !sizeWithinBoundaries)
        {
            throw new MaxUploadSizeExceededException(multipartConfigElement.getMaxFileSize());
        }
    }
}
