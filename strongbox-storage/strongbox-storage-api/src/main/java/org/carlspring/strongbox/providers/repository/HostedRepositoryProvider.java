package org.carlspring.strongbox.providers.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.io.RepositoryInputStream;
import org.carlspring.strongbox.io.RepositoryOutputStream;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
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
    public RepositoryInputStream getInputStream(String storageId,
                                                String repositoryId,
                                                String path)
            throws IOException
    {        
        return newInputStream(resolvePath(storageId, repositoryId, path));
    }
    
    protected RepositoryInputStream newInputStream(RepositoryPath path)
    {
        if (path == null)
        {
            return null;
        }

        Repository repository = path.getFileSystem().getRepository();
        try
        {
            return decorate(repository.getStorage().getId(), repository.getId(), RepositoryFiles.stringValue(path),
                            Files.newInputStream(path));
        }
        catch (IOException e)
        {
            logger.error(String.format("Failed to decorate InputStream for [%s]", path), e);
            return null;
        }
    }

    @Override
    public RepositoryOutputStream getOutputStream(String storageId,
                                                  String repositoryId,
                                                  String path)
            throws IOException, NoSuchAlgorithmException
    {
        Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);

        LayoutProvider layoutPtovider = getLayoutProviderRegistry().getProvider(repository.getLayout());
        RepositoryPath repositoryPath = layoutPtovider.resolve(repository).resolve(path);
        ArtifactOutputStream aos = (ArtifactOutputStream) Files.newOutputStream(repositoryPath);
        
        return decorate(storageId,
                        repositoryId,
                        path,
                        aos);
    }

    @Override
    public List<Path> search(RepositorySearchRequest searchRequest,
                             RepositoryPageRequest pageRequest)
    {
        List<Path> result = new LinkedList<Path>();
        
        Storage storage = configurationManager.getConfiguration().getStorage(searchRequest.getStorageId());
        Repository repository = storage.getRepository(searchRequest.getRepositoryId());
        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        
        List<ArtifactEntry> artifactEntryList = artifactEntryService.findArtifactList(searchRequest.getStorageId(),
                                                                                      searchRequest.getRepositoryId(),
                                                                                      searchRequest.getCoordinates(),
                                                                                      searchRequest.getTagSet(),
                                                                                      pageRequest.getSkip(),
                                                                                      pageRequest.getLimit(),
                                                                                      pageRequest.getOrderBy(), false);
        
        for (ArtifactEntry artifactEntry : artifactEntryList)
        {
            RepositoryPath repositoryPath;
            try
            {
                RootRepositoryPath rootRepositoryPath = layoutProvider.resolve(repository);
                repositoryPath = rootRepositoryPath.resolve(artifactEntry);
            }
            catch (Exception e)
            {
                logger.error(String.format("Failed to resolve Artifact [%s]", artifactEntry.getArtifactCoordinates()),
                             e);
                continue;
            }
            result.add(repositoryPath);
        }
        
        return result;
    }

    @Override
    public Long count(RepositorySearchRequest searchRequest)
    {
        return artifactEntryService.countArtifacts(searchRequest.getStorageId(), searchRequest.getRepositoryId(),
                                                   searchRequest.getCoordinates(), searchRequest.isStrict());
    }
    
    @Override
    public RepositoryPath resolvePath(String storageId,
                                  String repositoryId,
                                  String path) 
           throws IOException
    {
        Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);

        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final RepositoryPath artifactPath = layoutProvider.resolve(repository).resolve(path);

        logger.debug(" -> Checking local cache for {} ...", artifactPath);
        if (layoutProvider.containsPath(repository, path))
        {
            logger.debug("The artifact {} was found in the local cache", artifactPath);
            return artifactPath;
        }

        logger.debug("The artifact {} was not found in the local cache", artifactPath);
        
        return null;
    }
}
