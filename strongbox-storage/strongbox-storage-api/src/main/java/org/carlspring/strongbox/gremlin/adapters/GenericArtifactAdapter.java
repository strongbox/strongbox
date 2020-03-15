package org.carlspring.strongbox.gremlin.adapters;

import static org.apache.tinkerpop.gremlin.process.traversal.P.within;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.__;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.gremlin.dsl.EntityTraversalDsl.NULL;

/**
 * @author sbespalov
 */
@Component
public class GenericArtifactAdapter extends VertexEntityTraversalAdapter<Artifact>
{

    private Map<String, ArtifactHierarchyAdapter<?>> artifactAdaptersMap;

    @Inject
    private ArtifactAdapter artifactAdapter;

    @Inject
    public void setArtifactArapters(Set<ArtifactHierarchyAdapter<?>> artifactArapters)
    {
        artifactAdaptersMap = artifactArapters.stream()
                                              .sorted((a1,
                                                       a2) -> a1.entityClass().isAssignableFrom(a2.entityClass()) ? 1 : -1)
                                              .collect(Collectors.toMap(this::validateAndGetLabel,
                                                                        (a) -> a,
                                                                        (u,
                                                                         v) -> {
                                                                            throw new IllegalStateException(
                                                                                    String.format("Duplicate key %s", u));
                                                                        }, LinkedHashMap::new));
    }

    private String validateAndGetLabel(ArtifactHierarchyAdapter<?> adapterItem)
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
        return artifactAdaptersMap.keySet();
    }

    @Override
    public EntityTraversal<Vertex, Artifact> fold()
    {

        return __.map(fold(Optional.empty(), artifactAdaptersMap.values().iterator()));
    }

    private EntityTraversal<Vertex, Artifact> fold(Optional<EntityTraversal<Vertex, ?>> parentProjection,
                                                   Iterator<ArtifactHierarchyAdapter<?>> iterator)
    {
        if (!iterator.hasNext())
        {
            return __.<Vertex>V().constant(null);
        }

        ArtifactHierarchyAdapter<?> nextAdapter = iterator.next();
        EntityTraversal<Vertex, Object> vParentProjection = parentProjection.map(EntityTraversal.class::cast)
                                                                            .orElseGet(nextAdapter::parentProjection);
        EntityTraversal<Vertex, ?> nextTraversal = nextAdapter.foldHierarchy(vParentProjection, __.<Vertex>V().constant(NULL));

        return __.<Vertex>identity()
                 .optional(__.hasLabel(within(nextAdapter.labels()))
                             .map(nextTraversal)
                             .map(t -> nextAdapter.entityClass().cast(t.get())))
                 .choose((e) -> nextAdapter.entityClass().isInstance(e),
                         __.identity(),
                         fold(parentProjection, iterator));
    }

    <S> EntityTraversal<S, Artifact> fold(EntityTraversal<Vertex, Object> artifactTraversal)
    {
        return __.map(fold(Optional.of(artifactTraversal),
                           artifactAdaptersMap.values().iterator()));
    }

    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(Artifact entity)
    {
        UnfoldEntityTraversal<Vertex, Vertex> unfoldTraversal = artifactAdaptersMap.values()
                                                                                   .stream()
                                                                                   .map(ArtifactHierarchyAdapter.class::cast)
                                                                                   .filter(a -> a.entityClass().isInstance(entity))
                                                                                   .findFirst()
                                                                                   .map(a -> a.unfold((Artifact) entity))
                                                                                   .get();

        return new UnfoldEntityTraversal<>(unfoldTraversal.entityLabel(),
                __.<Vertex, Edge>coalesce(updateArtifact(entity),
                                          createArtifact(entity))
                  .outV()
                  .map(unfoldTraversal));
    }

    private Traversal<?, Edge> createArtifact(Artifact artifact)
    {
        return __.<Vertex>addE(Edges.REMOTE_ARTIFACT_INHERIT_ARTIFACT)
                 .from(__.identity())
                 .to(saveArtifact(artifact));
    }

    private Traversal<Vertex, Edge> updateArtifact(Artifact artifact)
    {
        return __.outE(Edges.REMOTE_ARTIFACT_INHERIT_ARTIFACT)
                 .as(Edges.REMOTE_ARTIFACT_INHERIT_ARTIFACT)
                 .inV()
                 .map(saveArtifact(artifact))
                 .select(Edges.REMOTE_ARTIFACT_INHERIT_ARTIFACT);
    }

    private <S> EntityTraversal<S, Vertex> saveArtifact(Artifact artifact)
    {
        return __.<S>V()
                 .saveV(Vertices.ARTIFACT,
                        artifact.getUuid(),
                        artifactAdapter.unfold(artifact));
    }

    @Override
    public EntityTraversal<Vertex, ? extends Element> cascade()
    {
        return __.<Vertex>aggregate("x")
                 .outE(Edges.REMOTE_ARTIFACT_INHERIT_ARTIFACT)
                 .inV()
                 .map(artifactAdapter.cascade())
                 .select("x")
                 .unfold();
    }

}
