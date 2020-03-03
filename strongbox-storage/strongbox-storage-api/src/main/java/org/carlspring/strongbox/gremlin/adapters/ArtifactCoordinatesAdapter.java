package org.carlspring.strongbox.gremlin.adapters;

import java.util.Iterator;
import java.util.Optional;
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

        return __.map(fold(Optional.empty(), artifactCoordinatesArapters.iterator()));
    }

    private EntityTraversal<ArtifactCoordinates, ArtifactCoordinates> fold(Optional<EntityTraversal<Vertex, Object>> artifactCoordinatesTraversal,
                                                                           Iterator<LayoutArtifactCoordinatesArapter> iterator)
    {
        if (!iterator.hasNext())
        {
            return __.constant(null);
        }

        LayoutArtifactCoordinatesArapter layoutArtifactCoordinatesAdapter = iterator.next();
        return __.<ArtifactCoordinates>optional(layoutArtifactCoordinatesAdapter.fold(artifactCoordinatesTraversal.orElse(layoutArtifactCoordinatesAdapter.genericArtifactCoordinatesProjection())))
                 .choose(t -> t instanceof ArtifactCoordinates,
                         __.identity(),
                         fold(artifactCoordinatesTraversal, iterator));
    }

    <S> EntityTraversal<S, ArtifactCoordinates> fold(EntityTraversal<Vertex, Object> artifactCoordinatesTraversal)
    {
        return __.map(fold(Optional.of(artifactCoordinatesTraversal), artifactCoordinatesArapters.iterator()));
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
