package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class RepositoryPathResolver
{

    @Inject
    protected LayoutProviderRegistry layoutProviderRegistry;
    
    @Inject
    protected ConfigurationManagementService configurationManagementService;
    
    @Inject
    protected ArtifactEntryService artifactEntryService;

    public RepositoryPath resolve(String storageId,
                                  String repositoryId,
                                  String... paths)
    {
        Storage storage = configurationManagementService.getConfiguration().getStorage(storageId);
        Objects.requireNonNull(storage, String.format("Storage [%s] not found", storageId));
        
        return resolve(storage.getRepository(repositoryId), paths);
    }
    
    public RepositoryPath resolve(final Repository repository,
                                  final String... paths)
    {
        Objects.requireNonNull(repository, "Repository should be provided");

        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final RootRepositoryPath repositoryPath = layoutProvider.resolve(repository);
        if (paths == null || paths.length == 0)
        {
            return repositoryPath;
        }
        
        if (paths.length == 1)
        {
            String path = paths[0];
            
            return artifactEntryService.findOneArtifact(repository.getStorage().getId(),
                                                        repository.getId(), path)
                                       .map(e -> repositoryPath.resolve(e))
                                       .orElseGet(() -> repositoryPath.resolve(path));
        }
        
        RepositoryPath result = repositoryPath;
        
        for (final String path : paths)
        {
            result = result.resolve(path);
        }
        
        return result;
    }


}
