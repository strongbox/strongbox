package org.carlspring.strongbox.repositories;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.gremlin.adapters.EntityTraversalAdapter;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ArtifactCoordinatesRepository extends GremlinVertexRepository<ArtifactCoordinates>
        implements ArtifactCoordinatesQueries
{

    @Inject
    ArtifactCoordinatesQueries queries;

    @Override
    protected EntityTraversalAdapter<Vertex, ArtifactCoordinates> adapter()
    {
        return null;
    }

}

@Repository
interface ArtifactCoordinatesQueries extends org.springframework.data.repository.Repository<ArtifactCoordinates, String>
{

}
