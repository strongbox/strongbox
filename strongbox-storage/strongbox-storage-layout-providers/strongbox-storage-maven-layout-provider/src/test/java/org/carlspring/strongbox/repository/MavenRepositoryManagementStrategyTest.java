package org.carlspring.strongbox.repository;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import static org.carlspring.strongbox.repository.MavenRepositoryManagementStrategy.shouldDownloadAllRemoteRepositoryIndexes;
import static org.carlspring.strongbox.repository.MavenRepositoryManagementStrategy.shouldDownloadRepositoryIndex;
import static org.carlspring.strongbox.testing.TestCaseWithRepository.STORAGE0;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class MavenRepositoryManagementStrategyTest
{

    public static final String REPOSITORY_RELEASES = "mrmst-releases";


    @After
    public void tearDown()
            throws Exception
    {
        System.getProperties().remove("strongbox.download.indexes");
        System.getProperties().remove("strongbox.download.indexes." + STORAGE0 + "." + REPOSITORY_RELEASES);
        System.getProperties().remove("strongbox.download.indexes." + STORAGE0 + ".*");
    }

    @Test
    public void testShouldDownloadRepositoryIndexCaseWithNoProperties()
    {
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("strongbox.download.indexes = " + System.getProperty("strongbox.download.indexes"));
        System.out.println("strongbox.download.indexes." + STORAGE0 + "." + REPOSITORY_RELEASES + " = " +
                           System.getProperty("strongbox.download.indexes." + STORAGE0 + "." + REPOSITORY_RELEASES));
        System.out.println("strongbox.download.indexes." + STORAGE0 + ".* = " +
                           System.getProperty("strongbox.download.indexes." + STORAGE0 + ".*"));

        System.out.println("shouldDownloadAllRemoteRepositoryIndexes ? " + shouldDownloadAllRemoteRepositoryIndexes());
        System.out.println("shouldDownloadRepositoryIndex(storage0, releases) ? " +
                           shouldDownloadRepositoryIndex(STORAGE0, REPOSITORY_RELEASES));
        System.out.println("----------------------------------------------------------------------------------");

        assertTrue("Did not handle download repository index decision properly!",
                   shouldDownloadRepositoryIndex(STORAGE0, REPOSITORY_RELEASES));
    }

    @Test
    public void testShouldDownloadRepositoryIndexCaseWithStorageAndRepository()
    {
        System.setProperty("strongbox.download.indexes." + STORAGE0 + "." + REPOSITORY_RELEASES, "false");

        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("strongbox.download.indexes = " + System.getProperty("strongbox.download.indexes"));
        System.out.println("strongbox.download.indexes." + STORAGE0 + "." + REPOSITORY_RELEASES + " = " +
                           System.getProperty("strongbox.download.indexes." + STORAGE0 + "." + REPOSITORY_RELEASES));
        System.out.println("strongbox.download.indexes." + STORAGE0 + ".* = " +
                           System.getProperty("strongbox.download.indexes." + STORAGE0 + ".*"));

        System.out.println("shouldDownloadAllRemoteRepositoryIndexes ? " + shouldDownloadAllRemoteRepositoryIndexes());
        System.out.println("shouldDownloadRepositoryIndex(storage0, releases) ? " +
                           shouldDownloadRepositoryIndex(STORAGE0, REPOSITORY_RELEASES));
        System.out.println("----------------------------------------------------------------------------------");

        assertFalse("Did not handle download repository index decision properly!",
                    shouldDownloadRepositoryIndex(STORAGE0, REPOSITORY_RELEASES));
    }

    @Test
    public void testShouldDownloadRepositoryIndexCaseWithStorageWildcard()
    {
        System.setProperty("strongbox.download.indexes", "false");
        System.setProperty("strongbox.download.indexes." + STORAGE0 + "." + REPOSITORY_RELEASES, "true");
        System.setProperty("strongbox.download.indexes." + STORAGE0 + ".*", "false");

        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("strongbox.download.indexes = " + System.getProperty("strongbox.download.indexes"));
        System.out.println("strongbox.download.indexes." + STORAGE0 + "." + REPOSITORY_RELEASES + " = " +
                           System.getProperty("strongbox.download.indexes." + STORAGE0 + "." + REPOSITORY_RELEASES));
        System.out.println("strongbox.download.indexes." + STORAGE0 + ".* = " +
                           System.getProperty("strongbox.download.indexes." + STORAGE0 + ".*"));

        System.out.println("shouldDownloadAllRemoteRepositoryIndexes ? " +
                           shouldDownloadAllRemoteRepositoryIndexes());
        System.out.println("shouldDownloadRepositoryIndex(storage0, releases) ? " +
                           shouldDownloadRepositoryIndex(STORAGE0, REPOSITORY_RELEASES));
        System.out.println("----------------------------------------------------------------------------------");

        assertTrue("Did not handle download repository index decision properly!",
                   shouldDownloadRepositoryIndex(STORAGE0, REPOSITORY_RELEASES));
    }

}
