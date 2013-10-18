package org.carlspring.strongbox.storage;

import java.io.*;

/**
 * @author mtodorov
 */
public class StorageManager
{

    public static void createStorageOnFileSystem(String basedir, String storageName)
    {
        //noinspection ResultOfMethodCallIgnored
        new File(basedir, storageName).mkdirs();
    }

    public static void createRepositoryOnFileSystem(String basedir, String repositoryName)
    {
        //noinspection ResultOfMethodCallIgnored
        new File(basedir, repositoryName).mkdirs();
    }

    public static void createConfiguration(String basedir)
            throws IOException
    {
        String filename;
        File configurationFile = new File("etc/configuration.xml");

        if (!configurationFile.exists())
        {
            filename = System.getProperty("repository.config.xml") != null ?
                       System.getProperty("repository.config.xml") :
                       "etc/configuration.xml";

            InputStream is = null;
            OutputStream os = null;

            try
            {
                // Read it from the classpath and copy it over
                is = StorageManager.class.getResourceAsStream(filename);
                os = new FileOutputStream(new File(basedir + "/etc", "configuration.xml"));

                final int size = 4096;
                byte[] bytes = new byte[size];

                while (is.read(bytes, 0, size) != -1)
                {
                    os.write(bytes);
                    os.flush();
                }
            }
            finally
            {
                if (is != null)
                {
                    is.close();
                }
                if (os != null)
                {
                    os.close();
                }
            }
        }
    }

    public static void createServerLayout(String basedir)
            throws IOException
    {
        //noinspection ResultOfMethodCallIgnored
        new File(basedir).mkdirs();
        //noinspection ResultOfMethodCallIgnored
        new File(basedir + "/etc").mkdirs();

        StorageManager.createConfiguration(basedir);

        StorageManager.createStorageOnFileSystem(basedir + "/storages", "storage0");

        StorageManager.createRepositoryOnFileSystem(basedir + "/storages/storage0", "releases");
        StorageManager.createRepositoryOnFileSystem(basedir + "/storages/storage0", "snapshots");
    }

}
