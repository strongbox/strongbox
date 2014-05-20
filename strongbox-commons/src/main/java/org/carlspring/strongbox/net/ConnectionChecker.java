package org.carlspring.strongbox.net;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

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
        catch (ConnectException e)
        {
            if (e.getMessage().contains("Connection refused"))
            {
                return false;
            }
        }

        return socket.isConnected();
    }

}
