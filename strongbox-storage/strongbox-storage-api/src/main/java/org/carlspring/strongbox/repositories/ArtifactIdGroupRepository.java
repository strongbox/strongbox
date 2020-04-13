package org.carlspring.strongbox.repositories;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactIdGroup;
import org.carlspring.strongbox.gremlin.adapters.ArtifactIdGroupAdapter;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.ExistsQuery;
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

    public Boolean artifactsExists(Set<String> storageRepositoryIds,
                                   String artifactId,
                                   Collection<String> coordinateValues)
    {
        return queries.artifactsExists(storageRepositoryIds, artifactId, coordinateValues);
    }

    @Override
    public Long countArtifacts(Set<String> storageRepositoryIds,
                               String artifactId,
                               Collection<String> coordinateValues)
    {
        return queries.countArtifacts(storageRepositoryIds, artifactId, coordinateValues);
    }

    public Long countArtifacts(String storageId,
                               String repositoryId,
                               String artifactId,
                               Collection<String> coordinateValues)
    {
        return queries.countArtifacts(Collections.singleton(storageId + ":" + repositoryId), artifactId, coordinateValues);
    }

    public List<Artifact> findArtifacts(Set<String> storageRepositoryIds,
                                        String artifactId,
                                        Collection<String> coordinateValues,
                                        Long skip,
                                        Integer limit)
    {
        return queries.findArtifacts(storageRepositoryIds, artifactId, coordinateValues, skip, limit);
    }
    
    public List<Artifact> findArtifacts(String storageId,
                                        String repositoryId,
                                        String artifactId,
                                        Collection<String> coordinateValues,
                                        Long skip,
                                        Integer limit)
    {
        return queries.findArtifacts(Collections.singleton(storageId + ":" + repositoryId), artifactId, coordinateValues, skip, limit);
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

    @ExistsQuery("UNWIND $storageRepositoryIds as storageRepositoryIdPair " +
                 "WITH split(storageRepositoryIdPair, ':') as storageRepositoryId " +
                 "MATCH (aig:`ArtifactIdGroup`) " +
                 "WHERE aig.storageId=storageRepositoryId[0] and aig.repositoryId=storageRepositoryId[1] and aig.name CONTAINS $artifactId " +
                 "WITH aig " +
                 "MATCH (aig)-[r0]->(artifact:Artifact)-[r1]->(genericCoordinates:GenericArtifactCoordinates)<-[r2]-(layoutCoordinates) " +
                 "UNWIND keys(genericCoordinates) AS coordinate " +
                 "WITH aig, r0, artifact, r1, genericCoordinates, r2, layoutCoordinates, coordinate " +
                 "WHERE coordinate STARTS WITH 'coordinates.' AND genericCoordinates[coordinate] IN $coordinateValues " +
                 "RETURN exists(artifact)")
    Boolean artifactsExists(@Param("storageRepositoryIds") Set<String> storageRepositoryIds,
                            @Param("artifactId") String artifactId,
                            @Param("coordinateValues") Collection<String> coordinateValues);

    @Query("UNWIND $storageRepositoryIds as storageRepositoryIdPair " +
           "WITH split(storageRepositoryIdPair, ':') as storageRepositoryId " +
           "MATCH (aig:`ArtifactIdGroup`) " +
           "WHERE aig.storageId=storageRepositoryId[0] and aig.repositoryId=storageRepositoryId[1] and aig.name CONTAINS $artifactId " +
           "WITH aig " +
           "MATCH (aig)-[r0]->(artifact:Artifact)-[r1]->(genericCoordinates:GenericArtifactCoordinates)<-[r2]-(layoutCoordinates) " +
           "UNWIND keys(genericCoordinates) AS coordinate " +
           "WITH aig, r0, artifact, r1, genericCoordinates, r2, layoutCoordinates, coordinate " +
           "WHERE coordinate STARTS WITH 'coordinates.' AND genericCoordinates[coordinate] IN $coordinateValues " +
           "RETURN count(artifact)")
    Long countArtifacts(@Param("storageRepositoryIds") Set<String> storageRepositoryIds,
                        @Param("artifactId") String artifactId,
                        @Param("coordinateValues") Collection<String> coordinateValues);
    
    @Query("UNWIND $storageRepositoryIds as storageRepositoryIdPair " +
           "WITH split(storageRepositoryIdPair, ':') as storageRepositoryId " +
           "MATCH (aig:`ArtifactIdGroup`) " +
           "WHERE aig.storageId=storageRepositoryId[0] and aig.repositoryId=storageRepositoryId[1] and aig.name CONTAINS $artifactId " +
           "WITH aig " +
           "MATCH (aig)-[r0]->(artifact:Artifact)-[r1]->(genericCoordinates:GenericArtifactCoordinates)<-[r2]-(layoutCoordinates) " +
           "UNWIND keys(genericCoordinates) AS coordinate " +
           "WITH aig, r0, artifact, r1, genericCoordinates, r2, layoutCoordinates, coordinate " +
           "WHERE coordinate STARTS WITH 'coordinates.' AND genericCoordinates[coordinate] IN $coordinateValues " +
           "OPTIONAL MATCH (artifact)-[r4]->(tag:ArtifactTag) " +
           "WITH aig, r0, artifact, r1, genericCoordinates, r2, layoutCoordinates, r4, tag " +
           "OPTIONAL MATCH (artifact)<-[r3]-(remoteArtifact) " +
           "RETURN artifact, r3, remoteArtifact, r1, genericCoordinates, r2, layoutCoordinates,  r4, tag " +
           "ORDER BY aig.name, genericCoordinates.version " +
           "SKIP $skip LIMIT $limit")
    List<Artifact> findArtifacts(@Param("storageRepositoryIds") Set<String> storageRepositoryIds,
                                 @Param("artifactId") String artifactId,
                                 @Param("coordinateValues") Collection<String> coordinateValues,
                                 @Param("skip") Long skip,
                                 @Param("limit") Integer limit);
    
}
