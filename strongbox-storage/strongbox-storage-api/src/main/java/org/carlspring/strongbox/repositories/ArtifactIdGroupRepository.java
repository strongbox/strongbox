package org.carlspring.strongbox.repositories;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.carlspring.strongbox.data.service.support.search.PagingCriteria;
import org.carlspring.strongbox.domain.ArtifactIdGroup;
import org.carlspring.strongbox.gremlin.adapters.ArtifactIdGroupAdapter;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
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

    public long count(String storageId,
                      String repositoryId)
    {
        return queries.count(storageId, repositoryId);
    }

    public List<ArtifactIdGroup> findMatching(String storageId,
                                              String repositoryId,
                                              PagingCriteria pagingCriteria)
    {
        return queries.findMatching(storageId, repositoryId, pagingCriteria);
    }

    public Optional<ArtifactIdGroup> findOne(String storageId,
                                             String repositoryId,
                                             String artifactId)
    {
        return queries.findOne(storageId, repositoryId, artifactId);
    }

}

@Repository
interface ArtifactIdGroupQueries
        extends org.springframework.data.repository.Repository<ArtifactIdGroup, String>
{

    default long count(String storageId,
                       String repositoryId)
    {
        return 0;
    }

    default List<ArtifactIdGroup> findMatching(String storageId,
                                               String repositoryId,
                                               PagingCriteria pagingCriteria)
    {
        return null;
    }

    @Query("MATCH (aig:`ArtifactIdGroup`)-[r0]->(artifact:Artifact)-[r1]->(genericCoordinates:GenericArtifactCoordinates) " +
           "WHERE aig.storageId=$storageId and aig.repositoryId=$repositoryId and aig.name=$artifactId " +
           "WITH aig, r0, artifact, r1, genericCoordinates " +
           "MATCH (genericCoordinates)<-[r2]-(layoutCoordinates) " +
           "WITH aig, r0, artifact, r1, genericCoordinates, r2, layoutCoordinates " +
           "OPTIONAL MATCH (artifact)<-[r3]-(remoteArtifact) " +
           "RETURN aig, r0, artifact, r3, remoteArtifact, r1, genericCoordinates, r2, layoutCoordinates")
    Optional<ArtifactIdGroup> findOne(@Param("storageId") String storageId,
                                      @Param("repositoryId") String repositoryId,
                                      @Param("artifactId") String artifactId);

}
