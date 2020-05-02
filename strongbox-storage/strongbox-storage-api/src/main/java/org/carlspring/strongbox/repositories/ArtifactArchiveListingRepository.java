package org.carlspring.strongbox.repositories;

import static org.carlspring.strongbox.db.schema.Edges.ARTIFACT_HAS_ARTIFACT_ARCHIVE_LISTING;
import static org.carlspring.strongbox.db.schema.Vertices.ARTIFACT;

import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactArchiveListing;
import org.carlspring.strongbox.gremlin.adapters.ArtifactArchiveListingAdapter;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.stereotype.Repository;

/**
 * @author ankit.tomar
 */
@Repository
@Transactional
public class ArtifactArchiveListingRepository extends GremlinVertexRepository<ArtifactArchiveListing>
        implements ArtifactArchiveListingQueries
{

    @Inject
    private ArtifactArchiveListingAdapter artifactArchiveListingAdapter;

    @Override
    protected ArtifactArchiveListingAdapter adapter()
    {
        return artifactArchiveListingAdapter;
    }

    public List<ArtifactArchiveListing> findByOutGoingEdge(Artifact artifactEntry)
    {
        EntityTraversal<Vertex, ArtifactArchiveListing> traversal = g().V()
                                                                       .hasLabel(ARTIFACT)
                                                                       .has("uuid", artifactEntry.getUuid())
                                                                       .outE(ARTIFACT_HAS_ARTIFACT_ARCHIVE_LISTING)
                                                                       .inV()
                                                                       .map(artifactArchiveListingAdapter.fold());

        return traversal.toList();
    }

}

@Repository
interface ArtifactArchiveListingQueries
        extends org.springframework.data.repository.Repository<ArtifactArchiveListing, String>
{

}