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
    public Class<? extends RawArtifactCoordinates> entityClass()
    {
        return RawArtifactCoordinates.class;
    }

    @Override
    public EntityTraversal<Vertex, RawArtifactCoordinates> foldHierarchy(EntityTraversal<Vertex, Object> parentProjection,
                                                                         EntityTraversal<Vertex, Object> childProjection)
    {
        return __.<Vertex>hasLabel(Vertices.RAW_ARTIFACT_COORDINATES)
                 .project("id", "uuid", "genericArtifactCoordinates")
                 .by(__.id())
                 .by(__.enrichPropertyValue("uuid"))
                 .by(parentProjection)
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
        result.setNativeId(extractObject(Long.class, t.get().get("id")));
        result.setUuid(extractObject(String.class, t.get().get("uuid")));

        return result;
    }

    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(RawArtifactCoordinates entity)
    {
        return new UnfoldEntityTraversal<>(Vertices.RAW_ARTIFACT_COORDINATES, __.identity());
    }

}
