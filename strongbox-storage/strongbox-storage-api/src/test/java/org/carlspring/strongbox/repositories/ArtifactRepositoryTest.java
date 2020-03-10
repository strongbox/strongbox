package org.carlspring.strongbox.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.assertj.core.api.Condition;
import org.carlspring.strongbox.artifact.coordinates.RawArtifactCoordinates;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.gremlin.tx.TransactionContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = DataServiceConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class ArtifactRepositoryTest
{

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
        String repositoryId = "repository-art-csw";
        String path = "path/to/resource/art-csw-10.jar";

        RawArtifactCoordinates artifactCoordinates = new RawArtifactCoordinates();
        artifactCoordinates.setId(path);

        ArtifactEntity artifactEntity = new ArtifactEntity("storage0", repositoryId,
                artifactCoordinates.getGenericArtifactCoordinates());

        artifactEntity = artifactRepository.save(artifactEntity);
        assertThat(artifactEntity.getUuid()).isNotNull();
        assertThat(artifactEntity.getStorageId()).isEqualTo(storageId);
        assertThat(artifactEntity.getRepositoryId()).isEqualTo(repositoryId);

        artifactCoordinates = (RawArtifactCoordinates) artifactEntity.getArtifactCoordinates();
        assertThat(artifactCoordinates.getUuid()).isEqualTo(path);
        assertThat(artifactCoordinates.getVersion()).isNull();
        assertThat(artifactCoordinates.getId()).isEqualTo(path);
        assertThat(artifactCoordinates.getCoordinates()).hasSize(1);
        assertThat(artifactCoordinates.getCoordinates()).hasValueSatisfying(new Condition<>(path::equals,
                "Coordinates should have path value."));

        assertThat(g.V().hasLabel(Vertices.RAW_ARTIFACT_COORDINATES).has("uuid", path).hasNext()).isTrue();
        assertThat(g.V().hasLabel(Vertices.GENERIC_ARTIFACT_COORDINATES).has("uuid", path).hasNext()).isTrue();
        assertThat(g.E()
                    .hasLabel(Edges.ARTIFACT_COORDINATES_INHERIT_GENERIC_ARTIFACT_COORDINATES)
                    .bothV()
                    .properties("uuid")
                    .map(p -> p.get().value())
                    .toList()).contains(path, atIndex(0)).contains(path, atIndex(0)).hasSize(2);

        artifactRepository.delete(artifactEntity);
        assertThat(artifactRepository.findById(artifactEntity.getUuid())).isEmpty();

        assertThat(g.V().hasLabel(Vertices.RAW_ARTIFACT_COORDINATES).has("uuid", path).hasNext()).isFalse();
        assertThat(g.V().hasLabel(Vertices.GENERIC_ARTIFACT_COORDINATES).has("uuid", path).hasNext()).isFalse();
        assertThat(g.E().hasLabel(Edges.ARTIFACT_COORDINATES_INHERIT_GENERIC_ARTIFACT_COORDINATES).hasNext()).isFalse();
        assertThat(g.E().hasLabel(Edges.ARTIFACT_HAS_ARTIFACT_COORDINATES).hasNext()).isFalse();
    }

}
