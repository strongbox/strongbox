package org.carlspring.strongbox.repositories;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.gremlin.adapters.EntityTraversalAdapter;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ArtifactTagRepository extends GremlinVertexRepository<ArtifactTag>
        implements ArtifactTagQueries
{

    @Inject
    ArtifactTagQueries queries;

    @Override
    protected EntityTraversalAdapter<Vertex, ArtifactTag> adapter()
    {
        return null;
    }

}

@Repository
interface ArtifactTagQueries
        extends org.springframework.data.repository.Repository<ArtifactTag, String>
{

}
