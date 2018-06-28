package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;

import java.util.Optional;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
public class WhenRepositoryIsNotAliveDontCleanExpiredArtifactsTestIT
        extends BaseLocalStorageProxyRepositoryExpiredArtifactsCleanerTest
{

    @Test
    public void expiredArtifactsCleanerShouldNotCleanupDatabaseAndStorageWhenRepositoryIsNotAlive()
            throws Exception
    {
        ArtifactEntry artifactEntry = downloadAndSaveArtifactEntry();

        // make repositories are not alive
        Mockito.when(remoteRepositoryAlivenessCacheManager.isAlive(any(RemoteRepository.class))).thenReturn(false);

        localStorageProxyRepositoryExpiredArtifactsCleaner.cleanup(5, artifactEntry.getSizeInBytes() - 1);
        Optional<ArtifactEntry> artifactEntryOptional = Optional.ofNullable(artifactEntryService.findOneArtifact(storageId,
                                                                                                                 repositoryId,
                                                                                                                 path));

        // it's still there
        assertThat(artifactEntryOptional, CoreMatchers.not(CoreMatchers.equalTo(Optional.empty())));
    }

}
