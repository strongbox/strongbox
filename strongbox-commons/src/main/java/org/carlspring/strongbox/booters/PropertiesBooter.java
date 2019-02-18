package org.carlspring.strongbox.booters;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
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

    @Value("${strongbox.temp}")
    private String tempDirectory;

    @Value("${logging.dir}")
    private String logsDirectory;

    @Value("${strongbox.storage.booter.basedir}")
    private String storageBooterBasedir;

    @Value("${strongbox.config.xml}")
    private String configFile;

    @Value("${strongbox.host:localhost}")
    private String host;

    @Value("${strongbox.port}")
    private int port;

    @Value("${strongbox.nuget.download.feed}")
    private boolean strongboxNugetDownloadFeed;

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
            System.setProperty("logging.config.file", getHomeDirectory() + "/etc/logback-spring.xml");
        }

        if (System.getProperty("java.io.tmpdir") == null)
        {
            System.setProperty("java.io.tmpdir", getVaultDirectory() + "/tmp");
        }

        if (System.getProperty("ehcache.disk.store.dir") == null)
        {
            System.setProperty("ehcache.disk.store.dir", getHomeDirectory() + "/cache");
        }
        
        if (System.getProperty("strongbox.storage.booter.basedir") == null)
        {
            System.setProperty("strongbox.storage.booter.basedir", getStorageBooterBasedir());
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
