package org.carlspring.strongbox.gremlin.adapters;

import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.extractObject;

import java.util.Map;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.artifact.coordinates.RawArtifactCoordinates;
import org.carlspring.strongbox.db.schema.Edges;
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

    @Inject
    private GenericArtifactCoordinatesArapter genericArtifactCoordinatesArapter;

    @Override
    public EntityTraversal<Vertex, RawArtifactCoordinates> fold()
    {
        return __.<Vertex>hasLabel(Vertices.RAW_ARTIFACT_COORDINATES)
                 .project("uuid", "generic", "coordinates")
                 .by(__.enrichPropertyValue("uuid"))
                 .by(__.enrichPropertyValue("genericArtifactCoordinates"))
                 .by(__.outE(Edges.ARTIFACT_COORDINATES_INHERIT_GENERIC_ARTIFACT_COORDINATES)
                       .mapToObject(__.inV()
                                      .hasLabel(Vertices.GENERIC_ARTIFACT_COORDINATES)
                                      .map(genericArtifactCoordinatesArapter.fold())
                                      .map(EntityTraversalUtils::castToObject))
                       .fold())
                 .map(this::map);
    }

    private RawArtifactCoordinates map(Traverser<Map<String, Object>> t)
    {

        RawArtifactCoordinates result = new RawArtifactCoordinates(
                extractObject(GenericArtifactCoordinatesEntity.class, t.get().get("genericArtifactCoordinates")));
        result.setUuid(extractObject(String.class, t.get().get("uuid")));

        return result;
    }

    @Override
    public EntityTraversal<Vertex, Vertex> unfold(RawArtifactCoordinates entity)
    {
        return null;
    }

    @Override
    public EntityTraversal<Vertex, ? extends Element> cascade()
    {
        return null;
    }

}
