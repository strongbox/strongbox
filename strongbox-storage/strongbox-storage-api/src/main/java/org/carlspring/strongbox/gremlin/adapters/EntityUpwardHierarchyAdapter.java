package org.carlspring.strongbox.gremlin.adapters;

import static org.apache.tinkerpop.gremlin.process.traversal.P.within;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.data.domain.DomainObject;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.__;

/**
 * @author sbespalov
 */
public abstract class EntityUpwardHierarchyAdapter<E extends DomainObject & EntityHierarchyNode, A extends EntityUpwardHierarchyNodeAdapter<E>>
        extends VertexEntityTraversalAdapter<E>
{

    /**
     * EntityUpwardHierarchyNodeAdapters mapped by Vertex label and sorted in
     * descending order by entity class hierarchy.
     */
    protected final Map<String, A> adaptersMap;

    public EntityUpwardHierarchyAdapter(Set<A> artifactArapters)
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
                                                                },
                                                                LinkedHashMap::new));
    }

    private String validateAndGetLabel(A adapterItem)
    {
        return Optional.of(adapterItem)
                       .filter(a -> a.labels().size() == 1)
                       .flatMap(a -> a.labels().stream().findFirst())
                       .orElseThrow(() -> new IllegalArgumentException(
                               String.format("The [%s] component must have only one label, but there was many instead [%s].",
                                             ArtifactCoordinatesHierarchyAdapter.class.getSimpleName(),
                                             adapterItem.labels())));
    }

    @Override
    public Set<String> labels()
    {
        return getRootAdapter().labels();
    }

    /**
     * Gets the entity hierarchy root class adapter.
     * 
     * @return
     */
    protected EntityUpwardHierarchyNodeAdapter<E> getRootAdapter()
    {
        return (EntityUpwardHierarchyNodeAdapter<E>) adaptersMap.values()
                                                                .stream()
                                                                .reduce((first,
                                                                         second) -> second)
                                                                .get();
    }

    @Override
    public EntityTraversal<Vertex, E> fold()
    {

        return __.map(fold(adaptersMap.values().iterator()));
    }

    private EntityTraversal<Vertex, E> fold(Iterator<A> iterator)
    {
        if (!iterator.hasNext())
        {
            return __.<Vertex>V(-1).constant(null);
        }

        A nextAdapter = iterator.next();
        EntityTraversal<Vertex, Object> childTraversal = (EntityTraversal<Vertex, Object>) __.inE(Edges.EXTENDS)
                                                                                             .outV()
                                                                                             .map(fold(adaptersMap.values()
                                                                                                                  .iterator()));
        EntityTraversal<Vertex, E> nextTraversal = nextAdapter.foldHierarchy(childTraversal);

        return __.<Vertex>hasLabel(within(nextAdapter.labels()))
                 .fold()
                 .choose(Collection::isEmpty,
                         fold(iterator),
                         __.unfold()
                           .map(nextTraversal));
    }

    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(E entity)
    {

        List<E> hierarchy = new ArrayList<>();
        hierarchy.add(entity);
        while (entity.getHierarchyParent() != null)
        {
            hierarchy.add(0, entity = (E) entity.getHierarchyParent());
        }
        EntityUpwardHierarchyNodeAdapter<E> rootAdapter = getRootAdapter();
        Class<? extends E> rootEntityType = rootAdapter.entityClass();
        E rootEntity = hierarchy.iterator().next();
        if (!rootEntityType.isAssignableFrom(rootEntity.getClass()))
        {
            throw new IllegalArgumentException(
                    String.format("Invalid hierarchy root type [%s], should be assignable tp [%s].",
                                  rootEntity.getClass(),
                                  rootEntityType));
        }

        A entityHierarchyAdapter = null;
        EntityTraversal<Vertex, Vertex> result = null;
        for (E entityHierarchyNode : hierarchy)
        {
            for (A adapter : adaptersMap.values())
            {
                if (!adapter.entityClass().isAssignableFrom(entityHierarchyNode.getClass()))
                {
                    continue;
                }

                entityHierarchyAdapter = adapter;
                if (result == null)
                {
                    result = adapter.unfold(entityHierarchyNode);
                }
                else
                {

                    result = result.coalesce(// Update child
                                             __.inE(Edges.EXTENDS)
                                               .outV()
                                               .saveV(entityHierarchyNode.getUuid(), adapter.unfold(entityHierarchyNode)),
                                             // Create child
                                             __.addE(Edges.EXTENDS)
                                               .to(__.identity())
                                               .from(__.saveV(entityHierarchyNode.getUuid(), adapter.unfold(entityHierarchyNode)))
                                               .outV());
                }
                break;
            }
        }

        String entityLabel = rootAdapter.labels().iterator().next();
        return new UnfoldEntityTraversal<>(entityLabel, entity, result);
    }

}