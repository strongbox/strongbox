package org.carlspring.strongbox.repositories;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.GenericArtifactCoordinates;
import org.carlspring.strongbox.gremlin.adapters.ArtifactCoordinatesHierarchyAdapter;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class ArtifactCoordinatesRepository extends GremlinVertexRepository<GenericArtifactCoordinates>
        implements ArtifactCoordinatesQueries
{

    @Inject
    ArtifactCoordinatesHierarchyAdapter artifactCoordinatesAdapter;
    @Inject
    ArtifactCoordinatesQueries queries;

    @Override
    protected ArtifactCoordinatesHierarchyAdapter adapter()
    {
        return artifactCoordinatesAdapter;
    }

    @Override
    public <R extends GenericArtifactCoordinates> R save(R entity)
    {
        if (entity.getUuid() == null)
        {
            ((ArtifactCoordinates)entity).buildPath();
        }

        return super.save(entity);
    }

}

@Repository
interface ArtifactCoordinatesQueries extends org.springframework.data.repository.Repository<GenericArtifactCoordinates, String>
{

}
