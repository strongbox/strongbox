package org.carlspring.strongbox.services.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.RawArtifactCoordinates;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.gremlin.tx.TransactionContext;
import org.carlspring.strongbox.repositories.ArtifactRepository;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = StorageApiTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class ArtifactTagServiceTest
{
    @Inject
    ArtifactTagService artifactTagService;
    @Inject
    ArtifactRepository artifactRepository;
    @Inject
    @TransactionContext
    Graph graph;

    @Test
    @Transactional
    public void tagsShouldWork()
    {
        GraphTraversalSource g = graph.traversal();
        String storageId = "storage0";
        String repositoryId = "repository-atst-tsw";
        String path = "path/to/resource/atst-tsw-10.jar";

        ArtifactTag releaseTag = artifactTagService.findOneOrCreate("release");
        ArtifactTag latestTag = artifactTagService.findOneOrCreate("latest");
        ArtifactTag ltsTag = artifactTagService.findOneOrCreate("LTS");
        assertThat(g.V()
                    .hasLabel(Vertices.ARTIFACT_TAG)
                    .has("uuid", P.within("release", "latest", "LTS"))
                    .count()
                    .next()).isEqualTo(3);

        RawArtifactCoordinates artifactCoordinates = new RawArtifactCoordinates();
        artifactCoordinates.setId(path);

        ArtifactEntity artifactEntity = new ArtifactEntity(storageId, repositoryId, artifactCoordinates);
        artifactEntity.setTagSet(new HashSet<>(Arrays.asList(new ArtifactTag[] { releaseTag, latestTag, ltsTag })));

        // Create Artifact with tags
        artifactEntity = artifactRepository.save(artifactEntity);
        assertThat(artifactEntity.getUuid()).isNotNull();
        assertThat(artifactEntity.getTagSet()).containsOnly(releaseTag, latestTag, ltsTag);

        // Update Artifact tags
        artifactEntity.setTagSet(new HashSet<>(Arrays.asList(new ArtifactTag[] { releaseTag, latestTag })));
        artifactEntity = artifactRepository.save(artifactEntity);
        assertThat(artifactEntity.getUuid()).isNotNull();
        assertThat(artifactEntity.getTagSet()).containsOnly(releaseTag, latestTag);

        // Remove Artifact tags
        artifactEntity.setTagSet(new HashSet<>());
        artifactEntity = artifactRepository.save(artifactEntity);
        assertThat(artifactEntity.getUuid()).isNotNull();
        assertThat(artifactEntity.getTagSet()).isEmpty();
    }

}
