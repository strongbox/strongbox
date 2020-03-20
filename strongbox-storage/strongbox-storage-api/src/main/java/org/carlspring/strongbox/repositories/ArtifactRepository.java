package org.carlspring.strongbox.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.gremlin.adapters.ArtifactHierarchyAdapter;
import org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public class ArtifactRepository extends GremlinVertexRepository<Artifact> implements ArtifactEntityQueries
{

    @Inject
    ArtifactHierarchyAdapter artifactAdapter;
    @Inject
    ArtifactEntityQueries queries;

    @Override
    protected ArtifactHierarchyAdapter adapter()
    {
        return artifactAdapter;
    }

    public List<Artifact> findByPathLike(String storageId,
                                         String repositoryId,
                                         String path)
    {
        return EntityTraversalUtils.reduceHierarchy(queries.findByPathLike(storageId, repositoryId, path));
    }

    public Page<Artifact> findMatching(Integer lastAccessedTimeInDays,
                                       Long minSizeInBytes,
                                       Pageable pagination)
    {
        LocalDateTime date = Optional.ofNullable(lastAccessedTimeInDays)
                                     .map(v -> LocalDateTime.now().minusDays(lastAccessedTimeInDays))
                                     .orElse(null);
        return findMatching(date, minSizeInBytes, pagination);
    }

    public Page<Artifact> findMatching(LocalDateTime lastAccessedDate,
                                       Long minSizeInBytes,
                                       Pageable pagination)
    {
        return queries.findMatching(lastAccessedDate, minSizeInBytes, pagination);
    }

    public Boolean artifactExists(String storageId,
                                  String repositoryId,
                                  String path)
    {
        return Optional.ofNullable(queries.artifactExists(storageId, repositoryId, path)).orElse(Boolean.FALSE);
    }

    public List<Artifact> findOneArtifactHierarchy(String storageId,
                                                   String repositoryId,
                                                   String path)
    {
        return queries.findOneArtifactHierarchy(storageId, repositoryId, path);
    }

    public Artifact findOneArtifact(String storageId,
                                    String repositoryId,
                                    String path)
    {
        List<Artifact> result = queries.findOneArtifactHierarchy(storageId, repositoryId, path);
        if (result.isEmpty())
        {
            return null;
        }
        return result.stream()
                     .reduce((a1,
                              a2) -> a1.getClass().isInstance(a2) ? a1 : a2)
                     .get();
    }

    @Override
    public <R extends Artifact> R save(R entity)
    {
        return super.save(entity);
    }

}

@Repository
interface ArtifactEntityQueries extends org.springframework.data.repository.Repository<Artifact, String>
{

    @Query("MATCH (genericCoordinates:GenericArtifactCoordinates)<-[r1]-(artifact:Artifact) " +
           "WHERE genericCoordinates.uuid STARTS WITH $path and artifact.storageId=$storageId and artifact.repositoryId=$repositoryId " +
           "WITH artifact, r1, genericCoordinates " +
           "MATCH (genericCoordinates)<-[r2]-(layoutCoordinates) " +
           "WITH artifact, r1, genericCoordinates, r2, layoutCoordinates " +
           "OPTIONAL MATCH (artifact)<-[r3]-(remoteArtifact) " +
           "RETURN artifact, r3, remoteArtifact, r1, genericCoordinates, r2, layoutCoordinates")
    List<Artifact> findByPathLike(@Param("storageId") String storageId,
                                  @Param("repositoryId") String repositoryId,
                                  @Param("path") String path);

    @Query(value = "MATCH (genericCoordinates:GenericArtifactCoordinates)<-[r1]-(artifact:Artifact) " +
                   "WHERE artifact.lastUsed <= coalesce($lastAccessedDate, artifact.lastUsed) and artifact.sizeInBytes >=  coalesce($minSizeInBytes, artifact.sizeInBytes) " +
                   "WITH artifact, r1, genericCoordinates " +
                   "MATCH (genericCoordinates)<-[r2]-(layoutCoordinates) " +
                   "WITH artifact, r1, genericCoordinates, r2, layoutCoordinates " +
                   "OPTIONAL MATCH (artifact)<-[r3]-(remoteArtifact) " +
                   "RETURN artifact, r3, remoteArtifact, r1, genericCoordinates, r2, layoutCoordinates", 
           countQuery = "MATCH (artifact:Artifact) " +
                        "WHERE artifact.lastUsed <= coalesce($lastAccessedDate, artifact.lastUsed) and artifact.sizeInBytes >=  coalesce($minSizeInBytes, artifact.sizeInBytes) " +
                        "RETURN count(artifact)")
    Page<Artifact> findMatching(@Param("lastAccessedDate") LocalDateTime lastAccessedDate,
                                @Param("minSizeInBytes") Long minSizeInBytes,
                                Pageable page);

    @Query("MATCH (genericCoordinates:GenericArtifactCoordinates)<-[r1]-(artifact:Artifact) " +
           "WHERE genericCoordinates.uuid=$path and artifact.storageId=$storageId and artifact.repositoryId=$repositoryId " +
           "RETURN EXISTS(artifact.uuid)")
    Boolean artifactExists(@Param("storageId") String storageId,
                           @Param("repositoryId") String repositoryId,
                           @Param("path") String path);

    @Query("MATCH (genericCoordinates:GenericArtifactCoordinates)<-[r1]-(artifact:Artifact) " +
           "WHERE genericCoordinates.uuid=$path and artifact.storageId=$storageId and artifact.repositoryId=$repositoryId " +
           "WITH artifact, r1, genericCoordinates " +
           "MATCH (genericCoordinates)<-[r2]-(layoutCoordinates) " +
           "WITH artifact, r1, genericCoordinates, r2, layoutCoordinates " +
           "OPTIONAL MATCH (artifact)<-[r3]-(remoteArtifact) " +
           "RETURN artifact, r3, remoteArtifact, r1, genericCoordinates, r2, layoutCoordinates")
    List<Artifact> findOneArtifactHierarchy(@Param("storageId") String storageId,
                                            @Param("repositoryId") String repositoryId,
                                            @Param("path") String path);

}
