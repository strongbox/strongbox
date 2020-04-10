package org.carlspring.strongbox.repositories;

import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactIdGroup;
import org.carlspring.strongbox.gremlin.adapters.ArtifactIdGroupAdapter;
import org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class ArtifactIdGroupRepository extends GremlinVertexRepository<ArtifactIdGroup>
        implements ArtifactIdGroupQueries
{

    @Inject
    ArtifactIdGroupAdapter adapter;
    @Inject
    ArtifactIdGroupQueries queries;

    @Override
    protected ArtifactIdGroupAdapter adapter()
    {
        return adapter;
    }

    public Page<ArtifactIdGroup> findMatching(String storageId,
                                              String repositoryId,
                                              Pageable page)
    {
        return queries.findMatching(storageId, repositoryId, page);
    }

    public Optional<ArtifactIdGroup> findOne(String storageId,
                                             String repositoryId,
                                             String artifactId)
    {
        return queries.findOne(storageId, repositoryId, artifactId);
    }

    @Override
    public Page<Artifact> findArtifacts(String storageId,
                                        String repositoryId,
                                        String artifactId,
                                        String coordinateValue,
                                        Pageable page)
    {
        Page<Artifact> result = queries.findArtifacts(storageId, repositoryId, artifactId, coordinateValue, page);
        return new PageImpl<>(EntityTraversalUtils.reduceHierarchy(result.toList()), page, result.getTotalElements());
    }

}

@Repository
interface ArtifactIdGroupQueries
        extends org.springframework.data.repository.Repository<ArtifactIdGroup, String>
{
    @Query(value = "MATCH (aig:`ArtifactIdGroup`) " +
                   "WHERE aig.storageId=$storageId and aig.repositoryId=$repositoryId " +
                   "WITH aig " +
                   "OPTIONAL MATCH (aig)-[r0]->(artifact:Artifact)-[r1]->(genericCoordinates:GenericArtifactCoordinates)<-[r2]-(layoutCoordinates) " +
                   "WITH aig, r0, artifact, r1, genericCoordinates, r2, layoutCoordinates " +
                   "OPTIONAL MATCH (artifact)-[r4]->(tag:ArtifactTag) " +
                   "WITH aig, r0, artifact, r1, genericCoordinates, r2, layoutCoordinates, r4, tag " +
                   "OPTIONAL MATCH (artifact)<-[r3]-(remoteArtifact) " +
                   "RETURN aig, r0, artifact, r3, remoteArtifact, r1, genericCoordinates, r2, layoutCoordinates, r4, tag",
           countQuery = "MATCH (aig:`ArtifactIdGroup`) " +
                        "WHERE aig.storageId=$storageId and aig.repositoryId=$repositoryId " +
                        "RETURN count(aig)")
    Page<ArtifactIdGroup> findMatching(@Param("storageId") String storageId,
                                       @Param("repositoryId") String repositoryId,
                                       Pageable page);

    @Query("MATCH (aig:`ArtifactIdGroup`) " +
           "WHERE aig.storageId=$storageId and aig.repositoryId=$repositoryId and aig.name=$artifactId " +
           "WITH aig " +
           "OPTIONAL MATCH (aig)-[r0]->(artifact:Artifact)-[r1]->(genericCoordinates:GenericArtifactCoordinates)<-[r2]-(layoutCoordinates) " +
           "WITH aig, r0, artifact, r1, genericCoordinates, r2, layoutCoordinates " +
           "OPTIONAL MATCH (artifact)-[r4]->(tag:ArtifactTag) " +
           "WITH aig, r0, artifact, r1, genericCoordinates, r2, layoutCoordinates, r4, tag " +
           "OPTIONAL MATCH (artifact)<-[r3]-(remoteArtifact) " +
           "RETURN aig, r0, artifact, r3, remoteArtifact, r1, genericCoordinates, r2, layoutCoordinates,  r4, tag")
    Optional<ArtifactIdGroup> findOne(@Param("storageId") String storageId,
                                      @Param("repositoryId") String repositoryId,
                                      @Param("artifactId") String artifactId);

    @Query(value = "MATCH (aig:`ArtifactIdGroup`) " +
                   "WHERE aig.storageId=$storageId and aig.repositoryId=$repositoryId and aig.name CONTAINS $artifactId " +
                   "WITH aig " +
                   "MATCH (aig)-[r0]->(artifact:Artifact)-[r1]->(genericCoordinates:GenericArtifactCoordinates)<-[r2]-(layoutCoordinates) " +
                   "UNWIND keys(genericCoordinates) AS coordinate " +
                   "WITH aig, r0, artifact, r1, genericCoordinates, r2, layoutCoordinates, coordinate " +
                   "WHERE coordinate STARTS WITH 'coordinates.' AND genericCoordinates[coordinate]=$coordinateValue " +
                   "OPTIONAL MATCH (artifact)-[r4]->(tag:ArtifactTag) " +
                   "WITH aig, r0, artifact, r1, genericCoordinates, r2, layoutCoordinates, r4, tag " +
                   "OPTIONAL MATCH (artifact)<-[r3]-(remoteArtifact) " +
                   "RETURN artifact, r3, remoteArtifact, r1, genericCoordinates, r2, layoutCoordinates,  r4, tag",
           countQuery = "MATCH (aig:`ArtifactIdGroup`) " +
                        "WHERE aig.storageId=$storageId and aig.repositoryId=$repositoryId and aig.name CONTAINS $artifactId " +
                        "WITH aig " +
                        "MATCH (aig)-[r0]->(artifact:Artifact)-[r1]->(genericCoordinates:GenericArtifactCoordinates)<-[r2]-(layoutCoordinates) " +
                        "UNWIND keys(genericCoordinates) AS coordinate " +
                        "WITH aig, r0, artifact, r1, genericCoordinates, r2, layoutCoordinates, coordinate " +
                        "WHERE coordinate STARTS WITH 'coordinates.' AND genericCoordinates[coordinate]=$coordinateValue " +
                        "RETURN count(artifact)")
    Page<Artifact> findArtifacts(@Param("storageId") String storageId,
                                 @Param("repositoryId") String repositoryId,
                                 @Param("artifactId") String artifactId,
                                 @Param("coordinateValue") String coordinateValue,
                                 Pageable page);

}
