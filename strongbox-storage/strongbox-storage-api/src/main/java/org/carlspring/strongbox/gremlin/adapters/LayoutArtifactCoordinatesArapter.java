package org.carlspring.strongbox.gremlin.adapters;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;

/**
 * @author sbespalov
 *
 */
public abstract class LayoutArtifactCoordinatesArapter extends VertexEntityTraversalAdapter<ArtifactCoordinates>
{

    @Override
    public EntityTraversal<Vertex, ArtifactCoordinates> fold()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityTraversal<Vertex, Vertex> unfold(ArtifactCoordinates entity)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityTraversal<Vertex, ? extends Element> cascade()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
