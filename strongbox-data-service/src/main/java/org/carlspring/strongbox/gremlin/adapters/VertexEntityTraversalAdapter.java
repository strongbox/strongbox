package org.carlspring.strongbox.gremlin.adapters;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.data.domain.DomainObject;

/**
 * {@link EntityTraversalAdapter} for entities associated with vertices.
 *
 * @param <E>
 *
 * @author sbespalov
 */
public interface VertexEntityTraversalAdapter<E extends DomainObject> extends EntityTraversalAdapter<Vertex, E>
{

}
