package org.carlspring.strongbox.providers.io;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactArchiveListing;
import org.carlspring.strongbox.repositories.ArtifactRepository;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class RepositoryPathResolver
{

    @Inject
    protected ConfigurationManager configurationManager;

    @Inject
    protected ArtifactRepository artifactEntityRepository;

    @Inject
    protected RepositoryFileSystemRegistry fileSystemRegistry;

    public RootRepositoryPath resolve(String storageId,
                                      String repositoryId)
    {
        Storage storage = configurationManager.getConfiguration().getStorage(storageId);
        Objects.requireNonNull(storage, String.format("Storage [%s] not found", storageId));

        return resolve(storage.getRepository(repositoryId));
    }
    
    public RootRepositoryPath resolve(final Repository repository)
    {
        Objects.requireNonNull(repository, "Repository should be provided");

        LayoutFileSystemFactory fileSystemFactory = fileSystemRegistry.lookupRepositoryFileSystemFactory(repository);

        return fileSystemFactory.create(repository).getRootDirectory();
    }

    public RepositoryPath resolve(String storageId,
                                  String repositoryId,
                                  String path)
    {
        Storage storage = configurationManager.getConfiguration().getStorage(storageId);
        Objects.requireNonNull(storage, String.format("Storage [%s] not found", storageId));

        return resolve(storage.getRepository(repositoryId), path);
    }

    public RepositoryPath resolve(final Repository repository,
                                  final ArtifactCoordinates c)
    {
        return resolve(repository, c.buildPath());
    }

    public RepositoryPath resolve(final Repository repository,
                                  final RepositoryPath otherPath)
        throws IOException
    {
        if (otherPath.getRepository().getId().equals(repository.getId())
                && otherPath.getArtifactEntry() != null)
        {
            return otherPath;
        }

        return resolve(repository, RepositoryFiles.relativizePath(otherPath));
    }

    public RepositoryPath resolve(final Repository repository,
                                  final String path)
    {
        RootRepositoryPath repositoryPath = resolve(repository);
        if (repository.isGroupRepository())
        {
            return repositoryPath.resolve(path);
        }
        
        return new LazyRepositoryPath(repositoryPath.resolve(path));
    }
    
    private class LazyRepositoryPath extends RepositoryPath
    {

        private LazyRepositoryPath(RepositoryPath target)
        {
            super(target.getTarget(), target.getFileSystem());
            this.artifact = target.artifact;
        }

        @Override
        public Artifact getArtifactEntry()
            throws IOException
        {
            Artifact artifact = super.getArtifactEntry();
            if (artifact == NullArtifact.INSTANCE)
            {
                return null;
            }
            if (artifact != null) 
            {
                return artifact;
            }
            
            if (this.getRepository().isGroupRepository() || !RepositoryFiles.isArtifact(this))
            {
                artifact = NullArtifact.INSTANCE;
                
                return null;
            }

            artifact = artifactEntityRepository.findOneArtifact(getRepository().getStorage().getId(),
                                                                getRepository().getId(),
                                                                RepositoryFiles.relativizePath(this));
            if (artifact == null) {
                artifact = NullArtifact.INSTANCE;
                
                return null;                
            }
            
            return artifact;
            // TODO: we should check this restriction 
//            if (Files.exists(this) && !Files.isDirectory(this) && RepositoryFiles.isArtifact(this) && result == null)
//            {
//                throw new IOException(String.format("Corresponding [%s] record not found for path [%s]",
//                                                    ArtifactEntry.class.getSimpleName(), this));
//            }

        }

        @Override
        public RepositoryPath normalize()
        {
            RepositoryPath target = super.normalize();
            return new LazyRepositoryPath(target);
        }
        
    }

    private static class NullArtifact implements Artifact
    {

        private static final Artifact INSTANCE = new NullArtifact();

        @Override
        public String getUuid()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void applyUnfold(Traverser<Vertex> t)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Artifact getHierarchyChild()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Artifact getHierarchyParent()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getStorageId()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setStorageId(String storageId)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getRepositoryId()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setRepositoryId(String repositoryId)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ArtifactCoordinates getArtifactCoordinates()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<ArtifactTag> getTagSet()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, String> getChecksums()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setChecksums(Map<String, String> digestMap)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long getSizeInBytes()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSizeInBytes(Long sizeInBytes)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public LocalDateTime getLastUpdated()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLastUpdated(LocalDateTime lastUpdated)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public LocalDateTime getLastUsed()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLastUsed(LocalDateTime lastUsed)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public LocalDateTime getCreated()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCreated(LocalDateTime created)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Integer getDownloadCount()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDownloadCount(Integer downloadCount)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getArtifactPath()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<ArtifactArchiveListing> getArtifactArchiveListings()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setArtifactArchiveListings(Set<ArtifactArchiveListing> artifactArchiveListings)
        {
            throw new UnsupportedOperationException();
        }

    }

}
