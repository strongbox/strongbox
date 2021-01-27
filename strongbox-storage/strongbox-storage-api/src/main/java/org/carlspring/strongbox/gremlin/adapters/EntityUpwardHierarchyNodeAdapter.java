package org.carlspring.strongbox.gremlin.adapters;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.data.domain.DomainObject;

/**
 * @author sbespalov
 */
public interface EntityUpwardHierarchyNodeAdapter<E extends DomainObject> extends EntityTraversalAdapter<Vertex, E>
{

    Class<? extends E> entityClass();

}
