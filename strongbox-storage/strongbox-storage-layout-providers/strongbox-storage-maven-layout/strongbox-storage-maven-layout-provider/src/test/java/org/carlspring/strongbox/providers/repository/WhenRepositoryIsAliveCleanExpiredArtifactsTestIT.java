package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;

import java.util.Optional;

import org.codehaus.plexus.util.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.mockito.ArgumentMatchers.argThat;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(CONCURRENT)
public class WhenRepositoryIsAliveCleanExpiredArtifactsTestIT
        extends BaseLocalStorageProxyRepositoryExpiredArtifactsCleanerTest
{

    private static final String REPOSITORY_ID = "maven-central-alive";

    private static final String REMOTE_URL = "https://repo1.maven.org/maven2/";

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void expiredArtifactsCleanerShouldCleanupDatabaseAndStorage(@Remote(url = REMOTE_URL)
                                                                       @MavenRepository(storageId = STORAGE_ID,
                                                                                        repositoryId = REPOSITORY_ID,
                                                                                        setup = MavenIndexedRepositorySetup.class)
                                                                       Repository proxyRepository)
            throws Exception
    {

        Mockito.when(getRemoteRepositoryAlivenessMock().isAlive(
                argThat(argument -> argument != null && REMOTE_URL.equals(argument.getUrl()))))
               .thenReturn(true);

        ArtifactEntry artifactEntry = downloadAndSaveArtifactEntry();

        localStorageProxyRepositoryExpiredArtifactsCleaner.cleanup(5, artifactEntry.getSizeInBytes() - 1);

        Optional<ArtifactEntry> artifactEntryOptional = Optional.ofNullable(
                artifactEntryService.findOneArtifact(proxyRepository.getStorage().getId(),
                                                     proxyRepository.getId(),
                                                     getPath()));
        assertThat(artifactEntryOptional).isEqualTo(Optional.empty());

        final Storage storage = getConfiguration().getStorage(artifactEntry.getStorageId());
        final Repository repository = storage.getRepository(artifactEntry.getRepositoryId());

        assertThat(RepositoryFiles.artifactExists(repositoryPathResolver.resolve(repository, getPath()))).isFalse();
        assertThat(RepositoryFiles.artifactExists(
                repositoryPathResolver.resolve(
                        repository,
                        StringUtils.replace(getPath(),"1.3/maven-commons-1.3.jar","maven-metadata.xml"))
                   )
        ).isTrue();
    }

    @Override
    protected String getRepositoryId()
    {
        return REPOSITORY_ID;
    }

    @Override
    protected String getPath()
    {
        return "org/carlspring/maven/maven-commons/1.3/maven-commons-1.3.jar";
    }

    @Override
    protected String getVersion()
    {
        return "1.3";
    }
}
