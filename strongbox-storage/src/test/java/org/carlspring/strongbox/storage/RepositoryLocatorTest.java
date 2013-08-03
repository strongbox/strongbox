package org.carlspring.strongbox.storage;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author mtodorov
 */
public class RepositoryLocatorTest
{

    public static final String TEST_RESOURCES_DIR = "target/test-resources";

    public static final String STORAGE_DIR = TEST_RESOURCES_DIR + "/storage1";


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp()
            throws Exception
    {
        new File(STORAGE_DIR, "repository1").mkdirs();
        new File(STORAGE_DIR, "repository2").mkdirs();
        new File(STORAGE_DIR, "repository3").mkdirs();

        new File(STORAGE_DIR, ".repository").mkdirs();
        new File(STORAGE_DIR, "repository.").mkdirs();
    }

    @Test
    public void testLocation()
            throws IOException
    {
        Storage storage = new Storage();
        storage.setBasedir(STORAGE_DIR);

        RepositoryLocator locator = new RepositoryLocator(storage);
        locator.initializeRepositories();

        assertEquals("Failed to locate the proper number of repositories",
                     3,
                     locator.getStorage().getRepositories().size());

        for (String repositoryName : locator.getStorage().getRepositories().keySet())
        {
            System.out.println(repositoryName);
        }
    }

}
