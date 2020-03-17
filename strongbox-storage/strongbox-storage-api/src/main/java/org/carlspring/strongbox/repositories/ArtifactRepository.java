package org.carlspring.strongbox.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.carlspring.strongbox.data.service.support.search.PagingCriteria;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.gremlin.adapters.ArtifactHierarchyAdapter;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;
import org.carlspring.strongbox.services.support.ArtifactEntrySearchCriteria;
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

    public List<Artifact> findArtifactList(String storageId,
                                           String repositoryId,
                                           Map<String, String> coordinates,
                                           boolean strict)
    {
        return queries.findArtifactList(storageId, repositoryId, coordinates, strict);
    }

    public List<Artifact> findMatching(ArtifactEntrySearchCriteria searchCriteria,
                                       PagingCriteria pagingCriteria)
    {
        return queries.findMatching(searchCriteria, pagingCriteria);
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

}

@Repository
interface ArtifactEntityQueries extends org.springframework.data.repository.Repository<Artifact, String>
{

    default List<Artifact> findArtifactList(String storageId,
                                            String repositoryId,
                                            Map<String, String> coordinates,
                                            boolean strict)
    {
        return null;
    }

    default List<Artifact> findMatching(ArtifactEntrySearchCriteria searchCriteria,
                                        PagingCriteria pagingCriteria)
    {
        return null;
    }

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
