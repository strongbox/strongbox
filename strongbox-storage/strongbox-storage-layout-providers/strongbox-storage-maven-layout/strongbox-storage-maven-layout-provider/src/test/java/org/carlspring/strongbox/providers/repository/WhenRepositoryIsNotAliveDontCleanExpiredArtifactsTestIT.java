package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;

import java.util.Optional;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.mockito.Matchers.any;

/**
 * @author Przemyslaw Fusik
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(CONCURRENT)
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
