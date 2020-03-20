package org.carlspring.strongbox.gremlin.dsl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sbespalov
 *
 * @param <S>
 * @param <E>
 */
@GremlinDsl
public interface EntityTraversalDsl<S, E> extends GraphTraversal.Admin<S, E>
{

    Logger logger = LoggerFactory.getLogger(EntityTraversalDsl.class);

    Object NULL = new Object()
    {

        @Override
        public String toString()
        {
            return "__null";
        }

    };

    @SuppressWarnings("unchecked")
    default GraphTraversal<S, Vertex> findById(Object uuid,
                                               String... labels)
    {
        GraphTraversal<S, Vertex> result = (GraphTraversal<S, Vertex>) has(labels[0], "uuid", uuid);
        for (String label : Arrays.copyOfRange(labels, 1, labels.length))
        {
            result = result.fold().choose(Collection::isEmpty, __.V().has(label, "uuid", uuid), __.unfold());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    default Traversal<S, Object> enrichPropertyValue(String propertyName)
    {
        return coalesce(__.properties(propertyName).value(), __.<Object>constant(NULL));
    }

    @SuppressWarnings("unchecked")
    default Traversal<S, Object> enrichPropertyValues(String propertyName)
    {
        return coalesce(__.propertyMap(propertyName).map(t -> t.get().get(propertyName)), __.<Object>constant(NULL));
    }

    default <S2> Traversal<S, Object> mapToObject(Traversal<S2, Object> enrichObjectTraversal)
    {
        return fold().choose(t -> t.isEmpty(),
                             __.<Object>constant(NULL),
                             __.<Edge>unfold().map(enrichObjectTraversal));
    }

    default <S2> Traversal<S, Vertex> saveV(String label,
                                            Object uuid,
                                            Traversal<S2, Vertex> unfoldTraversal)
    {
        uuid = Optional.ofNullable(uuid)
                       .orElse(NULL);
        return hasLabel(label).has("uuid", uuid)
                              .fold()
                              .choose(Collection::isEmpty,
                                      __.addV(label)
                                        .property("uuid",
                                                  Optional.of(uuid)
                                                          .filter(x -> !NULL.equals(x))
                                                          .orElse(UUID.randomUUID().toString()))
                                        .property("created", System.currentTimeMillis())
                                        .trace("Created"),
                                      __.unfold()
                                        .trace("Fetched"))
                              .map(unfoldTraversal);
    }

    @SuppressWarnings("unchecked")
    default <E2> Traversal<S, E2> trace(String action)
    {
        return (Traversal<S, E2>) sideEffect(t -> logger.info(String.format("%s [%s]-[%s]-[%s]",
                                                                            action,
                                                                            ((Element) t.get()).label(),
                                                                            ((Element) t.get()).id(),
                                                                            ((Element) t.get()).property("uuid")
                                                                                               .orElse("null"))));
    }

}
