package org.carlspring.strongbox.gremlin.adapters;

import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;
import static org.carlspring.strongbox.db.schema.Vertices.ARTIFACT_ARCHIVE_LISTING;
import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.extractObject;

import org.carlspring.strongbox.domain.ArtifactArchiveListing;
import org.carlspring.strongbox.domain.ArtifactArchiveListingEntity;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.__;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.stereotype.Component;

/**
 * @author ankit.tomar
 */
@Component
public class ArtifactArchiveListingAdapter extends VertexEntityTraversalAdapter<ArtifactArchiveListing>
{

    @Override
    public Set<String> labels()
    {
        return Collections.singleton(ARTIFACT_ARCHIVE_LISTING);
    }

    @Override
    public EntityTraversal<Vertex, ArtifactArchiveListing> fold()
    {
        return __.<Vertex, Object>project("id",
                                          "uuid",
                                          "fileName",
                                          "storageId",
                                          "repositoryId")
                 .by(__.id())
                 .by(__.enrichPropertyValue("uuid"))
                 .by(__.enrichPropertyValue("storageId"))
                 .by(__.enrichPropertyValue("repositoryId"))
                 .by(__.enrichPropertyValue("fileName"))
                 .map(this::map);
    }

    private ArtifactArchiveListing map(Traverser<Map<String, Object>> t)
    {
        ArtifactArchiveListingEntity result = new ArtifactArchiveListingEntity();
        result.setNativeId(extractObject(Long.class, t.get().get("id")));
        result.setUuid(extractObject(String.class, t.get().get("uuid")));
        result.setFileName(extractObject(String.class, t.get().get("fileName")));
        result.setStorageId(extractObject(String.class, t.get().get("storageId")));
        result.setRepositoryId(extractObject(String.class, t.get().get("repositoryId")));
        return result;
    }

    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(ArtifactArchiveListing entity)
    {

        EntityTraversal<Vertex, Vertex> t = __.<Vertex>identity();

        if (entity.getFileName() != null)
        {
            t = t.property(single, "fileName", entity.getFileName());
        }
        if (entity.getStorageId() != null)
        {
            t = t.property(single, "storageId", entity.getStorageId());
        }
        if (entity.getRepositoryId() != null)
        {
            t = t.property(single, "repositoryId", entity.getRepositoryId());
        }

        return new UnfoldEntityTraversal<>(ARTIFACT_ARCHIVE_LISTING, entity, t);
    }

    @Override
    public EntityTraversal<Vertex, Element> cascade()
    {
        return __.<Vertex>identity().map(t -> Element.class.cast(t.get()));
    }

}
