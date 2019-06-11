package org.carlspring.strongbox.net;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author mtodorov
 */
public class ConnectionCheckerTestIT
{

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Test
    public void testConnectToInvalidService()
            throws IOException
    {
        logger.debug("Searching for an unused port...");

        // Find an unused TCP port.
        final int port = SocketUtils.findAvailableTcpPort();

        logger.debug("Unused port found: {}", port);

        final boolean availability = ConnectionChecker.checkServiceAvailability("localhost", port, 3000);

        assertFalse(availability);
    }

    @Test
    public void testConnectToValidService()
            throws IOException
    {
        final ServerSocket serverSocket = new ServerSocket(0);

        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    //noinspection InfiniteLoopStatement
                    while (true)
                    {
                        serverSocket.accept();
                    }
                }
                catch (IOException e)
                {
                    fail("Failed to establish the connection!");
                }
            }
        };

        thread.start();

        final boolean availability = ConnectionChecker.checkServiceAvailability("localhost", serverSocket.getLocalPort(), 5000);

        thread.interrupt();

        assertTrue(availability, "Failed to connect!");
    }

}
