package org.carlspring.strongbox.gremlin.adapters;

import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;
import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.extractObject;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.artifact.coordinates.GenericArtifactCoordinates;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.domain.GenericArtifactCoordinatesEntity;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.__;

/**
 * @author sbespalov
 *
 */
public class GenericArtifactCoordinatesArapter extends VertexEntityTraversalAdapter<GenericArtifactCoordinates>
{

    @Override
    public EntityTraversal<Vertex, GenericArtifactCoordinates> fold()
    {
        return __.<Vertex, Object>project("uuid", "version", "coordinates")
                 .by(__.enrichPropertyValue("uuid"))
                 .by(__.enrichPropertyValue("version"))
                 .by(__.propertyMap())
                 .map(this::map);
    }

    private GenericArtifactCoordinates map(Traverser<Map<String, Object>> t)
    {
        GenericArtifactCoordinatesEntity result = new GenericArtifactCoordinatesEntity();
        result.setUuid(extractObject(String.class, t.get().get("uuid")));
        result.setVersion(extractObject(String.class, t.get().get("version")));

        Map<String, Object> coordinates = (Map<String, Object>) t.get().get("coordinates");
        coordinates.entrySet().stream().forEach(e -> result.setCoordinate(e.getKey(), (String) e.getValue()));

        return result;
    }

    @Override
    public EntityTraversal<Vertex, Vertex> unfold(GenericArtifactCoordinates entity)
    {
        EntityTraversal<Vertex, Vertex> t = __.<Vertex>identity();
        t = t.property(single, "version", entity.getVersion());

        for (Entry<String, String> coordinateEntry : entity.getCoordinates().entrySet())
        {
            t = t.property(single, coordinateEntry.getKey(), coordinateEntry.getValue());
        }

        return t;
    }

    @Override
    public EntityTraversal<Vertex, ? extends Element> cascade()
    {
        return __.<Vertex>aggregate("x")
                 // cascade by all incoming Artifacts
                 .inE(Edges.ARTIFACT_HAS_ARTIFACT_COORDINATES)
                 .outV()
                 .aggregate("x")
                 .select("x")
                 .unfold();
    }

}
