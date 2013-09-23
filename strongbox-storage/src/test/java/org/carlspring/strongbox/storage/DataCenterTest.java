package org.carlspring.strongbox.storage;

import org.carlspring.strongbox.storage.repository.Repository;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author mtodorov
 */
public class DataCenterTest
{

    public static final String TEST_CLASSES_DIR = new File("target/test-classes").getAbsolutePath();
    public static final String TEST_REPOSITORIES_DIR = TEST_CLASSES_DIR + "/repositories";
    public static final String TEST_STORAGES_DIR = TEST_CLASSES_DIR + "/storages";


    @Test
    public void testAddAnonymous()
    {
        Storage storage1 = new Storage();
        storage1.setBasedir("/foo/bar/anonymous");
        storage1.addRepository(new Repository("repository0"));

        Storage storage2 = new Storage();
        storage2.setBasedir("/foo/bar/storage");

        DataCenter dataCenter = new DataCenter();
        dataCenter.addStorage(null, storage1);
        dataCenter.addStorage(storage2.getBasedir(), storage2);

        assertEquals("Incorrect number of storages!", dataCenter.getStorages().size(), 2);
        assertNotNull("Failed to add named storage!", dataCenter.getStorages().get("/foo/bar/storage"));
        assertNotNull("Failed to add anonymous storage!", dataCenter.getStorages().get("anonymous-storage-1"));

        final List<Storage> storagesContainingRepository0 = dataCenter.getStoragesContainingRepository("repository0");
        assertNotNull("Failed to resolve anonymous storage!", storagesContainingRepository0);
        assertEquals("Failed to resolve anonymous storage!", 1, storagesContainingRepository0.size());
    }

    @Test
    public void testAddRepositoriesAsAnonymousStorages()
            throws IOException
    {
        Set<Repository> repositories = new LinkedHashSet<Repository>();
        repositories.add(new Repository(new File(TEST_REPOSITORIES_DIR, "repository0").getCanonicalFile().getName()));
        repositories.add(new Repository(new File(TEST_REPOSITORIES_DIR, "repository1").getCanonicalFile().getName()));
        repositories.add(new Repository(new File(TEST_REPOSITORIES_DIR, "repository2").getCanonicalFile().getName()));

        DataCenter dataCenter = new DataCenter();
        dataCenter.addRepositories(TEST_REPOSITORIES_DIR, repositories);

        assertEquals("Failed to add anonymous storages!", 3, dataCenter.getStorages().keySet().size());
    }

    @Test
    public void testAddStorageAndLocateRepositories()
            throws IOException
    {
        Storage storage0 = new Storage(new File(TEST_STORAGES_DIR, "storage0").getCanonicalPath());
        Storage storage1 = new Storage(new File(TEST_STORAGES_DIR, "storage1").getCanonicalPath());
        Storage storage2 = new Storage(new File(TEST_STORAGES_DIR, "storage2").getCanonicalPath());

        Collection<Storage> storages = new ArrayList<Storage>();
        storages.add(storage0);
        storages.add(storage1);
        storages.add(storage2);

        DataCenter dataCenter = new DataCenter();
        dataCenter.addStorages(storages);

        dataCenter.initializeStorages();

        Set<Repository> repositories = new LinkedHashSet<Repository>();
        repositories.add(new Repository(new File(TEST_REPOSITORIES_DIR, "repository0").getCanonicalFile().getName()));
        repositories.add(new Repository(new File(TEST_REPOSITORIES_DIR, "repository1").getCanonicalFile().getName(), true));
        repositories.add(new Repository(new File(TEST_REPOSITORIES_DIR, "repository2").getCanonicalFile().getName()));

        dataCenter.addRepositories(TEST_REPOSITORIES_DIR, repositories);

        assertEquals("Incorrect number of storages!", 6, dataCenter.getStorages().size());

        for (Map.Entry entry : dataCenter.getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            String storageKey = (String) entry.getKey();
            storageKey = storageKey.indexOf(File.separatorChar) != -1 ?
                         storageKey.substring(storageKey.lastIndexOf(File.separatorChar) + 1, storageKey.length()) :
                         storageKey;
            System.out.println(storageKey +": " + storage.getBasedir());

            for (String key : storage.getRepositories().keySet())
            {
                Repository repository = storage.getRepositories().get(key);
                System.out.println(" --> " + repository + (repository.isSecured() ? " : secured" : " : anonymous"));
            }
        }

        final List<Storage> storagesContainingIntReleases = dataCenter.getStoragesContainingRepository("int-releases");
        assertNotNull("Failed to resolve anonymous storage!", storagesContainingIntReleases);
        assertEquals("Incorrect number of storages containing repository 'int-releases' was returned!",
                     2,
                     storagesContainingIntReleases.size());
    }

}
