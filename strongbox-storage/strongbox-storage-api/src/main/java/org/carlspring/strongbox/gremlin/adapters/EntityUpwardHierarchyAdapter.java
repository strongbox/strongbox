package org.carlspring.strongbox.gremlin.adapters;

import static org.carlspring.strongbox.gremlin.dsl.EntityTraversalUtils.extractObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.data.domain.DomainObject;
import org.carlspring.strongbox.data.domain.EntityHierarchyNode;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.__;

/**
 * @author sbespalov
 */
public abstract class EntityUpwardHierarchyAdapter<E extends DomainObject & EntityHierarchyNode, A extends EntityUpwardHierarchyNodeAdapter<E>>
        extends VertexEntityTraversalAdapter<E>
{

    private final int maxHierarchyDepth;

    /**
     * EntityUpwardHierarchyNodeAdapters mapped by Vertex label and sorted in
     * descending order by entity class hierarchy.
     */
    protected final Deque<A> adaptersHierarchy;

    public EntityUpwardHierarchyAdapter(Set<A> artifactArapters,
                                        int maxHierarchyDepth)
    {
        this.maxHierarchyDepth = maxHierarchyDepth;
        adaptersHierarchy = artifactArapters.stream()
                                            .sorted((a1,
                                                     a2) -> a1.entityClass().isAssignableFrom(a2.entityClass()) ? 1 : -1)
                                            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public String label()
    {
        return getRootAdapter().label();
    }

    /**
     * Gets the entity hierarchy root class adapter.
     * 
     * @return
     */
    protected EntityUpwardHierarchyNodeAdapter<E> getRootAdapter()
    {
        return adaptersHierarchy.descendingIterator().next();
    }

    @Override
    public EntityTraversal<Vertex, E> fold()
    {
        Optional<EntityTraversal<?, E>> upwardTraversal = fold(Collections.singleton((A) getRootAdapter()).iterator(),
                                                               maxHierarchyDepth);
        return __.map(upwardTraversal.get());
    }

    private Optional<EntityTraversal<?, E>> fold(Iterator<A> iterator,
                                                 int depth)
    {
        if (!iterator.hasNext())
        {
            return Optional.empty();
        }

        A nextAdapter = iterator.next();
        EntityTraversal<Vertex, E> nextTraversal = nextAdapter.fold();
        Optional<EntityTraversal<?, E>> upwardTraversal = fold(iterator, depth);

        if (upwardTraversal.isPresent())
        {
            // Get child iterator
            Iterator<A> childIterator = adaptersHierarchy.descendingIterator();
            // Skip root adapter
            childIterator.next();
            // Get child traversal
            if (depth == 0)
            {
                return Optional.of(__.choose(t -> nextAdapter.label().equals(((Vertex) t).label()),
                                             __.map(nextTraversal),
                                             upwardTraversal.get()));
            }
            Optional<EntityTraversal<Vertex, E>> childTraversal = fold(childIterator, depth - 1).map(t -> __.inE(Edges.EXTENDS)
                                                                                                            .outV()
                                                                                                            .map(t));
            if (!childTraversal.isPresent())
            {
                return Optional.of(__.choose(t -> nextAdapter.label().equals(((Vertex) t).label()),
                                             __.map(nextTraversal),
                                             upwardTraversal.get()));
            }

            return Optional.of(__.choose(t -> nextAdapter.label().equals(((Vertex) t).label()),
                                         __.project("parent", "child")
                                           .by(nextTraversal)
                                           .by(childTraversal.get())
                                           .sideEffect(this::parentWithChild)
                                           .select("child"),
                                         upwardTraversal.get()));
        }
        else
        {
            // Get child iterator
            Iterator<A> childIterator = adaptersHierarchy.descendingIterator();
            // Skip root adapter
            childIterator.next();
            // Get child traversal
            if (depth == 0)
            {
                return Optional.of(__.map(nextTraversal));
            }
            Optional<EntityTraversal<Vertex, E>> childTraversal = fold(childIterator, depth - 1).map(t -> __.inE(Edges.EXTENDS)
                                                                                                            .outV()
                                                                                                            .map(t));
            if (!childTraversal.isPresent())
            {
                return Optional.of(__.map(nextTraversal));
            }

            return Optional.of(__.project("parent", "child")
                                 .by(nextTraversal)
                                 .by(childTraversal.get())
                                 .sideEffect(this::parentWithChild)
                                 .select("child"));
        }
    }

    private void parentWithChild(Traverser<Map<String, Object>> t)
    {
        EntityHierarchyNode parent = extractObject(EntityHierarchyNode.class, t.get().get("parent"));
        EntityHierarchyNode child = extractObject(EntityHierarchyNode.class, t.get().get("child"));

        parent.setHierarchyChild(child);
        child.setHierarchyParent(parent);
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

        EntityTraversal<Vertex, Vertex> result = null;
        for (E hierarchyNode : hierarchy)
        {
            for (A hierarchyNodeAdapter : adaptersHierarchy)
            {
                if (!hierarchyNodeAdapter.entityClass().isAssignableFrom(hierarchyNode.getClass()))
                {
                    continue;
                }
                else if (result == null)
                {
                    result = hierarchyNodeAdapter.unfold(hierarchyNode);
                }
                else
                {
                    result = result.optional(__.inE(Edges.EXTENDS)
                                               .outV())
                                   .choose(__.hasLabel(hierarchyNodeAdapter.label()),
                                           __.saveV(hierarchyNode.getUuid(), hierarchyNodeAdapter.unfold(hierarchyNode)),
                                           __.addE(Edges.EXTENDS)
                                             .from(__.addV(hierarchyNode.getUuid(),
                                                           hierarchyNodeAdapter.unfold(hierarchyNode)))
                                             .outV());
                }
                break;
            }
        }
        // Move back to root vertex
        for (int i = 0; i < hierarchy.size() - 1; i++)
        {
            result = result.outE(Edges.EXTENDS).inV();
        }

        return new UnfoldEntityTraversal<>(rootAdapter.label(), entity, result);
    }

}