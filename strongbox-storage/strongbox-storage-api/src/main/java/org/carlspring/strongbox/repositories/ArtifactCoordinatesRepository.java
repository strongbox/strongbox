package org.carlspring.strongbox.repositories;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.db.schema.Vertices;
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

    @Override
    protected ArtifactCoordinatesAdapter adapter()
    {
        return artifactCoordinatesAdapter;
    }

    @Override
    public <R extends ArtifactCoordinates> R save(R entity)
    {
        if (entity.getUuid() == null)
        {
            System.out.println(entity.buildPath());
        }

//        System.out.println(start(this::g).saveV(Vertices.GENERIC_ARTIFACT_COORDINATES, entity.getUuid(), __.identity())
//                      .enrichPropertyValue("uuid")
//                      .next());
        
        return super.save(entity);
    }

}

@Repository
interface ArtifactCoordinatesQueries extends org.springframework.data.repository.Repository<ArtifactCoordinates, String>
{

}
