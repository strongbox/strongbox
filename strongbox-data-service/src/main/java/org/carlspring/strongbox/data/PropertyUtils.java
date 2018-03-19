package org.carlspring.strongbox.data;


import java.nio.file.Paths;

/**
 * @author Alex Oreshkevich
 */
public class PropertyUtils
{

    public static String getVaultDirectory()
    {
        final String basedir = System.getenv("STRONGBOX_VAULT") != null ?
                               System.getenv("STRONGBOX_VAULT") :
                               System.getProperty("strongbox.vault");
        return resolvePath(basedir, "");
    }

    public static String getHomeDirectory()
    {
        final String basedir = System.getenv("STRONGBOX_HOME") != null ?
                               System.getenv("STRONGBOX_HOME") :
                               System.getProperty("strongbox.home");
        return resolvePath(basedir, "");
    }

    public static String getTempDirectory()
    {
        final String basedir = System.getenv("STRONGBOX_TMP") != null ?
                               System.getenv("STRONGBOX_TMP") :
                               System.getProperty("java.io.tmp");
        return resolvePath(basedir, Paths.get(getVaultDirectory()).resolve("tmp").toString());
    }

    public static String getEtcDirectory()
    {
        final String basedir = System.getenv("STRONGBOX_ETC") != null ?
                               System.getenv("STRONGBOX_ETC") :
                               System.getProperty("strongbox.etc");
        return resolvePath(basedir, "etc");
    }

    private static String resolvePath(final String basedir,
                                      final String defaultPath)
    {
        String pathToResolve = basedir != null ? basedir : defaultPath;
        return Paths.get(pathToResolve).toAbsolutePath().toString();
    }
}
