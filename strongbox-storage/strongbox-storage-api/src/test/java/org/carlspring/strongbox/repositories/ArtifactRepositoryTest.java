package org.carlspring.strongbox.repositories;

import org.carlspring.strongbox.artifact.coordinates.RawArtifactCoordinates;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactArchiveListing;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.gremlin.tx.TransactionContext;
import org.carlspring.strongbox.util.LocalDateTimeInstance;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = RepositoriesTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
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

        ArtifactEntity artifactEntity = new ArtifactEntity("storage0", repositoryId, artifactCoordinates);
        artifactEntity.getArtifactArchiveListing()
                      .setFilenames(new HashSet<>(Arrays.asList("file1.txt", "readme.md", "icon.svg")));
        artifactEntity.addChecksums(new HashSet<>(
                Arrays.asList("{md5}3111519d5b4efd31565831f735ab0d2f", "{sha-1}ba79baeb 9f10896a 46ae7471 5271b7f5 86e74640")));

        LocalDateTime now = LocalDateTimeInstance.now();

        artifactEntity.setCreated(now.minusDays(10));
        artifactEntity.setLastUsed(now.minusDays(5));
        artifactEntity.setLastUpdated(now);

        artifactEntity = artifactRepository.save(artifactEntity);
        assertThat(artifactEntity.getUuid()).isNotNull();
        assertThat(artifactEntity.getStorageId()).isEqualTo(storageId);
        assertThat(artifactEntity.getRepositoryId()).isEqualTo(repositoryId);
        assertThat(artifactEntity.getChecksums()).containsEntry("md5", "3111519d5b4efd31565831f735ab0d2f")
                                                 .containsEntry("sha-1", "ba79baeb 9f10896a 46ae7471 5271b7f5 86e74640");
        assertThat(artifactEntity.getCreated()).isEqualTo(now.minusDays(10));
        assertThat(artifactEntity.getLastUpdated()).isEqualTo(now);
        assertThat(artifactEntity.getLastUsed()).isEqualTo(now.minusDays(5));
        
        ArtifactArchiveListing artifactArchiveListing = artifactEntity.getArtifactArchiveListing();
        assertThat(artifactArchiveListing.getFilenames()).containsOnly("file1.txt", "readme.md", "icon.svg");

        artifactCoordinates = (RawArtifactCoordinates) artifactEntity.getArtifactCoordinates();
        assertThat(artifactCoordinates.getUuid()).isEqualTo(path);
        assertThat(artifactCoordinates.getVersion()).isNull();
        assertThat(artifactCoordinates.getId()).isEqualTo(path);
        assertThat(artifactCoordinates.getCoordinates()).hasSize(1);
        assertThat(artifactCoordinates.getCoordinates()).hasValueSatisfying(new Condition<>(path::equals,
                "Coordinates should have path value."));

        assertThat(g.E()
                    .hasLabel(Edges.ARTIFACT_HAS_ARTIFACT_COORDINATES)
                    .bothV()
                    .properties("uuid")
                    .map(p -> p.get().value())
                    .toList()).contains(artifactEntity.getUuid(), path).hasSize(2);
        assertThat(g.V().hasLabel(Vertices.RAW_ARTIFACT_COORDINATES).has("uuid", path).hasNext()).isTrue();
        assertThat(g.V().hasLabel(Vertices.GENERIC_ARTIFACT_COORDINATES).has("uuid", path).hasNext()).isTrue();
        assertThat(g.E()
                    .hasLabel(Edges.EXTENDS)
                    .bothV()
                    .properties("uuid")
                    .map(p -> p.get().value())
                    .toList()).contains(path, atIndex(0)).contains(path, atIndex(0)).hasSize(2);

        artifactRepository.delete(artifactEntity);
        assertThat(artifactRepository.findById(artifactEntity.getUuid())).isEmpty();

        assertThat(g.V().hasLabel(Vertices.RAW_ARTIFACT_COORDINATES).has("uuid", path).hasNext()).isTrue();
        assertThat(g.V().hasLabel(Vertices.GENERIC_ARTIFACT_COORDINATES).has("uuid", path).hasNext()).isTrue();
        assertThat(g.E().hasLabel(Edges.EXTENDS).hasNext()).isTrue();
        assertThat(g.E().hasLabel(Edges.ARTIFACT_HAS_ARTIFACT_COORDINATES).hasNext()).isFalse();
    }

    @Test
    @Transactional
    public void findByPathShouldWork()
    {
        GraphTraversalSource g = graph.traversal();
        String storageId = "storage0";
        String repositoryId = "repository-art-fbpsw";
        String path = "path/to/resource/art-fbpsw-10.jar";

        RawArtifactCoordinates artifactCoordinates = new RawArtifactCoordinates();
        artifactCoordinates.setId(path);

        ArtifactEntity artifactEntity = new ArtifactEntity(storageId, repositoryId, artifactCoordinates);
        artifactEntity.getArtifactArchiveListing()
                      .setFilenames(new HashSet<>(Arrays.asList("file1.txt", "readme.md", "icon.svg")));

        LocalDateTime now = LocalDateTimeInstance.now();

        artifactEntity.setCreated(now.minusDays(10));
        artifactEntity.setLastUsed(now.minusDays(5));
        artifactEntity.setLastUpdated(now);
        
        artifactEntity = artifactRepository.save(artifactEntity);

        assertThat(artifactEntity.getUuid()).isNotNull();
        assertThat(artifactEntity.getStorageId()).isEqualTo(storageId);
        assertThat(artifactEntity.getRepositoryId()).isEqualTo(repositoryId);
        assertThat(artifactEntity.getCreated()).isEqualTo(now.minusDays(10));
        assertThat(artifactEntity.getLastUpdated()).isEqualTo(now);
        assertThat(artifactEntity.getLastUsed()).isEqualTo(now.minusDays(5));
        
        artifactCoordinates = (RawArtifactCoordinates) artifactEntity.getArtifactCoordinates();

        assertThat(artifactCoordinates.getUuid()).isEqualTo(path);
        assertThat(artifactCoordinates.getVersion()).isNull();
        assertThat(artifactCoordinates.getId()).isEqualTo(path);
        assertThat(artifactCoordinates.getCoordinates()).hasSize(1);
        assertThat(artifactCoordinates.getCoordinates()).hasValueSatisfying(new Condition<>(path::equals,
                "Coordinates should have path value."));

        Artifact artifact = artifactRepository.findOneArtifact(storageId, repositoryId, path);

        assertThat(artifact).isInstanceOf(Artifact.class);

        ArtifactArchiveListing artifactArchiveListing = artifact.getArtifactArchiveListing();

        assertThat(artifactArchiveListing.getFilenames()).containsOnly("file1.txt", "readme.md", "icon.svg");

        artifactCoordinates = (RawArtifactCoordinates) artifact.getArtifactCoordinates();

        assertThat(artifactCoordinates.getUuid()).isEqualTo(path);
        assertThat(artifactCoordinates.getVersion()).isNull();
        assertThat(artifactCoordinates.getId()).isEqualTo(path);
        assertThat(artifactCoordinates.getCoordinates()).hasSize(1);
        assertThat(artifactCoordinates.getCoordinates()).hasValueSatisfying(new Condition<>(path::equals,
                "Coordinates should have path value."));

    }

    @Test
    @Transactional
    public void artifactEntityExistsShouldWork()
    {
        GraphTraversalSource g = graph.traversal();
        String storageId = "storage0";
        String repositoryId = "repository-art-esw";
        String path = "path/to/resource/art-esw-10.jar";

        assertThat(artifactRepository.artifactEntityExists(storageId, repositoryId, path)).isFalse();

        RawArtifactCoordinates artifactCoordinates = new RawArtifactCoordinates();
        artifactCoordinates.setId(path);
        ArtifactEntity artifactEntity = new ArtifactEntity(storageId, repositoryId, artifactCoordinates);
        artifactRepository.save(artifactEntity);

        assertThat(artifactRepository.artifactEntityExists(storageId, repositoryId, path)).isTrue();
    }

    @Test
    @Transactional
    public void artifactExistsShouldWork()
    {
        GraphTraversalSource g = graph.traversal();
        String storageId = "storage0";
        String repositoryId = "repository-art-aesw";
        String path = "path/to/resource/art-aesw-10.jar";

        assertThat(artifactRepository.artifactExists(storageId, repositoryId, path)).isFalse();

        RawArtifactCoordinates artifactCoordinates = new RawArtifactCoordinates();
        artifactCoordinates.setId(path);
        ArtifactEntity artifactEntity = new ArtifactEntity(storageId, repositoryId, artifactCoordinates);
        artifactRepository.save(artifactEntity);
        assertThat(artifactRepository.artifactExists(storageId, repositoryId, path)).isTrue();
        
        String remotePath = "path/to/resource/art-aesw-remote-10.jar";
        RawArtifactCoordinates remoteArtifactCoordinates = new RawArtifactCoordinates();
        remoteArtifactCoordinates.setId(remotePath);
        ArtifactEntity remoteArtifactEntity = new ArtifactEntity("storage0", repositoryId, remoteArtifactCoordinates);
        remoteArtifactEntity.setArtifactFileExists(false);
        remoteArtifactEntity = artifactRepository.save(remoteArtifactEntity);
        assertThat(remoteArtifactEntity.getArtifactFileExists()).isFalse();
        assertThat(artifactRepository.artifactExists(storageId, repositoryId, remotePath)).isFalse();
        
        remoteArtifactEntity.setArtifactFileExists(true);
        remoteArtifactEntity = artifactRepository.save(remoteArtifactEntity);
        assertThat(artifactRepository.artifactExists(storageId, repositoryId, remotePath)).isTrue();
    }

    
    @Test
    @Transactional
    public void findByPathLikeShouldWork()
    {
        GraphTraversalSource g = graph.traversal();
        String storageId = "storage0";
        String repositoryId = "repository-art-fbpsw";
        String path = "path/to/resource/art-fbpsw-10.jar";
        String remotePath = "path/to/resource/art-fbpsw-remote-10.jar";

        RawArtifactCoordinates artifactCoordinates = new RawArtifactCoordinates();
        artifactCoordinates.setId(path);
        ArtifactEntity artifactEntity = new ArtifactEntity(storageId, repositoryId, artifactCoordinates);
        artifactEntity = artifactRepository.save(artifactEntity);

        RawArtifactCoordinates remoteArtifactCoordinates = new RawArtifactCoordinates();
        remoteArtifactCoordinates.setId(remotePath);
        ArtifactEntity remoteArtifactEntity = new ArtifactEntity("storage0", repositoryId, remoteArtifactCoordinates);
        remoteArtifactEntity.setArtifactFileExists(true);
        remoteArtifactEntity = artifactRepository.save(remoteArtifactEntity);

        List<Artifact> artifacts = artifactRepository.findByPathLike(storageId, repositoryId, "path/to/resource/art-fbpsw");
        assertThat(artifacts).hasSize(2).contains(artifactEntity, remoteArtifactEntity);
    }

}
