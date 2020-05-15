package org.carlspring.strongbox.gremlin.adapters;

import java.util.Set;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.artifact.coordinates.GenericArtifactCoordinates;
import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.__;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 */
@Component
public class ArtifactCoordinatesHierarchyAdapter
        extends EntityUpwardHierarchyAdapter<GenericArtifactCoordinates, ArtifactCoodrinatesNodeAdapter>
{

    public ArtifactCoordinatesHierarchyAdapter(Set<ArtifactCoodrinatesNodeAdapter> artifactArapters)
    {
        super(artifactArapters, 1);
    }

    @Override
    public EntityTraversal<Vertex, Element> cascade()
    {
        return __.<Vertex>aggregate("x")
                 .inE(Edges.EXTENDS)
                 .outV()
                 .aggregate("x")
                 .select("x")
                 .unfold();
    }

}
