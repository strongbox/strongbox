package org.carlspring.strongbox.artifact.coordinates;

import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.extractObject;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.GenericArtifactCoordinatesEntity;
import org.carlspring.strongbox.gremlin.adapters.LayoutArtifactCoordinatesArapter;
import org.carlspring.strongbox.gremlin.adapters.UnfoldEntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.__;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 */
@Component
public class MavenArtifactCoordinatesAdapter
        extends LayoutArtifactCoordinatesArapter<MavenArtifactCoordinates, ComparableVersion>
{

    @Override
    public Set<String> labels()
    {
        return Collections.singleton(Vertices.MAVEN_ARTIFACT_COORDINATES);
    }

    @Override
    public Class<? extends MavenArtifactCoordinates> entityClass()
    {
        return MavenArtifactCoordinates.class;
    }

    @Override
    public EntityTraversal<Vertex, MavenArtifactCoordinates> foldHierarchy(EntityTraversal<Vertex, Object> parentProjection,
                                                                           EntityTraversal<Vertex, Object> childProjection)
    {
        return __.<Vertex>hasLabel(Vertices.MAVEN_ARTIFACT_COORDINATES)
                 .project("uuid", "genericArtifactCoordinates")
                 .by(__.enrichPropertyValue("uuid"))
                 .by(parentProjection)
                 .map(this::map);
    }

    private MavenArtifactCoordinates map(Traverser<Map<String, Object>> t)
    {
        GenericArtifactCoordinatesEntity genericArtifactCoordinates = extractObject(GenericArtifactCoordinatesEntity.class,
                                                                                    t.get()
                                                                                     .get("genericArtifactCoordinates"));
        MavenArtifactCoordinates result;
        if (genericArtifactCoordinates == null)
        {
            result = new MavenArtifactCoordinates();
        }
        else
        {
            result = new MavenArtifactCoordinates(genericArtifactCoordinates);
            genericArtifactCoordinates.setLayoutArtifactCoordinates(result);
        }
        result.setUuid(extractObject(String.class, t.get().get("uuid")));

        return result;
    }

    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(MavenArtifactCoordinates entity)
    {
        return new UnfoldEntityTraversal<>(Vertices.MAVEN_ARTIFACT_COORDINATES, __.identity());
    }

}
