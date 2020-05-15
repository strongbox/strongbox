package org.carlspring.strongbox.gremlin.adapters;

import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.extractObject;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
        return __.map(fold(adaptersHierarchy.descendingIterator(),
                           maxHierarchyDepth));
    }

    private EntityTraversal<?, E> fold(Iterator<A> iterator,
                                       int depth)
    {
        if (!iterator.hasNext())
        {
            return __.<Vertex>V(-1).constant(null);
        }

        A nextAdapter = iterator.next();
        EntityTraversal<Vertex, E> nextTraversal = nextAdapter.fold();
        if (depth == 0)
        {
            return __.choose(__.hasLabel(nextAdapter.label()),
                             __.map(nextTraversal),
                             fold(iterator, depth));
        }

        EntityTraversal<Vertex, E> childTraversal = __.inE(Edges.EXTENDS)
                                                      .outV()
                                                      .map(fold(adaptersHierarchy.descendingIterator(),
                                                                depth - 1));

        return __.choose(__.hasLabel(nextAdapter.label()),
                         __.project("parent", "child")
                           .by(nextTraversal)
                           .by(childTraversal)
                           .sideEffect(this::parentWithChild)
                           .select("child"),
                         fold(iterator, depth));
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
        for (E entityHierarchyNode : hierarchy)
        {
            for (A adapter : adaptersHierarchy)
            {
                if (!adapter.entityClass().isAssignableFrom(entityHierarchyNode.getClass()))
                {
                    continue;
                }
                else if (result == null)
                {
                    result = adapter.unfold(entityHierarchyNode);
                }
                else
                {

                    result = result.choose(__.inE(Edges.EXTENDS),
                                           // Update child
                                           __.inE(Edges.EXTENDS)
                                             .outV()
                                             .saveV(entityHierarchyNode.getUuid(), adapter.unfold(entityHierarchyNode)),
                                           // Create child
                                           __.addE(Edges.EXTENDS)
                                             .from(__.saveV(entityHierarchyNode.getUuid(),
                                                            adapter.unfold(entityHierarchyNode)))
                                             .outV());
                }
                break;
            }
        }

        String entityLabel = rootAdapter.label();
        return new UnfoldEntityTraversal<>(entityLabel, entity, result);
    }

}