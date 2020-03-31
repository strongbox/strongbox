package org.carlspring.strongbox.repositories;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.gremlin.adapters.ArtifactTagAdapter;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;
import org.springframework.stereotype.Repository;

/**
 * @author sbespalov
 */
@Repository
@Transactional
public class ArtifactTagRepository extends GremlinVertexRepository<ArtifactTag>
        implements ArtifactTagQueries
{

    @Inject
    ArtifactTagAdapter adapter;
    
    @Inject
    ArtifactTagQueries queries;

    @Override
    protected ArtifactTagAdapter adapter()
    {
        return adapter;
    }

}

@Repository
interface ArtifactTagQueries
        extends org.springframework.data.repository.Repository<ArtifactTag, String>
{

}
