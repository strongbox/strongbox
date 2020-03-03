package org.carlspring.strongbox.repositories;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.gremlin.adapters.ArtifactCoordinatesAdapter;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ArtifactCoordinatesRepository extends GremlinVertexRepository<ArtifactCoordinates>
        implements ArtifactCoordinatesQueries
{

    @Inject
    ArtifactCoordinatesAdapter artifactCoordinatesAdapter;

    @Inject
    ArtifactCoordinatesQueries queries;

    public ArtifactCoordinatesRepository()
    {
        super(ArtifactCoordinates.class);
    }

    @Override
    protected ArtifactCoordinatesAdapter adapter()
    {
        return artifactCoordinatesAdapter;
    }

}

@Repository
interface ArtifactCoordinatesQueries extends org.springframework.data.repository.Repository<ArtifactCoordinates, String>
{

}
