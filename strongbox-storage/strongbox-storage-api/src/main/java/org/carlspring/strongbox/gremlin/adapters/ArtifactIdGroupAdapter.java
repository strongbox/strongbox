package org.carlspring.strongbox.gremlin.adapters;

import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;
import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.extractObject;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.domain.ArtifactIdGroup;
import org.carlspring.strongbox.domain.ArtifactIdGroupEntity;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.__;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 */
@Component
public class ArtifactIdGroupAdapter extends VertexEntityTraversalAdapter<ArtifactIdGroup>
{

    @Inject
    private ArtifactHierarchyAdapter artifactAdapter;

    @Override
    public Set<String> labels()
    {
        return Collections.singleton(Vertices.ARTIFACT_ID_GROUP);
    }

    @Override
    public EntityTraversal<Vertex, ArtifactIdGroup> fold()
    {
        return __.<Vertex, Object>project("uuid", "storageId", "repositoryId", "name", "artifacts")
                 .by(__.enrichPropertyValue("uuid"))
                 .by(__.enrichPropertyValue("storageId"))
                 .by(__.enrichPropertyValue("repositoryId"))
                 .by(__.enrichPropertyValue("name"))
                 .by(__.outE(Edges.ARTIFACT_GROUP_HAS_ARTIFACTS)
                       .inV()
                       .trace("artifact")
                       .optional(__.inE(Edges.REMOTE_ARTIFACT_INHERIT_ARTIFACT)
                                   .trace(Edges.REMOTE_ARTIFACT_INHERIT_ARTIFACT)
                                   .otherV()
                                   .trace("remote-artifact"))
                       .map(artifactAdapter.fold())
                       .map(EntityTraversalUtils::castToObject)
                       .fold())
                 .map(this::map);
    }

    private ArtifactIdGroup map(Traverser<Map<String, Object>> t)
    {
        ArtifactIdGroupEntity result = new ArtifactIdGroupEntity(extractObject(String.class, t.get().get("storageId")),
                extractObject(String.class, t.get().get("repositoryId")), extractObject(String.class, t.get().get("name")));
        Collection<Artifact> artifacts = (Collection<Artifact>) t.get().get("artifacts");
        artifacts.stream().forEach(result::addArtifact);

        return result;
    }

    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(ArtifactIdGroup entity)
    {
        EntityTraversal<Vertex, Vertex> saveArtifacstTraversal = __.<Vertex>identity();
        for (Artifact artifact : entity.getArtifacts())
        {
            UnfoldEntityTraversal<Vertex, Vertex> unfoldArtifactTraversal = artifactAdapter.unfold(artifact);
            saveArtifacstTraversal = saveArtifacstTraversal.V()
                                                           .saveV(unfoldArtifactTraversal.entityLabel(), artifact.getUuid(),
                                                                  unfoldArtifactTraversal)
                                                           .optional(__.outE(Edges.REMOTE_ARTIFACT_INHERIT_ARTIFACT).otherV())
                                                           .aggregate("aiga");
        }

        EntityTraversal<Vertex, Vertex> unfoldTraversal = __.<Vertex>sideEffect(__.outE(Edges.ARTIFACT_GROUP_HAS_ARTIFACTS)
                                                                                  .drop())
                                                            .map(unfoldArtifactGroup(entity))
                                                            .store("aigaig")
                                                            .sideEffect(saveArtifacstTraversal.select("aiga")
                                                                                              .unfold()
                                                                                              .trace("111")
                                                                                              .addE(Edges.ARTIFACT_GROUP_HAS_ARTIFACTS)
                                                                                              .from(__.select("aigaig")
                                                                                                      .unfold()));

        return new UnfoldEntityTraversal<>(Vertices.ARTIFACT_ID_GROUP, unfoldTraversal);
    }

    private EntityTraversal<Vertex, Vertex> unfoldArtifactGroup(ArtifactIdGroup entity)
    {
        EntityTraversal<Vertex, Vertex> unfoldEntityTraversal = __.identity();

        if (entity.getStorageId() != null)
        {
            unfoldEntityTraversal = unfoldEntityTraversal.property(single, "storageId", entity.getStorageId());
        }
        if (entity.getRepositoryId() != null)
        {
            unfoldEntityTraversal = unfoldEntityTraversal.property(single, "repositoryId", entity.getRepositoryId());
        }
        if (entity.getName() != null)
        {
            unfoldEntityTraversal = unfoldEntityTraversal.property(single, "name", entity.getName());
        }

        return unfoldEntityTraversal;
    }

    @Override
    public EntityTraversal<Vertex, ? extends Element> cascade()
    {
        return null;
    }

}
