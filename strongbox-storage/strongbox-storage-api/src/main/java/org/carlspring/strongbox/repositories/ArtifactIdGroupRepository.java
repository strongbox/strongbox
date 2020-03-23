package org.carlspring.strongbox.repositories;

import java.util.List;

import javax.inject.Inject;

import org.carlspring.strongbox.data.service.support.search.PagingCriteria;
import org.carlspring.strongbox.domain.ArtifactIdGroup;
import org.carlspring.strongbox.gremlin.adapters.ArtifactIdGroupAdapter;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;
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

    public ArtifactIdGroup findOneOrCreate(String storageId,
                                           String repositoryId,
                                           String artifactId)
    {
        return queries.findOneOrCreate(storageId, repositoryId, artifactId);
    }

    public ArtifactIdGroup findOne(String storageId,
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

    // @Query("MATCH (aig:`ArtifactIdGroup`) WHERE aig.storageId=$storageId and
    // aig.storageId=$repositoryId and aig.storageId=$artifactId RETURN ae,
    // aeac, ac")
    default ArtifactIdGroup findOneOrCreate(String storageId,
                                            String repositoryId,
                                            String artifactId)
    {
        return null;
    }

    default ArtifactIdGroup findOne(String storageId,
                                    String repositoryId,
                                    String artifactId)
    {
        return null;
    }

}
