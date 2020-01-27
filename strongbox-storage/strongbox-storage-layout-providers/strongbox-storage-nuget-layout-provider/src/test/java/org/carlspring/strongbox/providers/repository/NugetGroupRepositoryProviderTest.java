package org.carlspring.strongbox.providers.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.data.criteria.Paginator;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.NugetTestArtifact;
import org.carlspring.strongbox.testing.repository.NugetRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryAttributes;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author sbespalov
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = NugetLayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class NugetGroupRepositoryProviderTest
{

    private static final String REPOSITORY_RELEASES_1 = "ngrpt-releases-1";

    private static final String REPOSITORY_RELEASES_2 = "ngrpt-releases-2";
    
    private static final String REPOSITORY_RELEASES_3 = "ngrpt-releases-3";

    private static final String REPOSITORY_GROUP = "ngrpt-releases-group";

    private static final String REPOSITORY_GROUP_WITH_NESTED_GROUP_1 = "ngrpt-releases-group-with-nested-group-level-1";

    private static final String REPOSITORY_GROUP_WITH_NESTED_GROUP_2 = "ngrpt-releases-group-with-nested-group-level-2";

    @Inject
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    @Disabled
    public void testGroupSearch(@NugetRepository(repositoryId = REPOSITORY_RELEASES_1)
                                Repository repository1,
                                @NugetTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
                                                   id = "ngrpt.search.package",
                                                   versions = { "1.0.0",
                                                                "1.0.1",
                                                                "1.0.2",
                                                                "1.0.3",
                                                                "1.0.4",
                                                                "1.0.5",
                                                                "1.0.6",
                                                                "1.0.7",
                                                                "1.0.8"})
                                Path artifactPath1,
                                @NugetRepository(repositoryId = REPOSITORY_RELEASES_2)
                                Repository repository2,
                                @NugetTestArtifact(repositoryId = REPOSITORY_RELEASES_2,
                                                   id = "ngrpt.search.package",
                                                   versions = { "1.0.0",
                                                                "1.0.1",
                                                                "1.0.2",
                                                                "1.0.3",
                                                                "1.0.4",
                                                                "1.0.5",
                                                                "1.0.6",
                                                                "1.0.7",
                                                                "1.0.9",
                                                                "1.0.10",
                                                                "1.0.11"})
                                Path artifactPath2,
                                @NugetRepository(repositoryId = REPOSITORY_RELEASES_3)
                                Repository repository3,
                                @NugetTestArtifact(repositoryId = REPOSITORY_RELEASES_3,
                                                   id = "ngrpt.search.package",
                                                   versions = { "1.0.0",
                                                                "1.0.1",
                                                                "1.0.2",
                                                                "1.0.3",
                                                                "1.0.4",
                                                                "1.0.5",
                                                                "1.0.6",
                                                                "1.0.7"})
                                Path artifactPath3,
                                @Group(repositories = { REPOSITORY_RELEASES_1,
                                                        REPOSITORY_RELEASES_2,
                                                        REPOSITORY_RELEASES_3 })
                                @NugetRepository(repositoryId = REPOSITORY_GROUP)
                                @RepositoryAttributes(allowsRedeployment = false,
                                                      allowsDelete = false)
                                Repository repositoryGroup,
                                @Group(repositories = REPOSITORY_GROUP)
                                @NugetRepository(repositoryId = REPOSITORY_GROUP_WITH_NESTED_GROUP_1)
                                @RepositoryAttributes(allowsRedeployment = false,
                                                      allowsDelete = false)
                                Repository repositoryGroupWithNestedGroup1,
                                @Group(repositories = REPOSITORY_GROUP_WITH_NESTED_GROUP_1)
                                @NugetRepository(repositoryId = REPOSITORY_GROUP_WITH_NESTED_GROUP_2)
                                @RepositoryAttributes(allowsRedeployment = false,
                                                      allowsDelete = false)
                                Repository repositoryGroupWithNestedGroup2)
    {
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repositoryGroup.getType());

        Paginator paginator = new Paginator();
        paginator.setLimit(10);
        paginator.setSkip(10L);

        RepositorySearchRequest predicate = new RepositorySearchRequest("ngrpt.search.package", Collections.singleton("nupkg"));

        List<Path> result = repositoryProvider.search(repositoryGroup.getStorage().getId(),
                                                      repositoryGroup.getId(),
                                                      predicate,
                                                      paginator);

        assertThat(result).hasSize(2);

        paginator.setLimit(-1);
        result = repositoryProvider.search(repositoryGroup.getStorage().getId(),
                                           repositoryGroup.getId(),
                                           predicate,
                                           paginator);

        assertThat(result).hasSize(2);

        repositoryProvider = repositoryProviderRegistry.getProvider(repositoryGroupWithNestedGroup2.getType());

        paginator.setSkip(11L);
        paginator.setLimit(10);

        result = repositoryProvider.search(repositoryGroupWithNestedGroup2.getStorage().getId(),
                                           repositoryGroupWithNestedGroup2.getId(),
                                           predicate,
                                           paginator);

        assertThat(result).hasSize(1);

        paginator.setLimit(-1);
        result = repositoryProvider.search(repositoryGroupWithNestedGroup2.getStorage().getId(),
                                           repositoryGroupWithNestedGroup2.getId(),
                                           predicate,
                                           paginator);

        assertThat(result).hasSize(1);

        Long count = repositoryProvider.count(repositoryGroupWithNestedGroup2.getStorage().getId(),
                                              repositoryGroupWithNestedGroup2.getId(),
                                              predicate);
        assertThat(count).isEqualTo(Long.valueOf(12));
    }

}
