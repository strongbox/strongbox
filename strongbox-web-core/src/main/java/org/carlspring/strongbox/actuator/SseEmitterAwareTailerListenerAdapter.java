package org.carlspring.strongbox.actuator;

import java.io.IOException;
import java.util.Objects;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.io.EofException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author Przemyslaw Fusik
 */
public class SseEmitterAwareTailerListenerAdapter
        extends TailerListenerAdapter
{

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private SseEmitter sseEmitter;

    private Tailer tailer;

    public SseEmitterAwareTailerListenerAdapter(final SseEmitter sseEmitter)
    {
        Objects.requireNonNull(sseEmitter, "sseEmitter cannot be null");
        this.sseEmitter = sseEmitter;
    }

    @Override
    public void init(final Tailer tailer)
    {
        this.tailer = tailer;
    }

    @Override
    public void fileNotFound()
    {
        sseEmitter.completeWithError(new IllegalStateException("File not found"));
        stopListeningAndCleanupResources();
        logger.error("File not found");
    }

    @Override
    public void fileRotated()
    {
        logger.info("File rotated");
    }

    @Override
    public void handle(final String line)
    {
        try
        {
            sseEmitter.send(line);
        }
        catch (IllegalStateException isEx)
        {
            if (isEx.getMessage().contains("ResponseBodyEmitter is already set complete"))
            {
                stopListeningAndCleanupResources();
            }
        }
        catch (EofException eofEx)
        {
            stopListeningAndCleanupResources();
        }
        catch (IOException ioEx)
        {
            logger.error(String.format("Unable to send message [%s]", line), ioEx);
        }
    }

    @Override
    public void handle(final Exception ex)
    {
        sseEmitter.completeWithError(
                new IllegalStateException(String.format("Exception occurred [%s]", ExceptionUtils.getStackTrace(ex))));
        stopListeningAndCleanupResources();
        logger.error("Exception occurred.", ex);
    }

    private void stopListeningAndCleanupResources()
    {
        tailer.stop();
        tailer = null;

        try
        {
            sseEmitter.complete();
        }
        catch (Exception ex)
        {
            // swallow
        }
        sseEmitter = null;
    }
}
