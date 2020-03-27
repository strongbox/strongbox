package org.carlspring.strongbox.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.RawArtifactCoordinates;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.domain.ArtifactIdGroup;
import org.carlspring.strongbox.domain.ArtifactIdGroupEntity;
import org.carlspring.strongbox.domain.ArtifactTagEntity;
import org.carlspring.strongbox.domain.RemoteArtifact;
import org.carlspring.strongbox.domain.RemoteArtifactEntity;
import org.carlspring.strongbox.gremlin.tx.TransactionContext;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.session.Session;
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

        RawArtifactCoordinates artifactCoordinatesOne = new RawArtifactCoordinates();
        artifactCoordinatesOne.setId(String.format(pathTemplate, "10"));
        ArtifactEntity artifactEntityOne = new ArtifactEntity(storageId, repositoryId, artifactCoordinatesOne);

        RawArtifactCoordinates artifactCoordinatesTwo = new RawArtifactCoordinates();
        artifactCoordinatesTwo.setId(String.format(pathTemplate, "20"));
        Artifact artifactEntityTwo = new RemoteArtifactEntity(storageId, repositoryId, artifactCoordinatesTwo);

        RawArtifactCoordinates artifactCoordinatesThree = new RawArtifactCoordinates();
        artifactCoordinatesThree.setId(String.format(pathTemplate, "30"));
        ArtifactEntity artifactEntityThree = new ArtifactEntity(storageId, repositoryId, artifactCoordinatesThree);

        // Create
        ArtifactIdGroupEntity artifactIdGroupEntity = new ArtifactIdGroupEntity(storageId, repositoryId, "path/to/resource/aigrt-csw");
        artifactIdGroupEntity.addArtifact(artifactEntityOne);
        artifactIdGroupEntity.addArtifact(artifactEntityTwo);
        artifactIdGroupEntity.addArtifact(artifactEntityThree);
        artifactIdGroupEntity = artifactIdGroupRepository.save(artifactIdGroupEntity);
        assertThat(artifactIdGroupEntity.getUuid()).isNotNull();
        assertThat(artifactIdGroupEntity.getStorageId()).isEqualTo(storageId);
        assertThat(artifactIdGroupEntity.getRepositoryId()).isEqualTo(repositoryId);
        assertThat(artifactIdGroupEntity.getArtifacts()).containsOnly(artifactEntityOne, artifactEntityTwo,
                                                                      artifactEntityThree);
        assertThat(artifactIdGroupEntity.getArtifacts()).filteredOnAssertions(a -> assertThat(a).isInstanceOf(RemoteArtifact.class))
                                                        .hasSize(1);

        assertThat(g.E()
                    .hasLabel(Edges.ARTIFACT_GROUP_HAS_ARTIFACTS)
                    .bothV()
                    .properties("uuid")
                    .map(p -> p.get().value())
                    .toList()).contains(artifactEntityOne.getUuid(), artifactEntityTwo.getUuid(), artifactEntityThree.getUuid(),
                                        artifactIdGroupEntity.getUuid())
                              .hasSize(6);

        // Update
        artifactIdGroupEntity.removeArtifact(artifactEntityOne);
        artifactIdGroupEntity.removeArtifact(artifactEntityTwo);
        ArtifactTag latestVersionTag = artifactTagRepository.save(new ArtifactTagEntity(ArtifactTag.LAST_VERSION));
        artifactEntityOne.getTagSet().add(latestVersionTag);
        artifactIdGroupEntity.addArtifact(artifactEntityOne);
        artifactIdGroupEntity = artifactIdGroupRepository.save(artifactIdGroupEntity);
        assertThat(artifactIdGroupEntity.getUuid()).isNotNull();
        assertThat(artifactIdGroupEntity.getStorageId()).isEqualTo(storageId);
        assertThat(artifactIdGroupEntity.getRepositoryId()).isEqualTo(repositoryId);
        assertThat(artifactIdGroupEntity.getArtifacts()).containsOnly(artifactEntityOne, artifactEntityThree);
        assertThat(artifactIdGroupEntity.getArtifacts()).filteredOnAssertions(a -> assertThat(a.getTagSet()).contains(latestVersionTag))
                                                        .hasSize(1);

        artifactIdGroupEntity.removeArtifact(artifactEntityOne);
        artifactIdGroupEntity.removeArtifact(artifactEntityThree);
        artifactIdGroupEntity = artifactIdGroupRepository.save(artifactIdGroupEntity);
        assertThat(artifactIdGroupEntity.getArtifacts()).isEmpty();
        assertThat(g.E()
                    .hasLabel(Edges.ARTIFACT_GROUP_HAS_ARTIFACTS)
                    .hasNext()).isFalse();

        // Delete
        artifactIdGroupEntity.addArtifact(artifactEntityOne);
        artifactIdGroupEntity.addArtifact(artifactEntityTwo);
        artifactIdGroupEntity.addArtifact(artifactEntityThree);
        artifactIdGroupRepository.save(artifactIdGroupEntity);
        artifactIdGroupRepository.delete(artifactIdGroupEntity);
        assertThat(artifactIdGroupRepository.findById(artifactIdGroupEntity.getUuid())).isEmpty();
        assertThat(g.V()
                    .label()
                    .toList()).hasSize(1).containsExactly(Vertices.ARTIFACT_TAG);
        assertThat(g.E()
                    .count()
                    .next()).isEqualTo(0L);
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
        RawArtifactCoordinates artifactCoordinatesOne = new RawArtifactCoordinates();
        artifactCoordinatesOne.setId(String.format(pathTemplate, "10"));
        ArtifactEntity artifactEntityOne = new ArtifactEntity(storageId, repositoryId, artifactCoordinatesOne);

        RawArtifactCoordinates artifactCoordinatesTwo = new RawArtifactCoordinates();
        artifactCoordinatesTwo.setId(String.format(pathTemplate, "20"));
        Artifact artifactEntityTwo = new RemoteArtifactEntity(storageId, repositoryId, artifactCoordinatesTwo);

        RawArtifactCoordinates artifactCoordinatesThree = new RawArtifactCoordinates();
        artifactCoordinatesThree.setId(String.format(pathTemplate, "30"));
        ArtifactEntity artifactEntityThree = new ArtifactEntity(storageId, repositoryId, artifactCoordinatesThree);

        ArtifactIdGroupEntity artifactIdGroupEntity = new ArtifactIdGroupEntity(storageId, repositoryId,
                "path/to/resource/aigrt-fosw");
        artifactIdGroupEntity.addArtifact(artifactEntityOne);
        artifactIdGroupEntity.addArtifact(artifactEntityTwo);
        artifactIdGroupEntity.addArtifact(artifactEntityThree);
        artifactIdGroupRepository.save(artifactIdGroupEntity);

        // Second group
        pathTemplate = "path/to/resource/aigrt-fosw-another-%s.jar";
        RawArtifactCoordinates artifactCoordinatesAnotherOne = new RawArtifactCoordinates();
        artifactCoordinatesAnotherOne.setId(String.format(pathTemplate, "10"));
        ArtifactEntity artifactEntityAnotherOne = new ArtifactEntity(storageId, repositoryId, artifactCoordinatesAnotherOne);

        RawArtifactCoordinates artifactCoordinatesAnotherTwo = new RawArtifactCoordinates();
        artifactCoordinatesAnotherTwo.setId(String.format(pathTemplate, "20"));
        Artifact artifactEntityAnotherTwo = new RemoteArtifactEntity(storageId, repositoryId, artifactCoordinatesAnotherTwo);

        ArtifactIdGroupEntity artifactIdGroupEntityAnother = new ArtifactIdGroupEntity(storageId, repositoryId,
                "path/to/resource/aigrt-fosw-another");
        artifactIdGroupEntityAnother.addArtifact(artifactEntityAnotherOne);
        artifactIdGroupEntityAnother.addArtifact(artifactEntityAnotherTwo);
        artifactIdGroupRepository.save(artifactIdGroupEntityAnother);

        Optional<ArtifactIdGroup> artifactIdGroupOptional = artifactIdGroupRepository.findOne(storageId, repositoryId,
                                                                                              "path/to/resource/aigrt-fosw");
        assertThat(artifactIdGroupOptional).isNotEmpty();
        assertThat(artifactIdGroupOptional.get().getUuid()).isNotNull();
        assertThat(artifactIdGroupOptional.get().getStorageId()).isEqualTo(storageId);
        assertThat(artifactIdGroupOptional.get().getRepositoryId()).isEqualTo(repositoryId);
        assertThat(artifactIdGroupOptional.get().getArtifacts()).containsOnly(artifactEntityOne, artifactEntityTwo,
                                                                              artifactEntityThree);
        assertThat(artifactIdGroupOptional.get()
                                          .getArtifacts()).filteredOnAssertions(a -> assertThat(a).isInstanceOf(RemoteArtifact.class))
                                                          .hasSize(1);

        artifactIdGroupEntity.removeArtifact(artifactEntityOne);
        artifactIdGroupEntity.removeArtifact(artifactEntityTwo);
        artifactIdGroupEntity.removeArtifact(artifactEntityThree);
        artifactIdGroupEntity = artifactIdGroupRepository.save(artifactIdGroupEntity);
        assertThat(artifactIdGroupEntity.getArtifacts()).isEmpty();
        assertThat(g.E()
                   .hasLabel(Edges.ARTIFACT_GROUP_HAS_ARTIFACTS)
                   .count().next()).isEqualTo(2);
        
        artifactIdGroupOptional = artifactIdGroupRepository.findOne(storageId, repositoryId,
                                                                    "path/to/resource/aigrt-fosw");
        assertThat(artifactIdGroupOptional).isNotEmpty();
        assertThat(artifactIdGroupOptional.get().getUuid()).isNotNull();
        assertThat(artifactIdGroupOptional.get().getArtifacts()).isEmpty();

    }
}
