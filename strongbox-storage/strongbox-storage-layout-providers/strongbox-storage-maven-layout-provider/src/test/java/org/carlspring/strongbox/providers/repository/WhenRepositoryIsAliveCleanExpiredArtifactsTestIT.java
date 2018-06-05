package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import java.util.Optional;

import org.codehaus.plexus.util.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
public class WhenRepositoryIsAliveCleanExpiredArtifactsTestIT
        extends BaseLocalStorageProxyRepositoryExpiredArtifactsCleanerTest
{

    @Test
    public void expiredArtifactsCleanerShouldCleanupDatabaseAndStorage()
            throws Exception
    {
        ArtifactEntry artifactEntry = downloadAndSaveArtifactEntry();

        localStorageProxyRepositoryExpiredArtifactsCleaner.cleanup(5, artifactEntry.getSizeInBytes() - 1);
        Optional<ArtifactEntry> artifactEntryOptional = artifactEntryService.findOneArtifact(storageId, repositoryId,
                                                                                             path);
        assertThat(artifactEntryOptional, CoreMatchers.equalTo(Optional.empty()));

        final Storage storage = getConfiguration().getStorage(artifactEntry.getStorageId());
        final Repository repository = storage.getRepository(artifactEntry.getRepositoryId());
        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        assertFalse(layoutProvider.containsPath(repositoryPathResolver.resolve(repository, path)));
        assertTrue(layoutProvider.containsPath(repositoryPathResolver.resolve(repository, StringUtils.replace(path,
                                                                                                              "1.6/properties-injector-1.6.jar",
                                                                                                              "maven-metadata.xml"))));
    }

}
