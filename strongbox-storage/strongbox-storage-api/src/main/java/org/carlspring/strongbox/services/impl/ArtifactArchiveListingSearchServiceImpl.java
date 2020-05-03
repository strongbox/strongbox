package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactArchiveListing;
import org.carlspring.strongbox.repositories.ArtifactArchiveListingRepository;
import org.carlspring.strongbox.services.ArtifactArchiveListingSearchService;

import javax.inject.Inject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author ankit.tomar
 */
@Service
@Transactional
public class ArtifactArchiveListingSearchServiceImpl implements ArtifactArchiveListingSearchService
{

    @Inject
    private ArtifactArchiveListingRepository archiveListingRepository;

    @Override
    public Set<String> fetchArchiveFileNames(Artifact artifactEntry)
    {
        List<ArtifactArchiveListing> artifactArchiveListings = archiveListingRepository.findByOutGoingEdge(artifactEntry);

        return artifactArchiveListings.stream()
                                      .map(ArtifactArchiveListing::getFileName)
                                      .collect(Collectors.toSet());

    }

}
