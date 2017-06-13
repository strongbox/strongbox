package org.carlspring.strongbox.providers.repository.proxy;

import org.carlspring.strongbox.client.ArtifactResolver;

import javax.ws.rs.client.Client;
import java.io.IOException;

import org.glassfish.jersey.client.ClientProperties;

/**
 * @author carlspring
 */
public class RemoteRepositoryStatusChecker extends Thread
{

    private RemoteRepositoryStatusInfo remoteRepositoryStatusInfo;


    public RemoteRepositoryStatusChecker()
    {
    }

    public boolean checkRemote(Client client)
            throws IOException
    {
        ArtifactResolver resolver = new ArtifactResolver(client);
        resolver.getClientInstance().property(ClientProperties.CONNECT_TIMEOUT, 5000);
        resolver.getClientInstance().property(ClientProperties.READ_TIMEOUT, 5000);
        resolver.getClientInstance().property(ClientProperties.FOLLOW_REDIRECTS, "true");

        // Retry three times, if there is an exception
        for (int i = 0; i < 3; i++)
        {
            try
            {
                // TODO: This will not work for repositories that don't allow directory browsing.
                // TODO: Further work will be required to improve this.
                resolver.pathExists(getRemoteRepositoryStatusInfo().getUrl());

                remoteRepositoryStatusInfo.setLastCheckedStatus(System.currentTimeMillis());
                remoteRepositoryStatusInfo.setStatus(RemoteRepositoryStatusEnum.ONLINE.getStatus());

                return true;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        remoteRepositoryStatusInfo.setLastCheckedStatus(System.currentTimeMillis());
        remoteRepositoryStatusInfo.setStatus(RemoteRepositoryStatusEnum.OFFLINE.getStatus());

        return false;
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
