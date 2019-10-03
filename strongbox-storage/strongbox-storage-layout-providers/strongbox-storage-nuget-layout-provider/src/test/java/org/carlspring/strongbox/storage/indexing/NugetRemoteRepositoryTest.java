package org.carlspring.strongbox.storage.indexing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.nuget.NugetSearchRequest;
import org.carlspring.strongbox.repository.NugetRepositoryFeatures;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.repository.NugetRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Sergey Bespalov
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = NugetLayoutProviderTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@Execution(CONCURRENT)
public class NugetRemoteRepositoryTest
{

    private static final String REPOSITORY_PROXY = "nrrt-proxy";

    private static final String REMOTE_URL = "https://www.nuget.org/api/v2";

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private NugetRepositoryFeatures features;

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testRepositoryIndexFetching(@Remote(url = REMOTE_URL)
                                            @NugetRepository(repositoryId = REPOSITORY_PROXY)
                                            Repository repository)
            throws ArtifactTransportException, IOException
    {
        NugetSearchRequest nugetSearchRequest = new NugetSearchRequest();
        nugetSearchRequest.setFilter(String.format("Id eq '%s'", "NHibernate"));

        features.downloadRemoteFeed(repository.getStorage().getId(),
                                    repository.getId(),
                                    nugetSearchRequest);

        NugetArtifactCoordinates coordinates = new NugetArtifactCoordinates("NHibernate", "4.0.4.4000");
        ArtifactEntry artifactEntry = artifactEntryService.findOneArtifact(repository.getStorage().getId(),
                                                                           repository.getId(),
                                                                           coordinates.toPath());
        Optional<ArtifactEntry> optionalArtifactEntry = Optional.ofNullable(artifactEntry);

        assertThat(optionalArtifactEntry).isPresent();
        assertThat(((RemoteArtifactEntry) optionalArtifactEntry.get()).getIsCached()).isFalse();
    }

}
