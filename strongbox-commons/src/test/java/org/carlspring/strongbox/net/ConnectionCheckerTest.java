package org.carlspring.strongbox.net;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
public class ConnectionCheckerTest
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
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    ServerSocket serverSocket = new ServerSocket(65533);

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

        final boolean availability = ConnectionChecker.checkServiceAvailability("localhost", 65533, 3000);

        thread.interrupt();

        assertTrue(availability);
    }

}
