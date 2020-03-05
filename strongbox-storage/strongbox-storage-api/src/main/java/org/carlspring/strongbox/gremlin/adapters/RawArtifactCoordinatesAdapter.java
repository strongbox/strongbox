package org.carlspring.strongbox.gremlin.adapters;

import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.extractObject;

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Element;
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
    public EntityTraversal<Vertex, RawArtifactCoordinates> fold()
    {
        return fold(genericArtifactCoordinatesProjection());
    }

    <S> EntityTraversal<S, RawArtifactCoordinates> fold(EntityTraversal<Vertex, Object> genericArtifactCoordinatesTraversal)
    {
        return __.<S>hasLabel(Vertices.RAW_ARTIFACT_COORDINATES)
                 .project("uuid", "genericArtifactCoordinates")
                 .by(__.enrichPropertyValue("uuid"))
                 .by(genericArtifactCoordinatesTraversal)
                 .map(this::map);
    }

    private RawArtifactCoordinates map(Traverser<Map<String, Object>> t)
    {
        GenericArtifactCoordinatesEntity genericArtifactCoordinates = extractObject(GenericArtifactCoordinatesEntity.class,
                                                                                    t.get()
                                                                                     .get("genericArtifactCoordinates"));
        RawArtifactCoordinates result = new RawArtifactCoordinates(genericArtifactCoordinates);
        result.setUuid(extractObject(String.class, t.get().get("uuid")));
        genericArtifactCoordinates.setLayoutArtifactCoordinates(result);

        return result;
    }

}
