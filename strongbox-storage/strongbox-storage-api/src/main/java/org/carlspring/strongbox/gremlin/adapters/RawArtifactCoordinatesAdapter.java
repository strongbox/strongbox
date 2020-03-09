package org.carlspring.strongbox.gremlin.adapters;

import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.extractObject;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.artifact.coordinates.RawArtifactCoordinates;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.GenericArtifactCoordinatesEntity;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.__;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 */
@Component
public class RawArtifactCoordinatesAdapter
        extends LayoutArtifactCoordinatesArapter<RawArtifactCoordinates, RawArtifactCoordinates>
{

    @Override
    public Set<String> labels()
    {
        return Collections.singleton(Vertices.RAW_ARTIFACT_COORDINATES);
    }

    @Override
    public EntityTraversal<Vertex, RawArtifactCoordinates> fold()
    {
        return fold(genericArtifactCoordinatesProjection());
    }

    <S> EntityTraversal<S, RawArtifactCoordinates> fold(EntityTraversal<Vertex, Object> genericArtifactCoordinatesProjection)
    {
        return __.<S>hasLabel(Vertices.RAW_ARTIFACT_COORDINATES)
                 .project("uuid", "genericArtifactCoordinates")
                 .by(__.enrichPropertyValue("uuid"))
                 .by(genericArtifactCoordinatesProjection)
                 .map(this::map);
    }

    private RawArtifactCoordinates map(Traverser<Map<String, Object>> t)
    {
        GenericArtifactCoordinatesEntity genericArtifactCoordinates = extractObject(GenericArtifactCoordinatesEntity.class,
                                                                                    t.get()
                                                                                     .get("genericArtifactCoordinates"));
        RawArtifactCoordinates result;
        if (genericArtifactCoordinates == null)
        {
            result = new RawArtifactCoordinates();
        }
        else
        {
            result = new RawArtifactCoordinates(genericArtifactCoordinates);
            genericArtifactCoordinates.setLayoutArtifactCoordinates(result);
        }
        result.setUuid(extractObject(String.class, t.get().get("uuid")));

        return result;
    }

    @Override
    public UnfoldTraversal<Vertex> unfold(RawArtifactCoordinates entity)
    {
        if (!RawArtifactCoordinates.class.isInstance(entity))
        {
            return null;
        }
        return new UnfoldTraversal<Vertex>(Vertices.RAW_ARTIFACT_COORDINATES, __.identity());
    }

}
