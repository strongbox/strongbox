package org.carlspring.strongbox.gremlin.adapters;

import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;
import static org.carlspring.strongbox.gremlin.dsl.EntityTraversalUtils.extractPropertyList;
import static org.carlspring.strongbox.gremlin.dsl.EntityTraversalUtils.extractObject;
import static org.carlspring.strongbox.gremlin.dsl.EntityTraversalUtils.toLocalDateTime;
import static org.carlspring.strongbox.gremlin.dsl.EntityTraversalUtils.toLong;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.carlspring.strongbox.artifact.coordinates.GenericArtifactCoordinates;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactArchiveListing;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversalUtils;
import org.carlspring.strongbox.gremlin.dsl.__;
import org.springframework.stereotype.Component;

import static org.carlspring.strongbox.db.schema.Properties.UUID;
import static org.carlspring.strongbox.db.schema.Properties.STORAGE_ID;
import static org.carlspring.strongbox.db.schema.Properties.REPOSITORY_ID;
import static org.carlspring.strongbox.db.schema.Properties.LAST_UPDATED;
import static org.carlspring.strongbox.db.schema.Properties.LAST_USED;
import static org.carlspring.strongbox.db.schema.Properties.CREATED;
import static org.carlspring.strongbox.db.schema.Properties.SIZE_IN_BYTES;
import static org.carlspring.strongbox.db.schema.Properties.DOWNLOAD_COUNT;
import static org.carlspring.strongbox.db.schema.Properties.FILE_NAMES;
import static org.carlspring.strongbox.db.schema.Properties.CHECKSUMS;
import static org.carlspring.strongbox.db.schema.Properties.ARTIFACT_FILE_EXISTS;

/**
 * @author sbespalov
 */
@Component
public class ArtifactAdapter implements VertexEntityTraversalAdapter<Artifact>
{

    @Inject
    ArtifactCoordinatesHierarchyAdapter artifactCoordinatesAdapter;
    @Inject
    ArtifactTagAdapter artifactTagAdapter;

    @Override
    public String label()
    {
        return Vertices.ARTIFACT;
    }

    @Override
    public EntityTraversal<Vertex, Artifact> fold()
    {
        return fold(Optional.empty());
    }

    public EntityTraversal<Vertex, Artifact> fold(Optional<Class<? extends GenericArtifactCoordinates>> layoutArtifactCoordinatesClass)
    {
        return __.<Vertex, Object>project("id",
        								  UUID,
        								  STORAGE_ID,
        								  REPOSITORY_ID,
        								  LAST_UPDATED,
        								  LAST_USED,
        								  CREATED,
        								  SIZE_IN_BYTES,
                                          DOWNLOAD_COUNT,
                                          FILE_NAMES,
                                          CHECKSUMS,
                                          "artifactCoordinates",
                                          "tags",
                                          ARTIFACT_FILE_EXISTS)
                 .by(__.id())
                 .by(__.enrichPropertyValue(UUID))
                 .by(__.enrichPropertyValue(STORAGE_ID))
                 .by(__.enrichPropertyValue(REPOSITORY_ID))
                 .by(__.enrichPropertyValue(LAST_UPDATED))
                 .by(__.enrichPropertyValue(LAST_USED))
                 .by(__.enrichPropertyValue(CREATED))
                 .by(__.enrichPropertyValue(SIZE_IN_BYTES))
                 .by(__.enrichPropertyValue(DOWNLOAD_COUNT))
                 .by(__.enrichPropertyValues(FILE_NAMES))
                 .by(__.enrichPropertyValues(CHECKSUMS))
                 .by(__.outE(Edges.ARTIFACT_HAS_ARTIFACT_COORDINATES)
                       .mapToObject(__.inV()
                                      .map(artifactCoordinatesAdapter.fold(layoutArtifactCoordinatesClass))
                                      .map(EntityTraversalUtils::castToObject)))
                 .by(__.outE(Edges.ARTIFACT_HAS_TAGS)
                       .inV()
                       .map(artifactTagAdapter.fold())
                       .map(EntityTraversalUtils::castToObject)
                       .fold())
                 .by(__.enrichPropertyValue(ARTIFACT_FILE_EXISTS))
                 .map(this::map);
    }

    private Artifact map(Traverser<Map<String, Object>> t)
    {
        String storageId = extractObject(String.class, t.get().get(STORAGE_ID));
        String repositoryId = extractObject(String.class, t.get().get(REPOSITORY_ID));
        ArtifactCoordinates artifactCoordinates = extractObject(ArtifactCoordinates.class,
                                                                t.get().get("artifactCoordinates"));

        ArtifactEntity result = new ArtifactEntity(storageId, repositoryId, artifactCoordinates);
        result.setNativeId(extractObject(Long.class, t.get().get("id")));
        result.setUuid(extractObject(String.class, t.get().get(UUID)));

        result.setCreated(toLocalDateTime(extractObject(Long.class, t.get().get(CREATED))));
        result.setLastUpdated(toLocalDateTime(extractObject(Long.class, t.get().get(LAST_UPDATED))));
        result.setLastUsed(toLocalDateTime(extractObject(Long.class, t.get().get(LAST_USED))));
        result.setSizeInBytes(extractObject(Long.class, t.get().get(SIZE_IN_BYTES)));
        result.setDownloadCount(extractObject(Integer.class, t.get().get(DOWNLOAD_COUNT)));

        result.getArtifactArchiveListing()
              .setFilenames(extractPropertyList(String.class, t.get().get(FILE_NAMES)).stream()
                                                                                       .filter(e -> !e.trim().isEmpty())
                                                                                       .collect(Collectors.toSet()));

        result.addChecksums(extractPropertyList(String.class, t.get().get(CHECKSUMS)).stream()
                                                                                       .filter(e -> !e.trim().isEmpty())
                                                                                       .collect(Collectors.toSet()));

        List<ArtifactTag> tags = (List<ArtifactTag>) t.get().get("tags");
        result.setTagSet(new HashSet<>(tags));

        result.setArtifactFileExists(extractObject(Boolean.class, t.get().get(ARTIFACT_FILE_EXISTS)));

        return result;
    }

    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(Artifact entity)
    {
        ArtifactCoordinates artifactCoordinates = entity.getArtifactCoordinates();
        String storedArtifactId = Vertices.ARTIFACT + ":" + java.util.UUID.randomUUID().toString();

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
                                                                        .has(UUID, P.within(tagNames))
                                                                        .addE(Edges.ARTIFACT_HAS_TAGS)
                                                                        .from(__.select(storedArtifactId).unfold()));

        return new UnfoldEntityTraversal<>(Vertices.ARTIFACT, entity, unfoldTraversal);
    }

    private Traversal<Vertex, Edge> createArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        return __.<Vertex>addE(Edges.ARTIFACT_HAS_ARTIFACT_COORDINATES)
                 .to(saveArtifactCoordinates(artifactCoordinates));
    }

    private <S2> EntityTraversal<S2, Vertex> saveArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
    {
        UnfoldEntityTraversal<Vertex, Vertex> artifactCoordinatesUnfold = artifactCoordinatesAdapter.unfold(artifactCoordinates);

        return __.<S2>V(artifactCoordinates)
                 .saveV(artifactCoordinates.getUuid(),
                        artifactCoordinatesUnfold);
    }

    private EntityTraversal<Vertex, Vertex> unfoldArtifact(Artifact entity)
    {
        EntityTraversal<Vertex, Vertex> t = __.<Vertex>identity();

        if (entity.getStorageId() != null)
        {
            t = t.property(single, STORAGE_ID, entity.getStorageId());
        }
        if (entity.getRepositoryId() != null)
        {
            t = t.property(single, REPOSITORY_ID, entity.getRepositoryId());
        }
        if (entity.getCreated() != null)
        {
            t = t.property(single, CREATED, toLong(entity.getCreated()));
        }
        if (entity.getLastUpdated() != null)
        {
            t = t.property(single, LAST_UPDATED, toLong(entity.getLastUpdated()));
        }
        if (entity.getLastUsed() != null)
        {
            t = t.property(single, LAST_USED, toLong(entity.getLastUsed()));
        }
        if (entity.getSizeInBytes() != null)
        {
            t = t.property(single, SIZE_IN_BYTES, entity.getSizeInBytes());
        }
        if (entity.getDownloadCount() != null)
        {
            t = t.property(single, DOWNLOAD_COUNT, entity.getDownloadCount());
        }

        ArtifactArchiveListing artifactArchiveListing = entity.getArtifactArchiveListing();

        Set<String> filenames = artifactArchiveListing.getFilenames();
        t = t.sideEffect(__.properties(FILE_NAMES).drop());
        t = t.property(FILE_NAMES, filenames);

        Map<String, String> checksums = entity.getChecksums();
        Set<String> checkSumAlgo = new HashSet<>();
        for (String alg : checksums.keySet())
        {
            checkSumAlgo.add("{" + alg + "}" + checksums.get(alg));
        }
        t = t.sideEffect(__.properties(CHECKSUMS).drop());
        t = t.property(CHECKSUMS, checkSumAlgo);

        if (entity.getArtifactFileExists() != null)
        {
            t = t.property(single, ARTIFACT_FILE_EXISTS, entity.getArtifactFileExists());
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
