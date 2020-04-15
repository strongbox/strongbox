package org.carlspring.strongbox.gremlin.adapters;

import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.set;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;
import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.extracPropertytList;
import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.extractObject;
import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.toLocalDateTime;
import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.toLong;
import static org.carlspring.strongbox.gremlin.dsl.EntityTraversalDsl.NULL;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactArchiveListing;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.domain.GenericArtifactCoordinatesEntity;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversalDsl;
import org.carlspring.strongbox.gremlin.dsl.__;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 */
@Component
public class ArtifactAdapter extends VertexEntityTraversalAdapter<Artifact> implements ArtifactHierarchyNodeAdapter<Artifact>
{

    @Inject
    GenericArtifactCoordinatesArapter genericArtifactCoordinatesArapter;
    @Inject
    ArtifactCoordinatesHierarchyAdapter artifactCoordinatesAdapter;
    @Inject
    ArtifactTagAdapter artifactTagAdapter;
    @Inject
    ArtifactHierarchyAdapter genericArtifactAdapter;

    @Override
    public Set<String> labels()
    {
        return Collections.singleton(Vertices.ARTIFACT);
    }

    @Override
    public Class<? extends Artifact> entityClass()
    {
        return Artifact.class;
    }

    @Override
    public EntityTraversal<Vertex, Artifact> fold()
    {
        return foldHierarchy(parentProjection(), childProjection());
    }

    @Override
    public EntityTraversal<Vertex, Object> parentProjection()
    {
        return __.<Vertex>V().constant(EntityTraversalDsl.NULL);
    }

    @Override
    public EntityTraversal<Vertex, Artifact> foldHierarchy(EntityTraversal<Vertex, Object> parentProjection,
                                                           EntityTraversal<Vertex, Object> childProjection)
    {
        return __.<Vertex, Object>project("id",
                                          "uuid",
                                          "storageId",
                                          "repositoryId",
                                          "lastUpdated",
                                          "lastUsed",
                                          "created",
                                          "sizeInBytes",
                                          "downloadCount",
                                          "filenames",
                                          "checksums",
                                          "genericArtifactCoordinates",
                                          "tags",
                                          "artifactHierarchyChild")
                 .by(__.id())
                 .by(__.enrichPropertyValue("uuid"))
                 .by(__.enrichPropertyValue("storageId"))
                 .by(__.enrichPropertyValue("repositoryId"))
                 .by(__.enrichPropertyValue("lastUpdated"))
                 .by(__.enrichPropertyValue("lastUsed"))
                 .by(__.enrichPropertyValue("created"))
                 .by(__.enrichPropertyValue("sizeInBytes"))
                 .by(__.enrichPropertyValue("downloadCount"))
                 .by(__.enrichPropertyValues("filenames"))
                 .by(__.enrichPropertyValues("checksums"))
                 .by(__.outE(Edges.ARTIFACT_HAS_ARTIFACT_COORDINATES)
                       .mapToObject(__.inV()
                                      .map(genericArtifactCoordinatesArapter.fold())
                                      .map(EntityTraversalUtils::castToObject)))
                 .by(__.outE(Edges.ARTIFACT_HAS_TAGS)
                       .inV()
                       .map(artifactTagAdapter.fold())
                       .map(EntityTraversalUtils::castToObject)
                       .fold())
                 .by(childProjection)
                 .map(this::map);
    }

    public EntityTraversal<Vertex, Object> childProjection()
    {
        return __.inE(Edges.REMOTE_ARTIFACT_INHERIT_ARTIFACT)
                 .mapToObject(__.outV()
                                .map(genericArtifactAdapter.fold(() -> __.<Vertex>identity().constant(NULL)))
                                .map(EntityTraversalUtils::castToObject));
    }

    private Artifact map(Traverser<Map<String, Object>> t)
    {
        String storageId = extractObject(String.class, t.get().get("storageId"));
        String repositoryId = extractObject(String.class, t.get().get("repositoryId"));
        GenericArtifactCoordinatesEntity artifactCoordinates = extractObject(GenericArtifactCoordinatesEntity.class,
                                                                             t.get().get("genericArtifactCoordinates"));

        ArtifactEntity result = new ArtifactEntity(storageId, repositoryId, artifactCoordinates.getLayoutArtifactCoordinates());
        result.setNativeId(extractObject(Long.class, t.get().get("id")));
        result.setUuid(extractObject(String.class, t.get().get("uuid")));

        result.setCreated(toLocalDateTime(extractObject(Long.class, t.get().get("created"))));
        result.setLastUpdated(toLocalDateTime(extractObject(Long.class, t.get().get("lastUpdated"))));
        result.setLastUsed(toLocalDateTime(extractObject(Long.class, t.get().get("lastUsed"))));
        result.setSizeInBytes(extractObject(Long.class, t.get().get("sizeInBytes")));
        result.setDownloadCount(extractObject(Integer.class, t.get().get("downloadCount")));
        
        result.getArtifactArchiveListing()
              .setFilenames(extracPropertytList(String.class, t.get().get("filenames")).stream()
                                                                                       .filter(e -> !e.trim().isEmpty())
                                                                                       .collect(Collectors.toSet()));

        result.addChecksums(extracPropertytList(String.class, t.get().get("checksums")).stream()
                                                                                       .filter(e -> !e.trim().isEmpty())
                                                                                       .collect(Collectors.toSet()));

        List<ArtifactTag> tags = (List<ArtifactTag>) t.get().get("tags");
        result.setTagSet(new HashSet<>(tags));

        Artifact artifactHierarchyChild = extractObject(Artifact.class,
                                                        t.get()
                                                         .get("artifactHierarchyChild"));
        result.setHierarchyChild(artifactHierarchyChild);
        // artifactCoordinates.setGenericArtifactCoordinates(result);

        return result;
    }

    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(Artifact entity)
    {
        ArtifactCoordinates artifactCoordinates = entity.getArtifactCoordinates();
        String storedArtifactId = Vertices.ARTIFACT + ":" + UUID.randomUUID().toString();        
        
        Set<String> tagNames = entity.getTagSet().stream().map(ArtifactTag::getName).collect(Collectors.toSet());
        EntityTraversal<Vertex, Vertex> unfoldTraversal = __.<Vertex, Edge>coalesce(__.<Vertex>outE(Edges.ARTIFACT_HAS_ARTIFACT_COORDINATES),
                                                                                    //cascading create ArtifactCoordinates only
                                                                                    createArtifactCoordinates(artifactCoordinates))
                                                            .outV()
                                                            .sideEffect(__.outE(Edges.ARTIFACT_HAS_TAGS).drop())
                                                            .map(unfoldArtifact(entity))
                                                            .store(storedArtifactId)
                                                            .sideEffect(__.V()
                                                                        .hasLabel(Vertices.ARTIFACT_TAG)
                                                                        .has("uuid", P.within(tagNames))
                                                                        .addE(Edges.ARTIFACT_HAS_TAGS)
                                                                        .from(__.select(storedArtifactId).unfold()));

        return new UnfoldEntityTraversal<>(Vertices.ARTIFACT, unfoldTraversal);
    }

    private Traversal<Vertex, Edge> createArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        return __.<Vertex>addE(Edges.ARTIFACT_HAS_ARTIFACT_COORDINATES)
                 .from(__.identity())
                 .to(saveArtifactCoordinates(artifactCoordinates));
    }

    private <S2> EntityTraversal<S2, Vertex> saveArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        UnfoldEntityTraversal<Vertex, Vertex> artifactCoordinatesUnfold = artifactCoordinatesAdapter.unfold(artifactCoordinates);
        String artifactCoordinatesLabel = artifactCoordinatesUnfold.entityLabel();

        return __.<S2>V()
                 .saveV(artifactCoordinatesLabel,
                        artifactCoordinates.getUuid(),
                        artifactCoordinatesUnfold)
                 .outE(Edges.ARTIFACT_COORDINATES_INHERIT_GENERIC_ARTIFACT_COORDINATES)
                 .inV();
    }

    private EntityTraversal<Vertex, Vertex> unfoldArtifact(Artifact entity)
    {
        EntityTraversal<Vertex, Vertex> t = __.<Vertex>identity();

        if (entity.getStorageId() != null)
        {
            t = t.property(single, "storageId", entity.getStorageId());
        }
        if (entity.getRepositoryId() != null)
        {
            t = t.property(single, "repositoryId", entity.getRepositoryId());
        }
        if (entity.getCreated() != null)
        {
            t = t.property(single, "created", toLong(entity.getCreated()));
        }
        if (entity.getLastUpdated() != null)
        {
            t = t.property(single, "lastUpdated", toLong(entity.getLastUpdated()));
        }
        if (entity.getLastUsed() != null)
        {
            t = t.property(single, "lastUsed", toLong(entity.getLastUsed()));
        }
        if (entity.getSizeInBytes() != null)
        {
            t = t.property(single, "sizeInBytes", entity.getSizeInBytes());
        }
        if (entity.getDownloadCount() != null)
        {
            t = t.property(single, "downloadCount", entity.getDownloadCount());
        }        

        ArtifactArchiveListing artifactArchiveListing = entity.getArtifactArchiveListing();
        t = t.sideEffect(__.properties("filenames").drop());
        Set<String> filenames = artifactArchiveListing.getFilenames();
        t = t.property(set, "filenames", "");
        for (String filename : filenames)
        {
            t = t.property(set, "filenames", filename);
        }

        Map<String, String> checksums = entity.getChecksums();
        t = t.sideEffect(__.properties("checksums").drop());
        t = t.property(set, "checksums", "");
        for (String alg : checksums.keySet())
        {
            t = t.property(set, "checksums", "{" + alg + "}" + checksums.get(alg));
        }

        return t;
    }

    @Override
    public EntityTraversal<Vertex, Element> cascade()
    {
        return __.<Vertex>aggregate("x")
                //TODO: remove ArtifactAoordinates
//                 .optional(__.outE(Edges.ARTIFACT_HAS_ARTIFACT_COORDINATES)
//                             .inV()
//                             .where(__.inE(Edges.ARTIFACT_HAS_ARTIFACT_COORDINATES).count().is(1))
//                             .aggregate("x")
//                             .inE(Edges.ARTIFACT_COORDINATES_INHERIT_GENERIC_ARTIFACT_COORDINATES)
//                             .outV()
//                             .aggregate("x"))
                 .select("x")
                 .unfold();
    }

}
