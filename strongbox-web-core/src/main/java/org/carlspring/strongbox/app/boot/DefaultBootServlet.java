package org.carlspring.strongbox.app.boot;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class DefaultBootServlet
        extends HttpServlet
{

    private static final Logger logger = LoggerFactory.getLogger(DefaultBootServlet.class);

    private static final String assetsPath = "/static/assets/";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            String requestedUri = request.getRequestURI();

            // Strip context path from the beginning.
            if (StringUtils.isNotBlank(getServletContext().getContextPath()))
            {
                requestedUri = requestedUri.substring(getServletContext().getContextPath().length());
            }

            String requestedResource = "/index.html";

            // Set default status code for all requests
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());

            // Serve assets
            if (requestedUri.startsWith(assetsPath))
            {
                response.setStatus(HttpStatus.OK.value());
                requestedResource = requestedUri;
            }

            readResource(requestedResource, response);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private void readResource(String requestedResource, HttpServletResponse response)
    {
        try (InputStream inputStream = DefaultBootServlet.class.getResourceAsStream(requestedResource))
        {
            String mime = MediaType.APPLICATION_OCTET_STREAM_VALUE;

            if (requestedResource.endsWith(".html"))
            {
                mime = MediaType.TEXT_HTML_VALUE;
            }
            else if (requestedResource.endsWith(".css"))
            {
                mime = "text/css";
            }
            else if (requestedResource.endsWith(".js"))
            {
                mime = "application/javascript";
            }

            response.setContentType(mime);

            response.flushBuffer();

            IOUtils.copy(inputStream, response.getOutputStream());
        }
        catch (IOException e)
        {
            logger.warn("Failed to read file from classpath!", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private void serviceUnavailable(HttpServletResponse response)
    {
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        serviceUnavailable(response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        serviceUnavailable(response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        serviceUnavailable(response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        serviceUnavailable(response);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        serviceUnavailable(response);
    }

    @Override
    protected void doTrace(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        serviceUnavailable(response);
    }
}
