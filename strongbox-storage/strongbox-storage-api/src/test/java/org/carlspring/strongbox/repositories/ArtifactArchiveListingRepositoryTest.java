package org.carlspring.strongbox.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import org.carlspring.strongbox.artifact.coordinates.RawArtifactCoordinates;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.domain.ArtifactArchiveListing;
import org.carlspring.strongbox.domain.ArtifactArchiveListingEntity;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.gremlin.tx.TransactionContext;

import javax.inject.Inject;
import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

/**
 * @author ankit.tomar
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = RepositoriesTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class ArtifactArchiveListingRepositoryTest
{
    @Inject
    private ArtifactArchiveListingRepository artifactArchiveListingRepository;

    @Inject
    @TransactionContext
    private Graph graph;

    @Test
    @Transactional
    public void crudShouldWork()
    {
        String storageId = "test-storage";
        String repositoryId = "test-repository";
        RawArtifactCoordinates artifactCoordinates = new RawArtifactCoordinates();
        artifactCoordinates.setId("path/to/resource/listings.jar");
        ArtifactEntity artifactEntity = new ArtifactEntity(storageId, repositoryId, artifactCoordinates);
        artifactEntity.addChecksums(new HashSet<>(Arrays.asList("{md5}3111519d5b4efd31565831f735ab0d2f",
                                                                "{sha-1}ba79baeb 9f10896a 46ae7471 5271b7f5 86e74640")));

        LocalDateTime now = LocalDateTime.now();
        artifactEntity.setCreated(now.minusDays(10));
        artifactEntity.setLastUsed(now.minusDays(5));
        artifactEntity.setLastUpdated(now);

        ArtifactArchiveListing artifactArchiveListing = new ArtifactArchiveListingEntity(storageId, repositoryId, "file1.txt");
        artifactArchiveListing = artifactArchiveListingRepository.save(artifactArchiveListing);
        assertThat(artifactArchiveListing.getUuid()).isNotNull();
        assertThat(artifactArchiveListing.getFileName()).isEqualTo("file1.txt");

        artifactArchiveListing = new ArtifactArchiveListingEntity(storageId, repositoryId, "readme.md");
        artifactArchiveListing = artifactArchiveListingRepository.save(artifactArchiveListing);
        assertThat(artifactArchiveListing.getUuid()).isNotNull();
        assertThat(artifactArchiveListing.getFileName()).isEqualTo("readme.md");

        artifactArchiveListing = new ArtifactArchiveListingEntity(storageId, repositoryId, "icon.svg");
        artifactArchiveListing = artifactArchiveListingRepository.save(artifactArchiveListing);
        assertThat(artifactArchiveListing.getUuid()).isNotNull();
        assertThat(artifactArchiveListing.getFileName()).isEqualTo("icon.svg");

    }
}
