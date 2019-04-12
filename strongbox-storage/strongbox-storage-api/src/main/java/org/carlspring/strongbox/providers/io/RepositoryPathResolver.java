package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Objects;

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
    protected ArtifactEntryService artifactEntryService;

    @Inject
    protected RepositoryFileSystemRegistry fileSystemRegistry;

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
        return resolve(repository, c.toPath());
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
        
        return new CachedRepositoryPath(repositoryPath.resolve(path));
    }
    
    private class CachedRepositoryPath extends RepositoryPath
    {

        private CachedRepositoryPath(RepositoryPath target)
        {
            super(target.getTarget(), target.getFileSystem());
        }

        @Override
        public ArtifactEntry getArtifactEntry()
            throws IOException
        {
            if (this.getRepository().isGroupRepository() || !RepositoryFiles.isArtifact(this))
            {
                return null;
            }

            return artifactEntryService.findOneArtifact(getRepository().getStorage().getId(),
                                                        getRepository().getId(),
                                                        RepositoryFiles.relativizePath(this));
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
            return new CachedRepositoryPath(target);
        }
        
    }

}
