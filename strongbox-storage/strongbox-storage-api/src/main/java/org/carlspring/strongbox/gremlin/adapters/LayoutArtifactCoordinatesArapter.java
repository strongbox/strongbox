package org.carlspring.strongbox.gremlin.adapters;

import org.carlspring.strongbox.domain.LayoutArtifactCoordinatesEntity;

/**
 * @author sbespalov
 */
public abstract class LayoutArtifactCoordinatesArapter<C extends LayoutArtifactCoordinatesEntity<C, V>, V extends Comparable<V>>
        extends VertexEntityTraversalAdapter<C>
{

}
