package org.carlspring.strongbox.repositories;

import org.carlspring.strongbox.domain.ArtifactArchiveListing;
import org.carlspring.strongbox.gremlin.adapters.ArtifactArchiveListingAdapter;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

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

}

@Repository
interface ArtifactArchiveListingQueries
        extends org.springframework.data.repository.Repository<ArtifactArchiveListing, String>
{

}