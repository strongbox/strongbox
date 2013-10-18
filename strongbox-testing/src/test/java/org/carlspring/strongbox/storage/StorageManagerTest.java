package org.carlspring.strongbox.storage;

import java.io.File;

import org.junit.Test;
import static junit.framework.Assert.assertTrue;

/**
 * @author mtodorov
 */
public class StorageManagerTest
{

    public static final long currentMillis = System.currentTimeMillis();


    @Test
    public void testCreateStorage()
    {
        StorageManager.createStorageOnFileSystem("target/storages", "storage-" + currentMillis);

        final File storageDir = new File("target/storages", "storage-" + currentMillis);

        assertTrue("Failed to create storage!", storageDir.exists() && storageDir.isDirectory());
    }

    @Test
    public void testCreateRepository()
    {
        StorageManager.createRepositoryOnFileSystem("target/storages/storage-" + currentMillis, "releases");

        final File storageDir = new File("target/storages/storage-" + currentMillis, "releases");

        assertTrue("Failed to create storage!", storageDir.exists() && storageDir.isDirectory());
    }

}
