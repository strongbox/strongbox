package org.carlspring.strongbox.gremlin.adapters;

import static org.carlspring.strongbox.gremlin.dsl.EntityTraversalDsl.NULL;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.LayoutArtifactCoordinatesEntity;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversalDsl;
import org.carlspring.strongbox.gremlin.dsl.__;

/**
 * @author sbespalov
 */
public abstract class LayoutArtifactCoordinatesAdapter<C extends LayoutArtifactCoordinatesEntity<C, V>, V extends Comparable<V>>
        extends VertexEntityTraversalAdapter<C>
        implements ArtifactCoodrinatesNodeAdapter<C>
{
    @Inject
    private GenericArtifactCoordinatesArapter genericArtifactCoordinatesArapter;

    @Override
    public EntityTraversal<Vertex, C> fold()
    {
        return foldHierarchy(parentProjection(), childProjection());
    }

    @Override
    public EntityTraversal<Vertex, Object> parentProjection()
    {
        return __.outE(Edges.ARTIFACT_COORDINATES_INHERIT_GENERIC_ARTIFACT_COORDINATES)
                 .mapToObject(__.inV()
                                .hasLabel(Vertices.GENERIC_ARTIFACT_COORDINATES)
                                .map(genericArtifactCoordinatesArapter.foldHierarchy(genericArtifactCoordinatesArapter.parentProjection(),
                                                                                     __.<Vertex>identity()
                                                                                       .constant(NULL)))
                                .map(EntityTraversalUtils::castToObject));
    }

    @Override
    public EntityTraversal<Vertex, Object> childProjection()
    {
        return __.<Vertex>identity().constant(EntityTraversalDsl.NULL);
    }

    @Override
    public EntityTraversal<Vertex, Element> cascade()
    {
        return null;
    }

}
