package org.carlspring.strongbox.storage.repository.remote.heartbeat.monitor;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Przemyslaw Fusik
 */
enum HttpGetRemoteRepositoryCheckStrategy
        implements RemoteRepositoryHeartbeatMonitorStrategy
{

    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(HttpGetRemoteRepositoryCheckStrategy.class);

    @Override
    public boolean isAlive(String remoteRepositoryUrl)
    {
        boolean response = false;
        try
        {
            int statusCode = Request.Get(
                    remoteRepositoryUrl).execute().returnResponse().getStatusLine().getStatusCode();

            response = HttpStatus.SC_OK == statusCode || HttpStatus.SC_MOVED_PERMANENTLY == statusCode ||
                       HttpStatus.SC_MOVED_TEMPORARILY == statusCode;
        }
        catch (IOException e)
        {
            logger.error("Problem executing HTTP GET request to " + remoteRepositoryUrl, e);
        }
        finally
        {
            return response;
        }
    }
}
