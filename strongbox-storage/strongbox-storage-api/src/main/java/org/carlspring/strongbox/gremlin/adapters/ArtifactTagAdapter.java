package org.carlspring.strongbox.gremlin.adapters;

import static org.carlspring.strongbox.gremlin.dsl.EntityTraversalUtils.extractObject;

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.ArtifactTagEntity;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.__;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 */
@Component
public class ArtifactTagAdapter implements VertexEntityTraversalAdapter<ArtifactTag>
{

    @Override
    public String label()
    {
        return Vertices.ARTIFACT_TAG;
    }

    @Override
    public EntityTraversal<Vertex, ArtifactTag> fold()
    {
        return __.<Vertex, Object>project("id", "uuid")
                 .by(__.id())
                 .by(__.enrichPropertyValue("uuid"))
                 .map(this::map);
    }

    private ArtifactTag map(Traverser<Map<String, Object>> t)
    {
        ArtifactTagEntity result = new ArtifactTagEntity();
        result.setNativeId(extractObject(Long.class, t.get().get("id")));
        result.setUuid(extractObject(String.class, t.get().get("uuid")));

        return result;
    }

    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(ArtifactTag entity)
    {
        return new UnfoldEntityTraversal<>(Vertices.ARTIFACT_TAG, entity, __.identity());
    }

    @Override
    public EntityTraversal<Vertex, Element> cascade()
    {
        return __.<Vertex>identity().map(t -> Element.class.cast(t.get()));
    }

}
