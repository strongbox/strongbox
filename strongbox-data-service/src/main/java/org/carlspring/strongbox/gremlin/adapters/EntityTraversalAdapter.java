package org.carlspring.strongbox.gremlin.adapters;

import java.util.Set;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.carlspring.strongbox.data.domain.DomainObject;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;

public interface EntityTraversalAdapter<S extends Element, E extends DomainObject>
{

    Set<String> labels();

    EntityTraversal<S, E> fold();

    UnfoldTraversal<S> unfold(E entity);

    EntityTraversal<S, ? extends Element> cascade();

    public static class UnfoldTraversal<S extends Element>
    {

        private final String label;
        private final EntityTraversal<S, S> traversal;

        public UnfoldTraversal(String label,
                               EntityTraversal<S, S> traversal)
        {
            this.label = label;
            this.traversal = traversal;
        }

        public String getLabel()
        {
            return label;
        }

        public EntityTraversal<S, S> getTraversal()
        {
            return traversal;
        }

    }
    
}