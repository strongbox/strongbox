package org.carlspring.strongbox.net;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author mtodorov
 */
public class ConnectionCheckerTestIT
{

    @Test
    public void testConnectToInvalidService()
            throws IOException
    {
        // We're assuming there's nothing running on port 65534.
        final boolean availability = ConnectionChecker.checkServiceAvailability("localhost", 65534, 3000);

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
