package org.carlspring.strongbox.repositories;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.carlspring.strongbox.data.service.support.search.PagingCriteria;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.gremlin.adapters.ArtifactAdapter;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;
import org.carlspring.strongbox.services.support.ArtifactEntrySearchCriteria;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public class ArtifactRepository extends GremlinVertexRepository<Artifact> implements ArtifactEntityQueries
{

    @Inject
    ArtifactAdapter artifactAdapter;
    @Inject
    ArtifactEntityQueries queries;

    @Override
    protected ArtifactAdapter adapter()
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

    public boolean artifactExists(String storageId,
                                  String repositoryId,
                                  String path)
    {
        return queries.artifactExists(storageId, repositoryId, path);
    }

    public Artifact findOneArtifact(String storageId,
                                    String repositoryId,
                                    String path)
    {
        return queries.findOneArtifact(storageId, repositoryId, path);
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

    default boolean artifactExists(String storageId,
                                   String repositoryId,
                                   String path)
    {
        return false;
    }

    @Query("MATCH (gac:`GenericArtifactCoordinates` {uuid:$path})<-[ahac:`ArtifactHasArtifactCoordinates`]-(a:`Artifact`) " +
            "WITH gac, ahac, a " +
            "MATCH (gac)<-[acigac:`ArtifactCoordinatesInheritGenericArtifactCoordinates`]-(ac) " +
            "WHERE a.storageId = $storageId and a.repositoryId = $repositoryId " +
            "RETURN a, ahac, gac, acigac, ac")
    Artifact findOneArtifact(@Param("storageId") String storageId,
                             @Param("repositoryId") String repositoryId,
                             @Param("path") String path);

}
