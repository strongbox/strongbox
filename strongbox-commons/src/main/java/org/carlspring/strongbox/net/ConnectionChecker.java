package org.carlspring.strongbox.net;

import java.io.IOException;
import java.net.*;

/**
 * @author mtodorov
 */
public class ConnectionChecker
{


    public static boolean checkServiceAvailability(String host, int port, int timeout)
            throws IOException
    {
        Socket socket = new Socket();
        try
        {
            socket.connect(new InetSocketAddress(host, port), timeout);
        }
        catch (SocketException e)
        {
            if (e.getMessage().contains("Connection refused"))
            {
                return false;
            }
        }
        catch (SocketTimeoutException e)
        {
            return false;
        }

        return socket.isConnected();
    }

}
