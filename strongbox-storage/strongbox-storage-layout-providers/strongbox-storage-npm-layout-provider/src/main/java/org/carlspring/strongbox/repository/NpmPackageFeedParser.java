package org.carlspring.strongbox.repository;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.ArtifactTagEntry;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.domain.RepositoryArtifactIdGroupEntry;
import org.carlspring.strongbox.npm.metadata.PackageEntry;
import org.carlspring.strongbox.npm.metadata.PackageFeed;
import org.carlspring.strongbox.npm.metadata.PackageVersion;
import org.carlspring.strongbox.npm.metadata.SearchResult;
import org.carlspring.strongbox.npm.metadata.SearchResults;
import org.carlspring.strongbox.npm.metadata.Versions;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathLock;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.carlspring.strongbox.services.RepositoryArtifactIdGroupService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NpmPackageFeedParser
{

    static final Logger logger = LoggerFactory.getLogger(NpmPackageFeedParser.class);

    @Inject
    private ArtifactTagService artifactTagService;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Inject
    private ArtifactEntryService artifactEntryService;
    
    @Inject
    private RepositoryArtifactIdGroupService repositoryArtifactIdGroupService;

    @Inject
    private RepositoryPathLock repositoryPathLock;

    public void parseSearchResult(Repository repository,
                                  SearchResults searchResults)
        throws IOException
    {
        ArtifactTag lastVersionTag = artifactTagService.findOneOrCreate(ArtifactTagEntry.LAST_VERSION);

        String repositoryId = repository.getId();
        String storageId = repository.getStorage().getId();

        Set<ArtifactEntry> artifactToSaveSet = new HashSet<>();
        for (SearchResult searchResult : searchResults.getObjects())
        {
            PackageEntry packageEntry = searchResult.getPackage();

            RemoteArtifactEntry remoteArtifactEntry = parseVersion(storageId, repositoryId, packageEntry);
            if (remoteArtifactEntry == null)
            {
                continue;
            }

            remoteArtifactEntry.getTagSet().add(lastVersionTag);

            artifactToSaveSet.add(remoteArtifactEntry);
        }

        saveArtifactEntrySet(repository, artifactToSaveSet);
    }

    private void saveArtifactEntrySet(Repository repository,
                                      Set<ArtifactEntry> artifactToSaveSet)
        throws IOException
    {
        for (ArtifactEntry e : artifactToSaveSet)
        {
            RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository).resolve(e);

            saveArtifactEntry(repositoryPath);
        }
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

        ArtifactTag lastVersionTag = artifactTagService.findOneOrCreate(ArtifactTagEntry.LAST_VERSION);

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

        Set<ArtifactEntry> artifactToSaveSet = new HashSet<>();
        for (PackageVersion packageVersion : versionMap.values())
        {
            RemoteArtifactEntry remoteArtifactEntry = parseVersion(storageId, repositoryId, packageVersion);
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

        saveArtifactEntrySet(repository, artifactToSaveSet);

    }

    private void saveArtifactEntry(RepositoryPath repositoryPath)
        throws IOException
    {
        ArtifactEntry e = repositoryPath.getArtifactEntry();
        
        Repository repository = repositoryPath.getRepository();
        Storage storage = repository.getStorage();
        ArtifactCoordinates coordinates = RepositoryFiles.readCoordinates(repositoryPath);

        Lock lock = repositoryPathLock.lock(repositoryPath).writeLock();
        lock.lock();

        try
        {
            if (artifactEntryService.artifactExists(e.getStorageId(), e.getRepositoryId(),
                                                    e.getArtifactCoordinates().toPath()))
            {
                return;
            }

            RepositoryArtifactIdGroupEntry artifactGroup = repositoryArtifactIdGroupService.findOneOrCreate(storage.getId(), repository.getId(), coordinates.getId());
            repositoryArtifactIdGroupService.addArtifactToGroup(artifactGroup, e);
        } 
        finally
        {
            lock.unlock();
        }
    }

    private RemoteArtifactEntry parseVersion(String storageId,
                                             String repositoryId,
                                             PackageVersion packageVersion)
    {
        NpmArtifactCoordinates c = NpmArtifactCoordinates.of(packageVersion.getName(), packageVersion.getVersion());

        RemoteArtifactEntry remoteArtifactEntry = new RemoteArtifactEntry();
        remoteArtifactEntry.setStorageId(storageId);
        remoteArtifactEntry.setRepositoryId(repositoryId);
        remoteArtifactEntry.setArtifactCoordinates(c);
        remoteArtifactEntry.setLastUsed(new Date());
        remoteArtifactEntry.setLastUpdated(new Date());
        remoteArtifactEntry.setDownloadCount(0);

        // TODO make HEAD request for `tarball` URL ???
        // remoteArtifactEntry.setSizeInBytes(packageVersion.getProperties().getPackageSize());

        return remoteArtifactEntry;
    }

    private RemoteArtifactEntry parseVersion(String storageId,
                                             String repositoryId,
                                             PackageEntry packageEntry)
    {
        String scope = packageEntry.getScope();
        String packageId = NpmArtifactCoordinates.calculatePackageId("unscoped".equals(scope) ? null : scope,
                                                                     packageEntry.getName());
        NpmArtifactCoordinates c = NpmArtifactCoordinates.of(packageId, packageEntry.getVersion());

        RemoteArtifactEntry remoteArtifactEntry = new RemoteArtifactEntry();
        remoteArtifactEntry.setStorageId(storageId);
        remoteArtifactEntry.setRepositoryId(repositoryId);
        remoteArtifactEntry.setArtifactCoordinates(c);
        remoteArtifactEntry.setLastUsed(new Date());
        remoteArtifactEntry.setLastUpdated(new Date());
        remoteArtifactEntry.setDownloadCount(0);

        return remoteArtifactEntry;
    }

}
