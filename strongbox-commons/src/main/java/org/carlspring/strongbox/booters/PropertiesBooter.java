package org.carlspring.strongbox.booters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
//TODO: SB-1266
@Component
public class PropertiesBooter
{

    @Value("${strongbox.home}")
    private String homeDirectory;

    @Value("${strongbox.vault:strongbox-vault}")
    private String vaultDirectory;

    @Value("${strongbox.etc:strongbox/etc}")
    private String etcDirectory;

    @Value("k${strongbox.temp:strongbox/tmp}")
    private String tempDirectory;

    @Value("${strongbox.storage.booter.basedir:strongbox-vault/storages}")
    private String storageBooterBasedir;

    @Value("${strongbox.config.xml:strongbox/etc/conf/strongbox.xml}")
    private String configFile;

    @Value("${strongbox.host:localhost}")
    private String host;

    @Value("${strongbox.port:48080}")
    private int port;

    @Value("${strongbox.nuget.download.feed:false}")
    private boolean strongboxNugetDownloadFeed;


//        -Dstrongbox.home=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox
//        -Dstrongbox.vault=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox-vault
//        -Dstrongbox.storage.booter.basedir=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox-vault/storages
//        -Dstrongbox.config.xml=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox/etc/conf/strongbox.xml
//        -Dstrongbox.host=localhost
//        -Dstrongbox.port=48080
//        -Dstrongbox.nuget.download.feed=false
//
//        -Dlogging.dir=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox-vault/logs
//        -Dlogging.config.file=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox/etc/logback-debug.xml
//        -Djava.io.tmpdir=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox-vault/tmp
//        -Dehcache.disk.store.dir=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox/cache


    public PropertiesBooter()
    {
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
