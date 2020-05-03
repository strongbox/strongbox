package org.carlspring.strongbox.services;

import org.carlspring.strongbox.domain.Artifact;

import java.util.Set;

/**
 * @author ankit.tomar
 */
public interface ArtifactArchiveListingSearchService
{

    Set<String> fetchArchiveFileNames(Artifact artifactEntry);

}
