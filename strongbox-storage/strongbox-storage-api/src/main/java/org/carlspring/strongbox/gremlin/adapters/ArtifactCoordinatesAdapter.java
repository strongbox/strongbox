package org.carlspring.strongbox.gremlin.adapters;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.GenericArtifactCoordinates;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.__;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 */
@Component
public class ArtifactCoordinatesAdapter extends VertexEntityTraversalAdapter<ArtifactCoordinates>
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactCoordinatesAdapter.class);

    @Inject
    private Set<LayoutArtifactCoordinatesArapter> artifactCoordinatesArapters;

    @Inject
    private GenericArtifactCoordinatesArapter genericArtifactCoordinatesArapter;

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
        GenericArtifactCoordinates genericArtifactCoordinates = entity;

        return __.<Vertex, Edge>coalesce(updateGenericArtifactCoordinates(genericArtifactCoordinates),
                                         createGenericArtifactCoordinates(genericArtifactCoordinates))
                 .outV();
    }

    private Traversal<?, Edge> createGenericArtifactCoordinates(GenericArtifactCoordinates genericArtifactCoordinates)
    {
        return __.<Vertex>addE(Edges.ARTIFACT_COORDINATES_INHERIT_GENERIC_ARTIFACT_COORDINATES)
                 .from(__.identity())
                 .to(saveGenericArtifactCoordinates(genericArtifactCoordinates));
    }

    private Traversal<Vertex, Edge> updateGenericArtifactCoordinates(GenericArtifactCoordinates genericArtifactCoordinates)
    {
        return __.outE(Edges.ARTIFACT_COORDINATES_INHERIT_GENERIC_ARTIFACT_COORDINATES)
                 .as(Edges.ARTIFACT_COORDINATES_INHERIT_GENERIC_ARTIFACT_COORDINATES)
                 .inV()
                 .map(saveGenericArtifactCoordinates(genericArtifactCoordinates))
                 .select(Edges.ARTIFACT_COORDINATES_INHERIT_GENERIC_ARTIFACT_COORDINATES);
    }

    private <S> EntityTraversal<S, Vertex> saveGenericArtifactCoordinates(GenericArtifactCoordinates genericArtifactCoordinates)
    {
        return __.<S>V()
                 .saveV(Vertices.GENERIC_ARTIFACT_COORDINATES,
                        genericArtifactCoordinates.getUuid(),
                        genericArtifactCoordinatesArapter.unfold(genericArtifactCoordinates));
    }

    @Override
    public EntityTraversal<Vertex, ? extends Element> cascade()
    {
        return __.<Vertex>aggregate("x")
                 .outE(Edges.ARTIFACT_COORDINATES_INHERIT_GENERIC_ARTIFACT_COORDINATES)
                 .inV()
                 .map(genericArtifactCoordinatesArapter.cascade())
                 .select("x")
                 .unfold();
    }

}
