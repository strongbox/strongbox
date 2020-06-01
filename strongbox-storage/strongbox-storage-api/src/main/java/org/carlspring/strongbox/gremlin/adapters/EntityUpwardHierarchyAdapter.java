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
        Optional<EntityTraversal<Vertex, E>> upwardTraversal = fold(Collections.singleton((A) getRootAdapter()).iterator(),
                                                                    maxHierarchyDepth);
        return __.map(upwardTraversal.get());
    }

    private Optional<EntityTraversal<Vertex, E>> fold(Iterator<A> iterator,
                                                      int depth)
    {
        if (!iterator.hasNext())
        {
            return Optional.empty();
        }

        A nextAdapter = iterator.next();
        EntityTraversal<Vertex, E> result = fold(iterator, depth).map(upwardTraversal -> {
            return foldNode(depth, nextAdapter, Optional.of(upwardTraversal));
        }).orElseGet(() -> {
            return foldTopNode(depth, nextAdapter);
        });

        return Optional.of(result);
    }

    private EntityTraversal<Vertex, E> foldTopNode(int depth,
                                                   A nodeAdapter)
    {
        return foldNode(depth, nodeAdapter, Optional.empty());
    }

    private EntityTraversal<Vertex, E> foldNode(int depth,
                                                A nodeAdapter,
                                                Optional<EntityTraversal<Vertex, E>> upwardTraversal)
    {
        String nodeLabel = nodeAdapter.label();
        EntityTraversal<Vertex, E> nodeTraversal = nodeAdapter.fold();
        EntityTraversal<Vertex, E> topTraversal = upwardTraversal.map(t -> chainUpward(nodeLabel, __.map(nodeTraversal), t))
                                                                 .orElse(__.map(nodeTraversal));
        if (depth == 0)
        {
            return topTraversal;
        }

        Iterator<A> childIterator = getChildIterator();
        return fold(childIterator, depth - 1).map(this::childTraversal)
                                             .map(childTraversal -> upwardTraversal.map(t -> chainUpward(nodeLabel,
                                                                                                         nodeTraversal(nodeTraversal,
                                                                                                                       childTraversal),
                                                                                                         t))
                                                                                   .orElse(nodeTraversal(nodeTraversal,
                                                                                                         childTraversal)))
                                             .orElse(topTraversal);
    }

    private EntityTraversal<Vertex, E> chainUpward(String nodeLabel,
                                                   EntityTraversal<Vertex, E> nodeTraversal,
                                                   EntityTraversal<Vertex, E> upwardTraversal)
    {
        return __.<Vertex, E>choose(t -> nodeLabel.equals(t.label()),
                                    __.map(nodeTraversal),
                                    upwardTraversal);
    }

    private EntityTraversal<Vertex, E> childTraversal(EntityTraversal<Vertex, E> childTraversal)
    {
        return __.inE(Edges.EXTENDS)
                 .outV()
                 .map(childTraversal);
    }

    private EntityTraversal<Vertex, E> nodeTraversal(EntityTraversal<Vertex, E> nodeTraversal,
                                                     EntityTraversal<Vertex, E> childTraversal)
    {
        return __.<Vertex, E>project("parent", "child")
                 .by(nodeTraversal)
                 .by(childTraversal)
                 .sideEffect(this::parentWithChild)
                 .<E>select("child");
    }

    private Iterator<A> getChildIterator()
    {
        // Get child iterator
        Iterator<A> childIterator = adaptersHierarchy.descendingIterator();
        // Skip root adapter
        childIterator.next();

        return childIterator;
    }

    private void parentWithChild(Traverser<Map<String, E>> t)
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