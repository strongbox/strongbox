package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.carlspring.strongbox.repository.IndexedMavenRepositoryManagementStrategy.shouldDownloadAllRemoteRepositoryIndexes;
import static org.carlspring.strongbox.repository.IndexedMavenRepositoryManagementStrategy.shouldDownloadRepositoryIndex;
import static org.carlspring.strongbox.testing.TestCaseWithRepository.STORAGE0;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author carlspring
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class MavenRepositoryManagementStrategyTest
{

    public static final String REPOSITORY_RELEASES = "mrmst-releases";


    @AfterEach
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

        assertTrue(shouldDownloadAllRemoteRepositoryIndexes() || shouldDownloadRepositoryIndex(STORAGE0, REPOSITORY_RELEASES),
                   "Did not handle download repository index decision properly!");
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

        assertTrue(shouldDownloadAllRemoteRepositoryIndexes() || shouldDownloadRepositoryIndex(STORAGE0, REPOSITORY_RELEASES),
                   "Did not handle download repository index decision properly!");
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

        assertTrue(shouldDownloadAllRemoteRepositoryIndexes() || shouldDownloadRepositoryIndex(STORAGE0, REPOSITORY_RELEASES),
                   "Did not handle download repository index decision properly!");
    }

    @Test
    public void testShouldNotDownloadRepositoryIndexCaseWithStorageWildcard()
    {
        System.setProperty("strongbox.download.indexes", "false");
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

        assertFalse(shouldDownloadAllRemoteRepositoryIndexes() || shouldDownloadRepositoryIndex(STORAGE0, REPOSITORY_RELEASES),
                    "Did not handle download repository index decision properly!");
    }

    @Test
    public void testShouldNotDownloadRepositoryIndexCaseWithExplicitNegation()
    {
        System.setProperty("strongbox.download.indexes", "false");
        System.setProperty("strongbox.download.indexes." + STORAGE0 + "." + REPOSITORY_RELEASES, "false");

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

        assertFalse(shouldDownloadAllRemoteRepositoryIndexes() || shouldDownloadRepositoryIndex(STORAGE0, REPOSITORY_RELEASES),
                    "Did not handle download repository index decision properly!");
    }

    @Test
    public void testShouldNotDownloadRepositoryIndexCaseWithStorageWildcardPositiveAndExplicitNegation()
    {
        System.setProperty("strongbox.download.indexes", "false");
        System.setProperty("strongbox.download.indexes." + STORAGE0 + "." + REPOSITORY_RELEASES, "false");
        System.setProperty("strongbox.download.indexes." + STORAGE0 + ".*", "true");

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

        assertFalse(shouldDownloadAllRemoteRepositoryIndexes() || shouldDownloadRepositoryIndex(STORAGE0, REPOSITORY_RELEASES),
                    "Did not handle download repository index decision properly!");
    }

}
