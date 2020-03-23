package org.carlspring.strongbox.gremlin.adapters;

import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.extractObject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
    private ArtifactAdapter artifactAdapter;

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
                       .map(artifactAdapter.fold())
                       .map(EntityTraversalUtils::castToObject)
                       .fold())
                 .map(this::map);
    }

    private ArtifactIdGroup map(Traverser<Map<String, Object>> t)
    {
        ArtifactIdGroupEntity result = new ArtifactIdGroupEntity(extractObject(String.class, t.get().get("storageId")),
                extractObject(String.class, t.get().get("repositoryId")), extractObject(String.class, t.get().get("name")));
        Collection<ArtifactEntity> artifacts = (Collection<ArtifactEntity>) t.get().get("artifacts");
        result.getArtifacts().addAll(artifacts);

        return result;
    }

    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(ArtifactIdGroup entity)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityTraversal<Vertex, ? extends Element> cascade()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
