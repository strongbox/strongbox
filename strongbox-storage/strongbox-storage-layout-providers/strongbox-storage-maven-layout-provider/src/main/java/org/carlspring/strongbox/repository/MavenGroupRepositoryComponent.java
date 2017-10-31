package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
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

    public void updateMetadataInRepositoryParents(final String storageId,
                                                  final String repositoryId,
                                                  final String artifactRelativePath,
                                                  final Function<Path, Path> artifactBasePathCalculation)
            throws IOException, XmlPullParserException
    {

        final Repository repository = configurationManagementService.getConfiguration().getStorage(
                storageId).getRepository(repositoryId);
        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final RepositoryPath repositoryAbsolutePath = layoutProvider.resolve(repository);
        final RepositoryPath artifactAbsolutePath = repositoryAbsolutePath.resolve(artifactRelativePath);

        Metadata mergeMetadata;
        try
        {
            mergeMetadata = mavenMetadataManager.readMetadata(artifactBasePathCalculation.apply(artifactAbsolutePath));
        }
        catch (FileNotFoundException ex)
        {
            // there is no metadata file - exit silently
            return;
        }

        updateMetadataInRepositoryParents(repository, artifactRelativePath, artifactBasePathCalculation, mergeMetadata);
    }

    private void updateMetadataInRepositoryParents(final Repository repository,
                                                   final String artifactRelativePath,
                                                   final Function<Path, Path> artifactBasePathCalculation,
                                                   final Metadata mergeMetadata)
            throws IOException
    {
        final List<Repository> groupRepositories = configurationManagementService.getGroupRepositoriesContaining(
                repository.getId());
        if (CollectionUtils.isEmpty(groupRepositories))
        {
            return;
        }
        for (final Repository parent : groupRepositories)
        {
            final LayoutProvider parentLayoutProvider = layoutProviderRegistry.getProvider(parent.getLayout());
            final RepositoryPath parentRepositoryAbsolutePath = parentLayoutProvider.resolve(parent);
            final RepositoryPath parentRepositoryArtifactAbsolutePath = parentRepositoryAbsolutePath.resolve(
                    artifactRelativePath);

            mavenMetadataManager.mergeAndStore(artifactBasePathCalculation.apply(parentRepositoryArtifactAbsolutePath),
                                               mergeMetadata);

            // go higher in the hierarchy
            updateMetadataInRepositoryParents(parent, artifactRelativePath, artifactBasePathCalculation, mergeMetadata);
        }
    }

}
