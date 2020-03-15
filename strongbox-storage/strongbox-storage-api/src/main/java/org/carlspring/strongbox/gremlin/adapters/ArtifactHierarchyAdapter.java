package org.carlspring.strongbox.gremlin.adapters;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;

/**
 * @author sbespalov
 */
public interface ArtifactHierarchyAdapter<T extends Artifact> extends EntityTraversalAdapter<Vertex, T>
{

    EntityTraversal<Vertex, T> foldHierarchy(EntityTraversal<Vertex, Object> parentProjection,
                                             EntityTraversal<Vertex, Object> childProjection);

    EntityTraversal<Vertex, Object> parentProjection();
    
    EntityTraversal<Vertex, Object> childProjection();

    Class<? extends T> entityClass();

}
