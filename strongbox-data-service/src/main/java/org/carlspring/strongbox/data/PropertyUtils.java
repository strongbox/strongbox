package org.carlspring.strongbox.data;

import java.io.File;

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
        if (basedir != null)
        {
            return new File(basedir).getAbsolutePath();
        }
        else
        {
            return new File(".").getAbsolutePath();
        }
    }

    public static String getHomeDirectory()
    {
        final String basedir = System.getenv("STRONGBOX_HOME") != null ?
                               System.getenv("STRONGBOX_HOME") :
                               System.getProperty("strongbox.home");
        if (basedir != null)
        {
            return new File(basedir).getAbsolutePath();
        }
        else
        {
            return new File(".").getAbsolutePath();
        }
    }

    public static String getTempDirectory()
    {
        final String basedir = System.getenv("STRONGBOX_TMP") != null ?
                               System.getenv("STRONGBOX_TMP") :
                               System.getProperty("java.io.tmp");
        if (basedir != null)
        {
            return new File(basedir).getAbsolutePath();
        }
        else
        {
            return new File(getVaultDirectory() + "/tmp").getAbsolutePath();
        }
    }

    public static String getEtcDirectory()
    {
        final String basedir = System.getenv("STRONGBOX_ETC") != null ?
                               System.getenv("STRONGBOX_ETC") :
                               System.getProperty("strongbox.etc");
        if (basedir != null)
        {
            return new File(basedir).getAbsolutePath();
        }
        else
        {
            return new File("etc").getAbsolutePath();
        }
    }
}
