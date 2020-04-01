package org.carlspring.strongbox.gremlin.adapters;

import static org.apache.tinkerpop.gremlin.process.traversal.P.within;
import static org.carlspring.strongbox.gremlin.dsl.EntityTraversalDsl.NULL;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.data.domain.DomainObject;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.__;

/**
 * @author sbespalov
 */
public abstract class EntityHierarchyAdapter<E extends DomainObject, A extends EntityHierarchyNodeAdapter<?>>
        extends VertexEntityTraversalAdapter<E>
{

    protected Map<String, A> adaptersMap;

    @Inject
    public void setArtifactArapters(Set<A> artifactArapters)
    {
        adaptersMap = artifactArapters.stream()
                                      .sorted((a1,
                                               a2) -> a1.entityClass().isAssignableFrom(a2.entityClass()) ? 1 : -1)
                                      .collect(Collectors.toMap(this::validateAndGetLabel,
                                                                (a) -> a,
                                                                (u,
                                                                 v) -> {
                                                                    throw new IllegalStateException(
                                                                            String.format("Duplicate key %s", u));
                                                                }, LinkedHashMap::new));
    }

    private String validateAndGetLabel(A adapterItem)
    {
        return Optional.of(adapterItem)
                       .filter(a -> a.labels().size() == 1)
                       .flatMap(a -> a.labels().stream().findFirst())
                       .orElseThrow(() -> new IllegalArgumentException(
                               String.format("The [%s] component must have only one label, but there was many instead [%s].",
                                             ArtifactCoordinatesHierarchyAdapter.class.getSimpleName(), adapterItem.labels())));
    }

    @Override
    public Set<String> labels()
    {
        return adaptersMap.keySet();
    }

    protected EntityHierarchyNodeAdapter<E> getRootAdapter()
    {
        return (EntityHierarchyNodeAdapter<E>) adaptersMap.values()
                                                          .stream()
                                                          .reduce((first,
                                                                   second) -> second)
                                                          .get();
    }

    @Override
    public EntityTraversal<Vertex, E> fold()
    {

        return __.map(fold(null, adaptersMap.values().iterator()));
    }

    private EntityTraversal<Vertex, E> fold(Supplier<EntityTraversal<Vertex, Object>> parentProjectionSupplier,
                                            Iterator<A> iterator)
    {
        if (!iterator.hasNext())
        {
            return __.<Vertex>V(-1).constant(null);
        }

        A nextAdapter = iterator.next();
        EntityTraversal<Vertex, Object> parentProjection = Optional.ofNullable(parentProjectionSupplier)
                                                                   .map(Supplier::get)
                                                                   .orElseGet(nextAdapter::parentProjection);
        EntityTraversal<Vertex, ?> nextTraversal = nextAdapter.foldHierarchy(parentProjection, __.<Vertex>identity().constant(NULL));

        return __.<Vertex>identity()
                 .optional(__.hasLabel(within(nextAdapter.labels()))
                             .map(nextTraversal))
                 .choose((e) -> nextAdapter.entityClass().isInstance(e),
                         __.identity(),
                         fold(parentProjectionSupplier, iterator));
    }

    <S> EntityTraversal<S, E> fold(Supplier<EntityTraversal<Vertex, Object>> artifactTraversal)
    {
        return __.map(fold(artifactTraversal,
                           adaptersMap.values().iterator()));
    }

    protected abstract String hierarchyEdge();

    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(E entity)
    {
        UnfoldEntityTraversal<Vertex, Vertex> unfoldTraversal = adaptersMap.values()
                                                                           .stream()
                                                                           .map(EntityHierarchyNodeAdapter.class::cast)
                                                                           .filter(a -> a.entityClass().isInstance(entity))
                                                                           .findFirst()
                                                                           .map(a -> a.unfold((E) entity))
                                                                           .get();

        if (getRootAdapter().labels().contains(unfoldTraversal.entityLabel()))
        {
            return unfoldTraversal;
        }

        return new UnfoldEntityTraversal<>(unfoldTraversal.entityLabel(),
                __.<Vertex, Edge>coalesce(updateParent(entity),
                                          createParent(entity))
                  .outV()
                  .map(unfoldTraversal));
    }

    private Traversal<?, Edge> createParent(E artifact)
    {
        return __.<Vertex>addE(hierarchyEdge())
                 .from(__.identity())
                 .to(saveParent(artifact));
    }

    private Traversal<Vertex, Edge> updateParent(E artifact)
    {
        return __.outE(hierarchyEdge())
                 .as(hierarchyEdge())
                 .inV()
                 .map(saveParent(artifact))
                 .select(hierarchyEdge());
    }

    private <S> EntityTraversal<S, Vertex> saveParent(E artifact)
    {
        String rootLabel = getRootAdapter().labels().stream().findFirst().get();
        return __.<S>V()
                 .saveV(rootLabel,
                        artifact.getUuid(),
                        getRootAdapter().unfold(artifact));
    }

}