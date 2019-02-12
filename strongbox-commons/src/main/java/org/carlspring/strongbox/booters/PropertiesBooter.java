package org.carlspring.strongbox.booters;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class PropertiesBooter
{

    @Value("${strongbox.home}")
    private String homeDirectory;

    @Value("${strongbox.vault:${strongbox.home}/../strongbox-vault}")
    private String vaultDirectory;

    @Value("${strongbox.etc:${strongbox.home}/etc}")
    private String etcDirectory;

    @Value("${strongbox.temp:strongbox/tmp}")
    private String tempDirectory;

    @Value("${logging.dir:${strongbox.vault}/logs}")
    private String logsDirectory;

    @Value("${strongbox.storage.booter.basedir:${strongbox.vault}/storages}")
    private String storageBooterBasedir;

    @Value("${strongbox.config.xml:${strongbox.home}/etc/conf/strongbox.xml}")
    private String configFile;

    @Value("${strongbox.host:localhost}")
    private String host;

    @Value("${strongbox.port:48080}")
    private int port;

    @Value("${strongbox.nuget.download.feed:false}")
    private boolean strongboxNugetDownloadFeed;


    public PropertiesBooter()
    {
    }

    /**
     * Initialization method that sets default system properties, if none are set.
     */
    @PostConstruct
    public void init()
    {
        if (System.getProperty("logging.dir") == null)
        {
            System.setProperty("logging.dir", getVaultDirectory() + "/logs");
        }

        if (System.getProperty("logging.config.file") == null)
        {
            System.setProperty("logging.config.file", getHomeDirectory() + "/etc/logback.xml");
        }

        if (System.getProperty("java.io.tmpdir") == null)
        {
            System.setProperty("java.io.tmpdir", getVaultDirectory() + "/tmp");
        }

        if (System.getProperty("ehcache.disk.store.dir") == null)
        {
            System.setProperty("ehcache.disk.store.dir", getHomeDirectory() + "/cache");
        }
    }

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

    public String getTempDirectory()
    {
        return tempDirectory;
    }

    public void setTempDirectory(String tempDirectory)
    {
        this.tempDirectory = tempDirectory;
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

}
