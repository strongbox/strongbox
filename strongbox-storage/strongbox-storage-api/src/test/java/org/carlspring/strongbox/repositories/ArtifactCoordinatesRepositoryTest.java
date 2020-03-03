package org.carlspring.strongbox.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
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
public class ArtifactCoordinatesRepositoryTest
{

    @Inject
    private ArtifactCoordinatesRepository artifactCoordinatesRepository;
    @Inject
    @TransactionContext
    private Graph graph;

    @Test
    @Transactional
    public void crudShouldWork()
    {
        GraphTraversalSource g = graph.traversal();

        String path = "path/to/resource/acrt-csw-gac-10.jar";
        Vertex vGenericArtifactCoordinates = g.addV(Vertices.GENERIC_ARTIFACT_COORDINATES)
                                              .property("uuid", path)
                                              .property("path", path)
                                              .next();

        Vertex vRawArtifactCoordinates = g.addV(Vertices.RAW_ARTIFACT_COORDINATES)
                                          .property("uuid", path)
                                          .next();

        g.addE(Edges.ARTIFACT_COORDINATES_INHERIT_GENERIC_ARTIFACT_COORDINATES)
         .from(vRawArtifactCoordinates)
         .to(vGenericArtifactCoordinates)
         .next();

        Optional<ArtifactCoordinates> artifactCordinates = artifactCoordinatesRepository.findById(path);
        assertThat(artifactCordinates).isNotEmpty();
    }

}
