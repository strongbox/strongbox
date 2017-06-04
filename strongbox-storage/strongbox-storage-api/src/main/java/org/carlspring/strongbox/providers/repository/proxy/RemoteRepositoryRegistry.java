package org.carlspring.strongbox.providers.repository.proxy;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class RemoteRepositoryRegistry
{

    /**
     * K, V: URL, Status
     */
    private Map<String, RemoteRepositoryStatusInfo> remoteRepositoryStatusInfos = new LinkedHashMap<>();


    public RemoteRepositoryRegistry()
    {
    }

    public String getRepositoryStatus(String url)
    {
        return remoteRepositoryStatusInfos.get(url).getStatus();
    }

    public void addRepositoryInfo(RemoteRepositoryStatusInfo remoteRepositoryStatusInfo)
    {
        remoteRepositoryStatusInfos.put(remoteRepositoryStatusInfo.getUrl(), remoteRepositoryStatusInfo);
    }

    public RemoteRepositoryStatusInfo getRepositoryInfo(RemoteRepositoryStatusInfo remoteRepositoryStatusInfo)
    {
        return remoteRepositoryStatusInfos.get(remoteRepositoryStatusInfo.getUrl());
    }

    public void removeRepositoryInfo(String url)
    {
        remoteRepositoryStatusInfos.remove(url);
    }

    public Map<String, RemoteRepositoryStatusInfo> getRemoteRepositoryStatusInfos()
    {
        return remoteRepositoryStatusInfos;
    }

    public void setRemoteRepositoryStatusInfos(Map<String, RemoteRepositoryStatusInfo> remoteRepositoryStatusInfos)
    {
        this.remoteRepositoryStatusInfos = remoteRepositoryStatusInfos;
    }

}
