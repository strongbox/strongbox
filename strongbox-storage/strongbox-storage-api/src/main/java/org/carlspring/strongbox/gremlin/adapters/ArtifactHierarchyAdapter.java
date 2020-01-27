package org.carlspring.strongbox.gremlin.adapters;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.__;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 */
@Component
public class ArtifactHierarchyAdapter extends EntityHierarchyAdapter<Artifact, ArtifactHierarchyNodeAdapter<?>>
{

    @Inject
    private ArtifactAdapter artifactAdapter;

    protected String hierarchyEdge()
    {
        return Edges.REMOTE_ARTIFACT_INHERIT_ARTIFACT;
    }

    @Override
    public EntityTraversal<Vertex, Element> cascade()
    {
        return __.<Vertex>aggregate("x")
                 .optional(__.outE(hierarchyEdge())
                             .inV())
                 .map(artifactAdapter.cascade())
                 .select("x")
                 .unfold();
    }

}
