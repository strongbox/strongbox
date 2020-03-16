package org.carlspring.strongbox.gremlin.adapters;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.data.domain.DomainObject;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;

/**
 * @author sbespalov
 */
public interface EntityHierarchyNodeAdapter<T extends DomainObject> extends EntityTraversalAdapter<Vertex, T>
{

    EntityTraversal<Vertex, T> foldHierarchy(EntityTraversal<Vertex, Object> parentProjection,
                                             EntityTraversal<Vertex, Object> childProjection);

    EntityTraversal<Vertex, Object> parentProjection();

    EntityTraversal<Vertex, Object> childProjection();

    Class<? extends T> entityClass();

}
