package org.carlspring.strongbox.app.boot;

import org.carlspring.strongbox.app.boot.listeners.BootProgressAsyncListener;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class BootProgressServlet
        extends HttpServlet
{

    private static final Logger logger = LoggerFactory.getLogger(BootProgressServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            if (!MediaType.TEXT_EVENT_STREAM_VALUE.equalsIgnoreCase(request.getHeader("Accept")))
            {
                response.sendError(HttpStatus.NOT_ACCEPTABLE.value());
                return;
            }

            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
            response.setCharacterEncoding("UTF-8");

            response.setHeader("Connection", "keep-alive");
            response.setHeader("Cache-control", "no-cache, no-store, max-age=0, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            // -- CORS - allow everybody while booting, but set max-age to force browsers re-check after boot.
            response.setHeader("Allow", "GET, OPTIONS");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Max-Age", "600");

            final AsyncContext asyncContext = request.startAsync(request, response);

            final Disposable disposable = BootProgressBeanPostProcessor.getProgressObservable()
                                                                       .doOnComplete(() -> {
                                                                           write("booted", "", asyncContext);
                                                                           asyncContext.getResponse().getWriter().flush();
                                                                           asyncContext.complete();
                                                                       })
                                                                       .doOnError((e) -> {
                                                                           logger.error("An error occurred!", e);
                                                                           asyncContext.complete();
                                                                       })
                                                                       .subscribe(data -> write("booting",
                                                                                                data,
                                                                                                asyncContext));

            asyncContext.addListener(new BootProgressAsyncListener(disposable));
        }
        catch (Exception e)
        {
            logger.error("Failed to process request {}", request.getRequestURI(), e);

            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private void write(String event, String data, AsyncContext asyncContext)
            throws IOException
    {
        PrintWriter writer = asyncContext.getResponse().getWriter();
        writer.println(String.format("event: %s", event));
        writer.println(String.format("data: %s", data));
        writer.println();
        writer.println();
        writer.flush();
    }

}
