package org.carlspring.strongbox.gremlin.adapters;

import static org.apache.tinkerpop.gremlin.process.traversal.P.within;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.GenericArtifactCoordinates;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.LayoutArtifactCoordinatesEntity;
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

    private Map<String, LayoutArtifactCoordinatesArapter<?, ?>> artifactCoordinatesAraptersMap;

    @Inject
    private GenericArtifactCoordinatesArapter genericArtifactCoordinatesArapter;

    @Inject
    public void setArtifactCoordinatesArapters(Set<LayoutArtifactCoordinatesArapter<?, ?>> artifactCoordinatesArapters)
    {
        artifactCoordinatesAraptersMap = artifactCoordinatesArapters.stream()
                                                                    .collect(Collectors.toMap(this::validateAndGetLabel,
                                                                                              (a) -> a));
    }

    private String validateAndGetLabel(LayoutArtifactCoordinatesArapter<?, ?> adapterItem)
    {
        return Optional.of(adapterItem)
                       .filter(a -> a.labels().size() == 1)
                       .flatMap(a -> a.labels().stream().findFirst())
                       .orElseThrow(() -> new IllegalArgumentException(
                               String.format("The [%s] component must have only one label, but there was many instead [%s].",
                                             ArtifactCoordinatesAdapter.class.getSimpleName(), adapterItem.labels())));
    }

    @Override
    public Set<String> labels()
    {
        return artifactCoordinatesAraptersMap.keySet();
    }

    @Override
    public EntityTraversal<Vertex, ArtifactCoordinates> fold()
    {

        return __.map(fold(Optional.empty(), artifactCoordinatesAraptersMap.values().iterator()));
    }

    private EntityTraversal<ArtifactCoordinates, ArtifactCoordinates> fold(Optional<EntityTraversal<Vertex, Object>> optionalGenericArtifactCoordinatesProjection,
                                                                           Iterator<LayoutArtifactCoordinatesArapter<?, ?>> iterator)
    {
        if (!iterator.hasNext())
        {
            return __.constant(null);
        }

        LayoutArtifactCoordinatesArapter<?, ?> nextAdapter = iterator.next();
        EntityTraversal<Vertex, Object> defaultGenericArtifactCoordinatesProjection = nextAdapter.genericArtifactCoordinatesProjection();
        EntityTraversal<Vertex, Object> genericArtifactCoordinatesProjection = optionalGenericArtifactCoordinatesProjection.orElse(defaultGenericArtifactCoordinatesProjection);
        EntityTraversal<Vertex, ?> nextTraversal = nextAdapter.fold(genericArtifactCoordinatesProjection);

        return __.<ArtifactCoordinates>optional(__.hasLabel(within(nextAdapter.labels()))
                                                  .map(nextTraversal)
                                                  .map(t -> ArtifactCoordinates.class.cast(t.get())))
                 .choose(ArtifactCoordinates.class::isInstance,
                         __.identity(),
                         fold(optionalGenericArtifactCoordinatesProjection, iterator));
    }

    <S> EntityTraversal<S, ArtifactCoordinates> fold(EntityTraversal<Vertex, Object> artifactCoordinatesTraversal)
    {
        return __.map(fold(Optional.of(artifactCoordinatesTraversal),
                           artifactCoordinatesAraptersMap.values().iterator()));
    }

    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(ArtifactCoordinates entity)
    {
        UnfoldEntityTraversal<Vertex, Vertex> unfoldTraversal = artifactCoordinatesAraptersMap.values()
                                                                                              .stream()
                                                                                              .map(LayoutArtifactCoordinatesArapter.class::cast)
                                                                                              .map(a -> a.unfold((LayoutArtifactCoordinatesEntity<?, ?>) entity))
                                                                                              .filter(Objects::nonNull)
                                                                                              .findFirst()
                                                                                              .get();

        return new UnfoldEntityTraversal<>(unfoldTraversal.entityLabel(),
                __.<Vertex, Edge>coalesce(updateGenericArtifactCoordinates(entity),
                                          createGenericArtifactCoordinates(entity))
                  .outV()
                  .map(unfoldTraversal));
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
