package org.carlspring.strongbox.providers.repository;


import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.repository.proxied.LocalStorageProxyRepositoryArtifactResolver;
import org.carlspring.strongbox.providers.repository.proxied.ProxyRepositoryArtifactResolver;
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
public class ProxyRepositoryProvider
        extends AbstractRepositoryProvider
{

    private static final Logger logger = LoggerFactory.getLogger(ProxyRepositoryProvider.class);

    private static final String ALIAS = "proxy";

    @Inject
    @LocalStorageProxyRepositoryArtifactResolver.LocalStorageProxyRepositoryArtifactResolverQualifier
    private ProxyRepositoryArtifactResolver proxyRepositoryArtifactResolver;

    @Inject
    private ArtifactEntryService artifactEntryService;
    
    @PostConstruct
    @Override
    public void register()
    {
        repositoryProviderRegistry.addProvider(ALIAS, this);

        logger.info("Registered repository provider '" + getClass().getCanonicalName() +
                    "' with alias '" + ALIAS + "'.");
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
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        return (ArtifactInputStream) proxyRepositoryArtifactResolver.getInputStream(storageId, repositoryId, path);
    }

    @Override
    public ArtifactOutputStream getOutputStream(String storageId,
                                                String repositoryId,
                                                String artifactPath)
            throws IOException,
                   NoSuchAlgorithmException
    {
        throw new UnsupportedOperationException(String.format("Can't write artifact into Proxy repository [%s/%s/%s].",
                                                              storageId, repositoryId, artifactPath));
    }
    
    @Override
    public List<Path> search(String storageId,
                             String repositoryId,
                             Map<String, String> coordinates,
                             int skip,
                             int limit,
                             String orderBy)
    {
        List<Path> result = new LinkedList<Path>();
        
        Storage storage = configurationManager.getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);
        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        
        List<ArtifactEntry> artifactEntryList = artifactEntryService.findByCoordinates(storageId, repositoryId,
                                                                                       coordinates, skip, limit,
                                                                                       orderBy, false);
        
        for (ArtifactEntry artifactEntry : artifactEntryList)
        {
            RepositoryPath repositoryPath;
            try
            {
                repositoryPath = layoutProvider.resolve(repository, artifactEntry.getArtifactCoordinates());
            }
            catch (IOException e)
            {
                logger.error(String.format("Failed to resolve Remote Artifact [%s]", artifactEntry.getArtifactPath()),
                             e);
                continue;
            }
            result.add(repositoryPath);
        }
        
        return result;
    }
    
}
