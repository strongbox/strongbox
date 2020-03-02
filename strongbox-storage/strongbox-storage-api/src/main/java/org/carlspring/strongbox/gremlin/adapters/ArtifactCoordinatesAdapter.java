package org.carlspring.strongbox.gremlin.adapters;

import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.__;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 */
@Component
public class ArtifactCoordinatesAdapter extends VertexEntityTraversalAdapter<ArtifactCoordinates>
{

    @Inject
    private Set<LayoutArtifactCoordinatesArapter> artifactCoordinatesArapters;

    @Override
    public EntityTraversal<Vertex, ArtifactCoordinates> fold()
    {

        return __.map(fold(artifactCoordinatesArapters.iterator()));
    }

    private EntityTraversal<ArtifactCoordinates, ArtifactCoordinates> fold(Iterator<LayoutArtifactCoordinatesArapter> iterator)
    {
        if (!iterator.hasNext())
        {
            return __.constant(null);
        }
        return __.<ArtifactCoordinates>optional(iterator.next().fold())
                 .fold()
                 .choose(t -> t.isEmpty(),
                         fold(iterator),
                         __.<ArtifactCoordinates>unfold());
    }

    @Override
    public EntityTraversal<Vertex, Vertex> unfold(ArtifactCoordinates entity)
    {
        return null;
    }

    @Override
    public EntityTraversal<Vertex, ? extends Element> cascade()
    {
        return null;
    }

}
