package org.carlspring.strongbox.booters;

import java.nio.file.Paths;

import org.springframework.util.StringUtils;

/**
 * @author carlspring
 */
public class PropertiesBooter
{

    static
    {
        //initialize();
    }


    public PropertiesBooter()
    {
    }

    public static void initialize()
    {
        /*
        -Dstrongbox.home=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox
        -Dstrongbox.vault=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox-vault
        -Dstrongbox.storage.booter.basedir=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox-vault/storages
        -Dstrongbox.config.xml=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox/etc/conf/strongbox.xml
        -Dstrongbox.host=localhost
        -Dstrongbox.port=48080
        -Dstrongbox.nuget.download.feed=false

        -Dlogging.dir=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox-vault/logs
        -Dlogging.config.file=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox/etc/logback-debug.xml
        -Djava.io.tmpdir=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox-vault/tmp
        -Dehcache.disk.store.dir=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox/cache
        */

        // Explicitly declare the Strongbox system properties
        if (System.getProperty("strongbox.home") == null)
        {
            System.setProperty("strongbox.home",
                               getVariable("STRONGBOX_HOME", "strongbox.home", "strongbox"));
        }

        if (System.getProperty("strongbox.vault") == null)
        {
            System.setProperty("strongbox.vault",
                               getVariable("STRONGBOX_VAULT",
                                           "strongbox.vault",
                                           Paths.get("strongbox-vault").toAbsolutePath().toString()));
        }

        if (System.getProperty("strongbox.booter.basedir") == null)
        {
            System.setProperty("strongbox.booter.basedir",
                               Paths.get("strongbox-vault").resolve("storages").toAbsolutePath().toString());
        }

        if (System.getProperty("strongbox.config.xml") == null)
        {
            System.setProperty("strongbox.config.xml",
                               Paths.get(System.getProperty("strongbox.home"))
                                    .resolve("etc")
                                    .resolve("conf")
                                    .resolve("strongbox.xml")
                                    .toAbsolutePath().toString());
        }

        if (System.getProperty("strongbox.host") == null)
        {
            System.setProperty("strongbox.host", "localhost");
        }

        if (System.getProperty("strongbox.port") == null)
        {
            System.setProperty("strongbox.port", "48080");
        }

        if (System.getProperty("strongbox.nuget.download.feed") == null)
        {
            System.setProperty("strongbox.nuget.download.feed", "false");
        }

        // Explicitly declare any necessary system properties for third-party libraries
        /*
         -Dlogging.config.file=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox/etc/logback-debug.xml
         -Dlogging.dir=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox-vault/logs
         -Djava.io.tmpdir=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox-vault/tmp
         -Dehcache.disk.store.dir=/java/opensource/carlspring/strongbox/strongbox-web-core/target/strongbox/cache
         */

        if (System.getProperty("logging.config.file") == null)
        {
            System.setProperty("logging.config.file", getEtcDirectory() + "logback.xml");
        }

        if (System.getProperty("logging.dir") == null)
        {
            System.setProperty("logging.dir", getVaultDirectory() + "/logs");
        }

        if (System.getProperty("ehcache.disk.store.dir") == null)
        {
            System.setProperty("ehcache.disk.store.dir", getHomeDirectory() + "/cache");
        }
    }

    /**
     * This method checks if an environment variable exists and returns it.
     * If it doesn't, it checks if a system property exists for it. If this fails, it returns a default value.
     *
     * @param environmentVariableName
     * @param systemPropertyKey
     * @param defaultValue
     * @return
     */
    public static String getVariable(String environmentVariableName,
                                     String systemPropertyKey,
                                     String defaultValue)
    {
        if (!StringUtils.isEmpty(System.getenv(environmentVariableName)))
        {
            return System.getenv(environmentVariableName);
        }
        else if (System.getProperty(systemPropertyKey) != null)
        {
            return System.getProperty(systemPropertyKey);
        }
        else
        {
            return defaultValue;
        }
    }

    /**
     * This method checks if a system property exists for it. If this fails, it returns a default value.
     *
     * @param systemPropertyKey
     * @param defaultValue
     * @return
     */
    public static String getVariable(String systemPropertyKey,
                                     String defaultValue)
    {
        if (System.getProperty(systemPropertyKey) != null)
        {
            return System.getProperty(systemPropertyKey);
        }
        else
        {
            return defaultValue;
        }
    }

    public static String getHomeDirectory()
    {
        final String dir = getVariable("STRONGBOX_HOME", "strongbox.home", "strongbox");

        return resolvePath(dir, "strongbox");
    }

    public static String getVaultDirectory()
    {
        final String dir = getVariable("STRONGBOX_VAULT", "strongbox.vault", "strongbox-vault");

        return resolvePath(dir, "strongbox-vault");
    }

    public static String getEtcDirectory()
    {
        final String dir = getVariable("STRONGBOX_ETC", "strongbox.etc", "etc");

        return resolvePath(dir, null);
    }

    public static String getTempDirectory()
    {
        final String dir = getVariable("STRONGBOX_TMP",
                                       System.getProperty("java.io.tmpdir"),
                                       Paths.get(getVaultDirectory()).resolve("tmp").toString());

        return resolvePath(dir, null);
    }

    private static String resolvePath(final String basedir,
                                      final String defaultPath)
    {
        String pathToResolve = basedir != null ? basedir : defaultPath;

        return Paths.get(pathToResolve).toAbsolutePath().toString();
    }

}
