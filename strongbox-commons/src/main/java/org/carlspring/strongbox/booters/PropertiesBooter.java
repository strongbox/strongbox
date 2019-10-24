package org.carlspring.strongbox.booters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PropertiesBooter
{
    @Value("${strongbox.home}")
    private String homeDirectory;

    @Value("${strongbox.vault}")
    private String vaultDirectory;

    @Value("${strongbox.etc}")
    private String etcDirectory;

    @Value("${logging.dir}")
    private String logsDirectory;

    @Value("${strongbox.storage.booter.basedir}")
    private String storageBooterBasedir;

    @Value("${strongbox.config.file}")
    private String configFile;

    @Value("${strongbox.host:localhost}")
    private String host;

    @Value("${strongbox.port}")
    private int port;

    @Value("${strongbox.nuget.download.feed}")
    private boolean strongboxNugetDownloadFeed;

    @Value("${strongbox.version}")
    private String strongboxVersion;

    @Value("${strongbox.revision}")
    private String strongboxRevision;

    public String getHomeDirectory()
    {
        return homeDirectory;
    }

    public void setHomeDirectory(String homeDirectory)
    {
        this.homeDirectory = homeDirectory;
    }

    public String getVaultDirectory()
    {
        return vaultDirectory;
    }

    public void setVaultDirectory(String vaultDirectory)
    {
        this.vaultDirectory = vaultDirectory;
    }

    public String getEtcDirectory()
    {
        return etcDirectory;
    }

    public void setEtcDirectory(String etcDirectory)
    {
        this.etcDirectory = etcDirectory;
    }

    public String getLogsDirectory()
    {
        return logsDirectory;
    }

    public void setLogsDirectory(String logsDirectory)
    {
        this.logsDirectory = logsDirectory;
    }

    public String getStorageBooterBasedir()
    {
        return storageBooterBasedir;
    }

    public void setStorageBooterBasedir(String storageBooterBasedir)
    {
        this.storageBooterBasedir = storageBooterBasedir;
    }

    public String getConfigFile()
    {
        return configFile;
    }

    public void setConfigFile(String configFile)
    {
        this.configFile = configFile;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public boolean shouldDownloadStrongboxNugetFeed()
    {
        return strongboxNugetDownloadFeed;
    }

    public void setStrongboxNugetDownloadFeed(boolean strongboxNugetDownloadFeed)
    {
        this.strongboxNugetDownloadFeed = strongboxNugetDownloadFeed;
    }

    public String getStrongboxVersion()
    {
        return strongboxVersion;
    }

    public void setStrongboxVersion(String strongboxVersion)
    {
        this.strongboxVersion = strongboxVersion;
    }

    public String getStrongboxRevision()
    {
        return strongboxRevision;
    }

    public void setStrongboxRevision(String strongboxRevision)
    {
        this.strongboxRevision = strongboxRevision;
    }
}
