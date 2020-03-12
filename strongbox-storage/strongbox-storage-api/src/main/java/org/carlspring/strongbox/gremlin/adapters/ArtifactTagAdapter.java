package org.carlspring.strongbox.gremlin.adapters;

import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.extractObject;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

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
public class ArtifactTagAdapter extends VertexEntityTraversalAdapter<ArtifactTag>
{

    @Override
    public Set<String> labels()
    {
        return Collections.singleton(Vertices.ARTIFACT_TAG);
    }

    @Override
    public EntityTraversal<Vertex, ArtifactTag> fold()
    {
        return __.<Vertex, Object>project("uuid")
                 .by(__.enrichPropertyValue("uuid"))
                 .map(this::map);
    }

    private ArtifactTag map(Traverser<Map<String, Object>> t)
    {
        ArtifactTagEntity result = new ArtifactTagEntity();
        result.setUuid(extractObject(String.class, t.get().get("uuid")));

        return result;
    }

    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(ArtifactTag entity)
    {
        return new UnfoldEntityTraversal<>(Vertices.ARTIFACT_TAG, __.identity());
    }

    @Override
    public EntityTraversal<Vertex, ? extends Element> cascade()
    {
        return __.identity();
    }

}
