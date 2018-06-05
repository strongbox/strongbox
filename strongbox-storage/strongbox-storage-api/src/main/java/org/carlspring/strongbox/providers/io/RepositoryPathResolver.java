package org.carlspring.strongbox.providers.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
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

        RepositoryFileSystemFactory fileSystemFactory = fileSystemRegistry.lookupRepositoryFileSystemFactory(repository);

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

        return artifactEntryService.findOneArtifact(repository.getStorage().getId(),
                                                    repository.getId(), path)
                                   .map(e -> repositoryPath.resolve(e))
                                   .orElseGet(() -> repositoryPath.resolve(path));

    }

}
