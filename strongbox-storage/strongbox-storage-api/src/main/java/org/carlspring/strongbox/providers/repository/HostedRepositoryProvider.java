package org.carlspring.strongbox.providers.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class HostedRepositoryProvider extends AbstractRepositoryProvider
{

    private static final Logger logger = LoggerFactory.getLogger(HostedRepositoryProvider.class);

    private static final String ALIAS = "hosted";

    @Inject
    private ArtifactEntryService artifactEntryService;

    @PostConstruct
    @Override
    public void register()
    {
        getRepositoryProviderRegistry().addProvider(ALIAS, this);

        logger.info("Registered repository provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public ArtifactInputStream getInputStream(String storageId,
                                              String repositoryId,
                                              String path)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException
    {
        Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);

        LayoutProvider layoutPtovider = getLayoutProviderRegistry().getProvider(repository.getLayout());
        RepositoryPath repositoryPath = layoutPtovider.resolve(repository).resolve(path);
        return (ArtifactInputStream) Files.newInputStream(repositoryPath);
    }

    @Override
    public ArtifactOutputStream getOutputStream(String storageId,
                                                String repositoryId,
                                                String path)
            throws IOException, NoSuchAlgorithmException
    {
        Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);

        LayoutProvider layoutPtovider = getLayoutProviderRegistry().getProvider(repository.getLayout());
        RepositoryPath repositoryPath = layoutPtovider.resolve(repository).resolve(path);
        return (ArtifactOutputStream) Files.newOutputStream(repositoryPath);
    }

    @Override
    public List<Path> search(String storageId,
                             String repositoryId,
                             Map<String, String> coordinates,
                             int skip,
                             int limit,
                             String orderBy)
    {
        List<Path> result = new LinkedList<>();
        for (ArtifactEntry artifactEntry : artifactEntryService.findByCoordinates(storageId, repositoryId,
                                                                                  coordinates, skip, limit, orderBy,
                                                                                  false))
        {
            ArtifactCoordinates artifactCoordinates = artifactEntry.getArtifactCoordinates();
            Storage storage = getLayoutProviderRegistry().getStorage(storageId);
            Repository repository = storage.getRepository(repositoryId);
            LayoutProvider provider = getLayoutProviderRegistry().getProvider(repository.getLayout());
            try
            {
                result.add(provider.resolve(repository, artifactCoordinates));
            }
            catch (IOException e)
            {
                throw new RuntimeException(
                        String.format("Failed to resolve RepositoryPath: repository-[%s]; coordinates-[%s]",
                                      repository.getId(), artifactCoordinates));
            }
        }
        return result;
    }

    
}
