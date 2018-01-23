package org.carlspring.strongbox.providers.repository;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.event.CommonEventListenerRegistry;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.io.RepositoryInputStream;
import org.carlspring.strongbox.io.RepositoryOutputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.repository.event.RemoteRepositorySearchEvent;
import org.carlspring.strongbox.providers.repository.proxied.LocalStorageProxyRepositoryArtifactResolver;
import org.carlspring.strongbox.providers.repository.proxied.ProxyRepositoryArtifactResolver;
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
    private HostedRepositoryProvider hostedRepositoryProvider;
    
    @Inject
    private CommonEventListenerRegistry commonEventListenerRegistry;

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
    public RepositoryInputStream getInputStream(String storageId,
                                                String repositoryId,
                                                String path)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        InputStream is = proxyRepositoryArtifactResolver.getInputStream(storageId, repositoryId, path);
        return decorate(storageId, repositoryId, path, is);
    }

    @Override
    public RepositoryOutputStream getOutputStream(String storageId,
                                                  String repositoryId,
                                                  String artifactPath)
            throws IOException,
                   NoSuchAlgorithmException
    {
        Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);

        LayoutProvider layoutPtovider = getLayoutProviderRegistry().getProvider(repository.getLayout());
        RepositoryPath repositoryPath = layoutPtovider.resolve(repository).resolve(artifactPath);
        ArtifactOutputStream aos = (ArtifactOutputStream) Files.newOutputStream(repositoryPath);
        
        return decorate(storageId, repositoryId, artifactPath, aos);
    }
    
    @Override
    public List<Path> search(RepositorySearchRequest searchRequest,
                             RepositoryPageRequest pageRequest)
    {
    	commonEventListenerRegistry.dispatchEvent(new RemoteRepositorySearchEvent(searchRequest, pageRequest));
    	
        return hostedRepositoryProvider.search(searchRequest, pageRequest);
    }
    
    @Override
    public Long count(RepositorySearchRequest searchRequest)
    {
        return hostedRepositoryProvider.count(searchRequest);
    }
    
    
    protected ArtifactEntry provideArtirfactEntry(String storageId,
                                                  String repositoryId,
                                                  String path)
    {
        RemoteArtifactEntry artifactEntry = Optional.of(super.provideArtirfactEntry(storageId, repositoryId, path))
                                                    .map(e -> e.getObjectId() == null ? new RemoteArtifactEntry()
                                                            : (RemoteArtifactEntry) e)
                                                    .get();
        artifactEntry.setIsCached(Boolean.TRUE);

        return artifactEntry;
    }
    
    @Override
    public RepositoryPath resolvePath(String storageId,
                                      String repositoryId,
                                      String artifactPath)
        throws IOException
    {
        return Optional.ofNullable(hostedRepositoryProvider.resolvePath(storageId, repositoryId, artifactPath))
                       .orElse(resolvePathForceFetch(storageId, repositoryId, artifactPath));
    }

    public RepositoryPath resolvePathForceFetch(String storageId,
                                                String repositoryId,
                                                String artifactPath)
    {
        try
        {
            Optional.ofNullable(proxyRepositoryArtifactResolver.getInputStream(storageId, repositoryId, artifactPath))
                    .ifPresent(s -> IOUtils.closeQuietly(s));
            return hostedRepositoryProvider.resolvePath(storageId, repositoryId, artifactPath);
        }
        catch (Exception e)
        {
            logger.error(String.format("Failed to resolve Path for prixied artifact [%s]/[%s]/[%s]", storageId,
                                       repositoryId, artifactPath),
                         e);
            return null;
        }
    }
}
