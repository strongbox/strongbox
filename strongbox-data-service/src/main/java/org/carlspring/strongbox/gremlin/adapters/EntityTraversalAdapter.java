package org.carlspring.strongbox.gremlin.adapters;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.carlspring.strongbox.data.domain.DomainObject;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;

public interface EntityTraversalAdapter<S, E extends DomainObject>
{

    String getLabel();
    
    EntityTraversal<S, E> fold();

    EntityTraversal<S, S> unfold(E entity);
    
    EntityTraversal<S, ? extends Element> cascade();
}