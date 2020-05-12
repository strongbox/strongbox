package org.carlspring.strongbox.gremlin.adapters;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.data.domain.DomainObject;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;

/**
 * @author sbespalov
 */
public interface EntityUpwardHierarchyNodeAdapter<E extends DomainObject> extends EntityTraversalAdapter<Vertex, E>
{

    EntityTraversal<Vertex, E> foldHierarchy(EntityTraversal<Vertex, Object> childProjection);

    Class<? extends E> entityClass();

}
