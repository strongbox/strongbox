package org.carlspring.strongbox.gremlin.adapters;

import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.extractObject;
import static org.carlspring.strongbox.gremlin.dsl.EntityTraversalDsl.NULL;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.GenericArtifactCoordinatesEntity;
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
    private GenericArtifactCoordinatesAdapter genericArtifactCoordinatesAdapter;

    private final String layoutCoorinatesLabel;
    private final Class<C> layoutCoordinatesClass;
    
    public LayoutArtifactCoordinatesAdapter(String label,
                                            Class<C> layoutCoordinatesClass)
    {
        this.layoutCoorinatesLabel = label;
        this.layoutCoordinatesClass = layoutCoordinatesClass;
    }

    @Override
    public Set<String> labels()
    {
        return Collections.singleton(layoutCoorinatesLabel);
    }

    @Override
    public Class<C> entityClass()
    {
        return layoutCoordinatesClass;
    }
    
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
                                .map(genericArtifactCoordinatesAdapter.foldHierarchy(genericArtifactCoordinatesAdapter.parentProjection(),
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
    public EntityTraversal<Vertex, C> foldHierarchy(EntityTraversal<Vertex, Object> parentProjection,
                                                                           EntityTraversal<Vertex, Object> childProjection)
    {
        return __.<Vertex>hasLabel(layoutCoorinatesLabel)
                 .project("id", "uuid", "genericArtifactCoordinates")
                 .by(__.id())
                 .by(__.enrichPropertyValue("uuid"))
                 .by(parentProjection)
                 .map(this::map);
    }

    private C map(Traverser<Map<String, Object>> t)
    {
        GenericArtifactCoordinatesEntity genericArtifactCoordinates = extractObject(GenericArtifactCoordinatesEntity.class,
                                                                                    t.get()
                                                                                     .get("genericArtifactCoordinates"));
        C result;
        try
        {
            result = newInstance(genericArtifactCoordinates);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
        result.setNativeId(extractObject(Long.class, t.get().get("id")));
        result.setUuid(extractObject(String.class, t.get().get("uuid")));

        return result;
    }

    private C newInstance(GenericArtifactCoordinatesEntity genericArtifactCoordinates)
        throws InstantiationException,
        IllegalAccessException,
        InvocationTargetException,
        NoSuchMethodException
    {
        C result;
        if (genericArtifactCoordinates == null)
        {
            result = layoutCoordinatesClass.newInstance();
        }
        else
        {
            result = layoutCoordinatesClass.getDeclaredConstructor(GenericArtifactCoordinatesEntity.class).newInstance(genericArtifactCoordinates);
            genericArtifactCoordinates.setHierarchyChild(result);
        }
        return result;
    }
    
    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(C entity)
    {
        return new UnfoldEntityTraversal<>(layoutCoorinatesLabel, entity, __.identity());
    }
    
    @Override
    public EntityTraversal<Vertex, Element> cascade()
    {
        return null;
    }

}
