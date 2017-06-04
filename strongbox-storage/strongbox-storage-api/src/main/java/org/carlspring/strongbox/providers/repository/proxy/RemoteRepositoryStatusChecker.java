package org.carlspring.strongbox.providers.repository.proxy;

import java.io.IOException;

/**
 * @author carlspring
 */
public class RemoteRepositoryStatusChecker extends Thread
{

    private RemoteRepositoryStatusInfo remoteRepositoryStatusInfo;


    public RemoteRepositoryStatusChecker()
    {
    }

    public void checkRemote()
            throws IOException
    {
        /*
        URL url = new URL(remoteRepositoryStatusInfo.getUrl());
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(remoteRepositoryStatusInfo.getCheckInterval());
        connection.connect();

        connection.
        */
        /*
        RestClient client = new RestClient();
        client.setContextBaseUrl();
        */
    }

    public RemoteRepositoryStatusInfo getRemoteRepositoryStatusInfo()
    {
        return remoteRepositoryStatusInfo;
    }

    public void setRemoteRepositoryStatusInfo(RemoteRepositoryStatusInfo remoteRepositoryStatusInfo)
    {
        this.remoteRepositoryStatusInfo = remoteRepositoryStatusInfo;
    }

}
