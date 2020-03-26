package org.carlspring.strongbox.gremlin.adapters;

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
        extends EntityHierarchyAdapter<GenericArtifactCoordinates, ArtifactCoodrinatesNodeAdapter<?>>
{

    @Override
    protected String hierarchyEdge()
    {
        return Edges.ARTIFACT_COORDINATES_INHERIT_GENERIC_ARTIFACT_COORDINATES;
    }

    @Override
    public EntityTraversal<Vertex, Element> cascade()
    {
        return __.<Vertex>aggregate("x")
                 .outE(hierarchyEdge())
                 .inV()
                 .map(getRootAdapter().cascade())
                 .select("x")
                 .unfold();
    }

}
