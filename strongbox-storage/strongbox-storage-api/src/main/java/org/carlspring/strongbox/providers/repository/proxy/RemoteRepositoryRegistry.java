package org.carlspring.strongbox.providers.repository.proxy;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class RemoteRepositoryRegistry
{

    private static final Logger logger = LoggerFactory.getLogger(RemoteRepositoryRegistry.class);

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
        if (remoteRepositoryStatusInfos.containsKey(remoteRepositoryStatusInfo.getUrl()) &&
            !remoteRepositoryStatusInfos.get(remoteRepositoryStatusInfo.getUrl()).equals(remoteRepositoryStatusInfo))
        {
            // Generally, there should be just one proxy per remote host,
            // but who knows what people might configure, or mis-configure...
            // In essence, it makes little sense to have more than one configuration per remote host.
            logger.warn("An entry for " + remoteRepositoryStatusInfo.getUrl() + " already exists! " +
                        "Overriding previous configuration.");
        }

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
