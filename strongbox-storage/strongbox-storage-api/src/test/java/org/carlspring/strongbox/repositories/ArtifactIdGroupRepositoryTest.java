package org.carlspring.strongbox.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.RawArtifactCoordinates;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Properties;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.db.schema.migration.ChangelogStorage;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.domain.ArtifactIdGroup;
import org.carlspring.strongbox.domain.ArtifactIdGroupEntity;
import org.carlspring.strongbox.domain.ArtifactTagEntity;
import org.carlspring.strongbox.gremlin.tx.TransactionContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = RepositoriesTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class ArtifactIdGroupRepositoryTest
{

    @Inject
    private ArtifactIdGroupRepository artifactIdGroupRepository;
    @Inject
    private ArtifactTagRepository artifactTagRepository;
    @Inject
    private ArtifactRepository artifactRepository;
    @Inject
    @TransactionContext
    private Graph graph;

    @Test
    @Transactional
    public void crudShouldWork()
    {
        GraphTraversalSource g = graph.traversal();
        String storageId = "storage0";
        String repositoryId = "repository-aigrt-csw";
        String pathTemplate = "path/to/resource/aigrt-csw-%s.jar";

        ArtifactEntity artifactEntityOne = createArtifact(storageId, repositoryId, pathTemplate, "10");
        ArtifactEntity artifactEntityTwo = createArtifact(storageId, repositoryId, pathTemplate, "20");
        ArtifactEntity artifactEntityThree = createArtifact(storageId, repositoryId, pathTemplate, "30");

        // Create
        ArtifactIdGroupEntity artifactIdGroupEntity = new ArtifactIdGroupEntity(storageId, repositoryId, "path/to/resource/aigrt-csw");
        artifactIdGroupEntity.addArtifact(artifactEntityOne);
        artifactIdGroupEntity.addArtifact(artifactEntityTwo);
        artifactIdGroupEntity.addArtifact(artifactEntityThree);
        artifactIdGroupEntity = artifactIdGroupRepository.save(artifactIdGroupEntity);
        assertThat(artifactIdGroupEntity.getUuid()).isNotNull();
        assertThat(artifactIdGroupEntity.getStorageId()).isEqualTo(storageId);
        assertThat(artifactIdGroupEntity.getRepositoryId()).isEqualTo(repositoryId);
        assertThat(artifactIdGroupEntity.getArtifacts()).containsOnly(artifactEntityOne,
                                                                      artifactEntityTwo,
                                                                      artifactEntityThree);

        assertThat(g.E()
                    .hasLabel(Edges.ARTIFACT_GROUP_HAS_ARTIFACTS)
                    .bothV()
                    .properties("uuid")
                    .map(p -> p.get().value())
                    .toList()).contains(artifactEntityOne.getUuid(),
                                        artifactEntityTwo.getUuid(),
                                        artifactEntityThree.getUuid(),
                                        artifactIdGroupEntity.getUuid())
                              .hasSize(6);

        // Update
        artifactRepository.delete(artifactEntityOne);
        artifactEntityOne.setNativeId(null);
        artifactRepository.delete(artifactEntityTwo);
        artifactEntityTwo.setNativeId(null);

        artifactIdGroupEntity = artifactIdGroupRepository.findById(artifactIdGroupEntity.getUuid())
                                                         .map(ArtifactIdGroupEntity.class::cast)
                                                         .get();
        assertThat(artifactIdGroupEntity.getArtifacts()).containsOnly(artifactEntityThree);

        // ArtifactTag relation
        ArtifactTag latestVersionTag = artifactTagRepository.save(new ArtifactTagEntity(ArtifactTag.LAST_VERSION));
        artifactEntityOne.getTagSet().add(latestVersionTag);
        //artifactRepository.save(artifactEntityOne);

        artifactIdGroupEntity.addArtifact(artifactEntityOne);
        artifactIdGroupEntity = artifactIdGroupRepository.save(artifactIdGroupEntity);
        assertThat(artifactIdGroupEntity.getUuid()).isNotNull();
        assertThat(artifactIdGroupEntity.getStorageId()).isEqualTo(storageId);
        assertThat(artifactIdGroupEntity.getRepositoryId()).isEqualTo(repositoryId);
        assertThat(artifactIdGroupEntity.getArtifacts()).containsOnly(artifactEntityOne, artifactEntityThree);
        assertThat(artifactIdGroupEntity.getArtifacts()).filteredOnAssertions(a -> assertThat(a.getTagSet()).contains(latestVersionTag))
                                                        .hasSize(1);

        assertThat(g.E()
                    .hasLabel(Edges.ARTIFACT_GROUP_HAS_ARTIFACTS)
                    .sideEffect(t -> System.out.println(t.get().property(Properties.TAG_NAME).orElse("no-tag")))
                    .toList()).hasSize(2);

        artifactRepository.delete(artifactEntityOne);
        artifactEntityOne.setNativeId(null);
        artifactRepository.delete(artifactEntityThree);
        artifactEntityThree.setNativeId(null);
        artifactIdGroupEntity = artifactIdGroupRepository.findById(artifactIdGroupEntity.getUuid())
                                                         .map(ArtifactIdGroupEntity.class::cast)
                                                         .get();
        assertThat(artifactIdGroupEntity.getArtifacts()).isEmpty();
        assertThat(g.E()
                    .hasLabel(Edges.ARTIFACT_GROUP_HAS_ARTIFACTS)
                    .hasNext()).isFalse();

        // Delete
        artifactIdGroupRepository.delete(artifactIdGroupEntity);
        assertThat(artifactIdGroupRepository.findById(artifactIdGroupEntity.getUuid())).isEmpty();

        assertThat(g.V()
                    .not(__.hasLabel(ChangelogStorage.VERTEX_SCHEMA_VERSION))
                    .label()
                    .toSet()).hasSize(3)
                             .containsOnly(Vertices.RAW_ARTIFACT_COORDINATES,
                                           Vertices.GENERIC_ARTIFACT_COORDINATES,
                                           Vertices.ARTIFACT_TAG);
        assertThat(g.E()
                    .not(__.hasLabel(ChangelogStorage.EDGE_CHANGESET))
                    .count()
                    .next()).isEqualTo(3L);
    }

    private ArtifactEntity createArtifact(String storageId,
                                          String repositoryId,
                                          String pathTemplate,
                                          String version)
    {
        RawArtifactCoordinates artifactCoordinates = new RawArtifactCoordinates();
        artifactCoordinates.setId(String.format(pathTemplate, version));
        ArtifactEntity artifactEntity = new ArtifactEntity(storageId, repositoryId, artifactCoordinates);

        return artifactEntity;
    }

    @Test
    @Transactional
    public void findOneShouldWork()
    {
        GraphTraversalSource g = graph.traversal();
        String storageId = "storage0";
        String repositoryId = "repository-aigrt-fosw";
        String pathTemplate = "path/to/resource/aigrt-fosw-%s.jar";

        // First group
        ArtifactEntity artifactEntityOne = createArtifact(storageId, repositoryId, pathTemplate, "10");
        ArtifactEntity artifactEntityTwo = createArtifact(storageId, repositoryId, pathTemplate, "20");
        ArtifactEntity artifactEntityThree = createArtifact(storageId, repositoryId, pathTemplate, "30");

        ArtifactIdGroupEntity artifactIdGroupEntity = new ArtifactIdGroupEntity(storageId, repositoryId,
                "path/to/resource/aigrt-fosw");
        assertThat(artifactIdGroupRepository.findAllArtifactsInGroup(storageId,
                                                                     repositoryId,
                                                                     "path/to/resource/aigrt-fosw")).isEmpty();

        artifactIdGroupEntity = artifactIdGroupRepository.save(artifactIdGroupEntity);
        artifactIdGroupEntity.addArtifact(artifactEntityOne);
        artifactIdGroupEntity.addArtifact(artifactEntityTwo);
        artifactIdGroupEntity.addArtifact(artifactEntityThree);
        artifactIdGroupRepository.save(artifactIdGroupEntity);

        // Second group
        pathTemplate = "path/to/resource/aigrt-fosw-another-%s.jar";
        ArtifactEntity artifactEntityAnotherOne = createArtifact(storageId, repositoryId, pathTemplate, "10");
        ArtifactTag latestVersionTag = artifactTagRepository.save(new ArtifactTagEntity(ArtifactTag.LAST_VERSION));
        artifactEntityAnotherOne.getTagSet().add(latestVersionTag);
        ArtifactEntity artifactEntityAnotherTwo = createArtifact(storageId, repositoryId, pathTemplate, "20");

        ArtifactIdGroupEntity artifactIdGroupEntityAnother = new ArtifactIdGroupEntity(storageId, repositoryId,
                "path/to/resource/aigrt-fosw-another");
        artifactIdGroupEntityAnother.addArtifact(artifactEntityAnotherOne);
        artifactIdGroupEntityAnother.addArtifact(artifactEntityAnotherTwo);
        artifactIdGroupRepository.save(artifactIdGroupEntityAnother);

        //Should find first group with all artifacts
        Optional<ArtifactIdGroup> artifactIdGroupOptional = artifactIdGroupRepository.findAllArtifactsInGroup(storageId,
                                                                                                              repositoryId,
                                                                                                              "path/to/resource/aigrt-fosw");
        assertThat(artifactIdGroupOptional).isNotEmpty();
        assertThat(artifactIdGroupOptional.get().getUuid()).isNotNull();
        assertThat(artifactIdGroupOptional.get().getStorageId()).isEqualTo(storageId);
        assertThat(artifactIdGroupOptional.get().getRepositoryId()).isEqualTo(repositoryId);
        assertThat(artifactIdGroupOptional.get().getArtifacts()).containsOnly(artifactEntityOne,
                                                                              artifactEntityTwo,
                                                                              artifactEntityThree);

        artifactRepository.delete(artifactEntityOne);
        artifactEntityOne.setNativeId(null);
        artifactRepository.delete(artifactEntityTwo);
        artifactEntityTwo.setNativeId(null);
        artifactRepository.delete(artifactEntityThree);
        artifactEntityThree.setNativeId(null);

        artifactIdGroupEntity = artifactIdGroupRepository.findById(artifactIdGroupEntity.getUuid())
                                                         .map(ArtifactIdGroupEntity.class::cast)
                                                         .get();
        assertThat(artifactIdGroupEntity.getArtifacts()).isEmpty();
        assertThat(g.E()
                    .hasLabel(Edges.ARTIFACT_GROUP_HAS_ARTIFACTS)
                    .count()
                    .next()).isEqualTo(2);

        artifactIdGroupOptional = artifactIdGroupRepository.findAllArtifactsInGroup(storageId,
                                                                                    repositoryId,
                                                                                    "path/to/resource/aigrt-fosw");
        assertThat(artifactIdGroupOptional).isNotEmpty();
        assertThat(artifactIdGroupOptional.get().getUuid()).isNotNull();
        assertThat(artifactIdGroupOptional.get().getArtifacts()).isEmpty();

        // Search by tag
        artifactIdGroupOptional = artifactIdGroupRepository.findArtifactsGroupWithTag(storageId,
                                                                                    repositoryId,
                                                                                    "path/to/resource/aigrt-fosw-another",
                                                                                    Optional.of(latestVersionTag));
        assertThat(artifactIdGroupOptional).isNotEmpty();
        assertThat(artifactIdGroupOptional.get().getUuid()).isNotNull();
        assertThat(artifactIdGroupOptional.get().getStorageId()).isEqualTo(storageId);
        assertThat(artifactIdGroupOptional.get().getRepositoryId()).isEqualTo(repositoryId);
        assertThat(artifactIdGroupOptional.get().getArtifacts()).containsOnly(artifactEntityAnotherOne);
    }

    @Test
    @Transactional
    public void findArtifactsShouldWork()
    {
        GraphTraversalSource g = graph.traversal();
        String storageId = "storage0";
        String repositoryId = "repository-aigrt-fasw";
        String pathTemplate = "path/to/resource/aigrt-fasw-%s.jar";

        ArtifactEntity artifactEntityOne = createArtifact(storageId, repositoryId, pathTemplate, "10");
        ArtifactEntity artifactEntityTwo = createArtifact(storageId, repositoryId, pathTemplate, "20");
        ArtifactEntity artifactEntityThree = createArtifact(storageId, repositoryId, pathTemplate, "30");

        ArtifactIdGroupEntity artifactIdGroupEntity = new ArtifactIdGroupEntity(storageId, repositoryId,
                "path/to/resource/aigrt-fasw");
        assertThat(artifactIdGroupRepository.findAllArtifactsInGroup(storageId,
                                                                     repositoryId,
                                                                     "path/to/resource/aigrt-fasw")).isEmpty();

        artifactIdGroupEntity = artifactIdGroupRepository.save(artifactIdGroupEntity);
        artifactIdGroupEntity.addArtifact(artifactEntityOne);
        artifactIdGroupEntity.addArtifact(artifactEntityTwo);
        artifactIdGroupEntity.addArtifact(artifactEntityThree);
        artifactIdGroupRepository.save(artifactIdGroupEntity);

        List<Artifact> artifacts = artifactIdGroupRepository.findArtifacts(storageId,
                                                                           repositoryId,
                                                                           "path/to/resource/aigrt-fasw",
                                                                           Collections.singletonList(String.format(pathTemplate,
                                                                                                                   "10")),
                                                                           0L,
                                                                           Integer.MAX_VALUE);
        assertThat(artifacts.size()).isEqualTo(1);
        assertThat(artifacts.stream().map(a -> a.getArtifactCoordinates().getId())).contains(String.format(pathTemplate, "10"));
    }
}
