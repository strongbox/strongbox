package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.storage.StorageManager;

import java.io.File;

/**
 * @author mtodorov
 */
public class StrongboxDirectoryLayoutManager
{

    public static void createDirectoryStructures(String strongboxBasedir)
    {
        final File basedir = new File(strongboxBasedir);

        //noinspection ResultOfMethodCallIgnored
        basedir.mkdirs();

        StorageManager.createStorageOnFileSystem(basedir + "/storages", "storage0");

        StorageManager.createRepositoryOnFileSystem(basedir + "/storages/storage0", "releases");
        StorageManager.createRepositoryOnFileSystem(basedir + "/storages/storage0", "snapshots");
    }

}
