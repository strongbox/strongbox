package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenGroupRepositoryComponent
{

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private MavenMetadataManager mavenMetadataManager;

    public void deleteMetadataInRepositoryParents(final String storageId,
                                                  final String repositoryId,
                                                  final String artifactRelativePath)
            throws IOException
    {
        final Map<String, MutableBoolean> repositoryArtifactExistence = new HashMap<>();
        deleteMetadataInRepositoryParents(storageId, repositoryId, artifactRelativePath, repositoryArtifactExistence);
    }

    private void deleteMetadataInRepositoryParents(final String storageId,
                                                   final String repositoryId,
                                                   final String artifactRelativePath,
                                                   final Map<String, MutableBoolean> repositoryArtifactExistence)
            throws IOException
    {
        final List<Repository> directParents = configurationManagementService.getGroupRepositoriesContaining(
                repositoryId);
        if (CollectionUtils.isEmpty(directParents))
        {
            return;
        }
        for (final Repository groupRepository : directParents)
        {
            final MutableBoolean artifactExistence = new MutableBoolean();
            checkArtifactExistenceInTheGroupRepositorySubtree(groupRepository, artifactRelativePath,
                                                              repositoryArtifactExistence, artifactExistence);
            if (artifactExistence.isFalse())
            {
                final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(groupRepository.getLayout());
                layoutProvider.deleteMetadata(groupRepository.getStorage().getId(), groupRepository.getId(),
                                              artifactRelativePath);
            }
            // go higher in the hierarchy
            deleteMetadataInRepositoryParents(groupRepository.getStorage().getId(), groupRepository.getId(),
                                              artifactRelativePath,
                                              repositoryArtifactExistence);
        }
    }

    /**
     * This method goes down the group repository tree and lookups for an existence of the artifact in the given path.
     */
    private void checkArtifactExistenceInTheGroupRepositorySubtree(final Repository groupRepository,
                                                                   final String artifactRelativePath,
                                                                   final Map<String, MutableBoolean> repositoryArtifactExistence,
                                                                   final MutableBoolean artifactExistence)
            throws IOException
    {
        for (final String maybeStorageAndRepositoryId : groupRepository.getGroupRepositories())
        {
            final String subStorageId = configurationManager.getStorageId(groupRepository.getStorage(),
                                                                          maybeStorageAndRepositoryId);
            final String subRepositoryId = configurationManager.getRepositoryId(maybeStorageAndRepositoryId);
            final Repository subRepository = configurationManager.getConfiguration().getStorage(
                    subStorageId).getRepository(
                    subRepositoryId);

            final String storageAndRepositoryId = subStorageId + ":" + subRepositoryId;
            repositoryArtifactExistence.putIfAbsent(storageAndRepositoryId, new MutableBoolean());
            if (repositoryArtifactExistence.get(storageAndRepositoryId).isTrue())
            {
                return;
            }

            if (subRepository.isGroupRepository())
            {
                checkArtifactExistenceInTheGroupRepositorySubtree(subRepository, artifactRelativePath,
                                                                  repositoryArtifactExistence, artifactExistence);
                if (artifactExistence.isTrue())
                {
                    repositoryArtifactExistence.get(storageAndRepositoryId).setTrue();
                    return;
                }
            }
            else
            {
                final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(subRepository.getLayout());
                if (layoutProvider.containsPath(subRepository, artifactRelativePath))
                {
                    artifactExistence.setTrue();
                    repositoryArtifactExistence.get(storageAndRepositoryId).setTrue();
                    return;
                }
            }
        }
    }

    public void update(final String storageId,
                       final String repositoryId,
                       final String artifactRelativePath)
            throws Exception
    {

        final Repository repository = configurationManager.getConfiguration().getStorage(storageId).getRepository(
                repositoryId);
        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final RepositoryPath repositoryAbsolutePath = layoutProvider.resolve(repository);
        final RepositoryPath artifactAbsolutePath = repositoryAbsolutePath.resolve(artifactRelativePath);
        final Path artifactBaseAbsolutePath = artifactAbsolutePath.getParent();

        final Metadata metadata = mavenMetadataManager.readMetadata(artifactBaseAbsolutePath.getParent());

        final List<Repository> groupRepositories = configurationManagementService.getGroupRepositoriesContaining(
                repositoryId);
        if (CollectionUtils.isEmpty(groupRepositories))
        {
            return;
        }
        for (Repository parent : groupRepositories)
        {
            final LayoutProvider parentLayoutProvider = layoutProviderRegistry.getProvider(parent.getLayout());
            final RepositoryPath parentRepositoryAbsolutePath = parentLayoutProvider.resolve(parent);
            final RepositoryPath parentRepositoryArtifactAbsolutePath = parentRepositoryAbsolutePath.resolve(
                    artifactRelativePath);
            final Path parentRepositoryArtifactBaseAbsolutePath = parentRepositoryArtifactAbsolutePath.getParent().getParent();

            mavenMetadataManager.mergeAndStore(parentRepositoryArtifactBaseAbsolutePath, metadata);
        }
    }

}
