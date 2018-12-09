package org.carlspring.strongbox.storage.validation.resource;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.storage.ArtifactResolutionException;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

import javax.inject.Inject;
import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author mtodorov
 */
@Component("artifactOperationsValidator")
public class ArtifactOperationsValidator
{

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    public ArtifactOperationsValidator()
    {
    }

    public void validate(RepositoryPath repositoryPath)
            throws ArtifactResolutionException
    {
        checkArtifactPath(repositoryPath);
        
        Repository repository = repositoryPath.getRepository();
        Storage storage = repository.getStorage();
        
        checkStorageExists(storage.getId());
        checkRepositoryExists(storage.getId(), repository.getId());
    }

    public void checkStorageExists(String storageId)
            throws ArtifactResolutionException
    {
        if (storageId == null)
        {
            throw new ArtifactResolutionException("No storage specified.");
        }

        if (getConfiguration().getStorage(storageId) == null)
        {
            throw new ArtifactResolutionException("Storage " + storageId + " does not exist.");
        }
    }

    public void checkRepositoryExists(String storageId,
                                      String repositoryId)
            throws ArtifactResolutionException
    {
        if (repositoryId == null)
        {
            throw new ArtifactResolutionException("No repository specified.");
        }

        if (getConfiguration().getStorage(storageId)
                              .getRepository(repositoryId) == null)
        {
            throw new ArtifactResolutionException("Repository " + repositoryId + " does not exist.");
        }
    }

    private void checkArtifactPath(RepositoryPath repositoryPath)
            throws ArtifactResolutionException
    {
        if (repositoryPath == null)
        {
            throw new ArtifactResolutionException("No artifact path specified.");
        }
    }

    public void checkAllowsDeployment(Repository repository)
            throws ArtifactStorageException
    {
        if (!repository.allowsDeployment() ||
            RepositoryTypeEnum.GROUP.getType().equals(repository.getType()) ||
            RepositoryTypeEnum.PROXY.getType().equals(repository.getType()))
        {
            // It should not be possible to write artifacts to:
            // - a repository that doesn't allow the deployment of artifacts
            // - a proxy repository
            // - a group repository
            //
            // NOTE:
            // - A proxy repository should only serve artifacts that already exist in the cache, or the remote host.
            // - Both the ProxyRepositoryProvider and GroupRepositoryProvider need to have an implementation of the
            //   getOutputStream(...) method, which is why this check is performed here instead.

            throw new ArtifactStorageException("Deployment of artifacts to " + repository.getType() +
                                               " repositories is not allowed!");
        }
    }

    public void checkAllowsRedeployment(Repository repository,
                                        ArtifactCoordinates coordinates)
            throws IOException
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, coordinates);
        if (RepositoryFiles.artifactExists(repositoryPath) && !repository.allowsRedeployment())
        {
            throw new ArtifactStorageException("Re-deployment of artifacts to " +
                                               repository.getStorage().getId() + ":" + repository.getId() +
                                               " repository is not allowed!");
        }
    }

    public void checkAllowsDeletion(Repository repository)
            throws ArtifactStorageException
    {
        if (repository != null && !repository.allowsDeletion())
        {
            throw new ArtifactStorageException("Deleting artifacts from " + repository.getType() +
                                               " repository is not allowed!");
        }
    }

    void checkArtifactSize(String storageId,
                           String repositoryId,
                           MultipartFile uploadedFile)
            throws ArtifactResolutionException
    {
        if (uploadedFile.isEmpty() || uploadedFile.getSize() == 0)
        {
            throw new ArtifactResolutionException("Uploaded file is empty.");
        }

        Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);
        long artifactMaxSize = repository.getArtifactMaxSize();

        if (artifactMaxSize > 0 && uploadedFile.getSize() > artifactMaxSize)
        {
            throw new ArtifactResolutionException("The size of the artifact exceeds the maximum size accepted by " +
                                                  "this repository (" + uploadedFile.getSize() + "/" +
                                                  artifactMaxSize + ").");
        }
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
