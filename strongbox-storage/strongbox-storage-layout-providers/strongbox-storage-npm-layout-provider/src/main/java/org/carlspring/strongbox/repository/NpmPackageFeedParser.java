package org.carlspring.strongbox.repository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.domain.ArtifactTagEntity;
import org.carlspring.strongbox.npm.metadata.PackageEntry;
import org.carlspring.strongbox.npm.metadata.PackageFeed;
import org.carlspring.strongbox.npm.metadata.PackageVersion;
import org.carlspring.strongbox.npm.metadata.SearchResult;
import org.carlspring.strongbox.npm.metadata.SearchResults;
import org.carlspring.strongbox.npm.metadata.Versions;
import org.carlspring.strongbox.services.ArtifactIdGroupService;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NpmPackageFeedParser
{

    static final Logger logger = LoggerFactory.getLogger(NpmPackageFeedParser.class);

    @Inject
    private ArtifactTagService artifactTagService;

    @Inject
    private ArtifactIdGroupService repositoryArtifactIdGroupService;

    @Transactional
    public void parseSearchResult(Repository repository,
                                  SearchResults searchResults)
        throws IOException
    {
        ArtifactTag lastVersionTag = artifactTagService.findOneOrCreate(ArtifactTagEntity.LAST_VERSION);

        String repositoryId = repository.getId();
        String storageId = repository.getStorage().getId();

        Set<Artifact> artifactToSaveSet = new HashSet<>();
        for (SearchResult searchResult : searchResults.getObjects())
        {
            PackageEntry packageEntry = searchResult.getPackage();

            ArtifactEntity remoteArtifactEntry = parseVersion(storageId, repositoryId, packageEntry);
            if (remoteArtifactEntry == null)
            {
                continue;
            }

            remoteArtifactEntry.getTagSet().add(lastVersionTag);

            artifactToSaveSet.add(remoteArtifactEntry);
        }

        repositoryArtifactIdGroupService.saveArtifacts(repository, artifactToSaveSet);
    }

    @Transactional
    public void parseFeed(Repository repository,
                          PackageFeed packageFeed)
        throws IOException
    {
        if (packageFeed == null)
        {
            return;
        }

        String repositoryId = repository.getId();
        String storageId = repository.getStorage().getId();

        ArtifactTag lastVersionTag = artifactTagService.findOneOrCreate(ArtifactTagEntity.LAST_VERSION);

        Versions versions = packageFeed.getVersions();
        if (versions == null)
        {
            return;
        }

        Map<String, PackageVersion> versionMap = versions.getAdditionalProperties();
        if (versionMap == null || versionMap.isEmpty())
        {
            return;
        }

        Set<Artifact> artifactToSaveSet = new HashSet<>();
        for (PackageVersion packageVersion : versionMap.values())
        {
            ArtifactEntity remoteArtifactEntry = parseVersion(storageId, repositoryId, packageVersion);
            if (remoteArtifactEntry == null)
            {
                continue;
            }

            if (packageVersion.getVersion().equals(packageFeed.getDistTags().getLatest()))
            {
                remoteArtifactEntry.getTagSet().add(lastVersionTag);
            }

            artifactToSaveSet.add(remoteArtifactEntry);
        }

        repositoryArtifactIdGroupService.saveArtifacts(repository, artifactToSaveSet);
    }

    private ArtifactEntity parseVersion(String storageId,
                                              String repositoryId,
                                              PackageVersion packageVersion)
    {
        NpmArtifactCoordinates c = NpmArtifactCoordinates.of(packageVersion.getName(), packageVersion.getVersion());

        ArtifactEntity remoteArtifactEntry = new ArtifactEntity(storageId, repositoryId, c);
        remoteArtifactEntry.setStorageId(storageId);
        remoteArtifactEntry.setRepositoryId(repositoryId);
        remoteArtifactEntry.setArtifactCoordinates(c);
        remoteArtifactEntry.setLastUsed(LocalDateTime.now());
        remoteArtifactEntry.setLastUpdated(LocalDateTime.now());
        remoteArtifactEntry.setDownloadCount(0);
        remoteArtifactEntry.setArtifactFileExists(Boolean.FALSE);

        // TODO make HEAD request for `tarball` URL ???
        // remoteArtifactEntry.setSizeInBytes(packageVersion.getProperties().getPackageSize());

        return remoteArtifactEntry;
    }

    private ArtifactEntity parseVersion(String storageId,
                                        String repositoryId,
                                        PackageEntry packageEntry)
    {
        String scope = packageEntry.getScope();
        String packageId = NpmArtifactCoordinates.calculatePackageId("unscoped".equals(scope) ? null : scope,
                                                                     packageEntry.getName());
        NpmArtifactCoordinates c = NpmArtifactCoordinates.of(packageId, packageEntry.getVersion());

        ArtifactEntity remoteArtifactEntry = new ArtifactEntity(storageId, repositoryId, c);
        remoteArtifactEntry.setStorageId(storageId);
        remoteArtifactEntry.setRepositoryId(repositoryId);
        remoteArtifactEntry.setArtifactCoordinates(c);
        remoteArtifactEntry.setLastUsed(LocalDateTime.now());
        remoteArtifactEntry.setLastUpdated(LocalDateTime.now());
        remoteArtifactEntry.setDownloadCount(0);
        remoteArtifactEntry.setArtifactFileExists(Boolean.FALSE);

        return remoteArtifactEntry;
    }

}
