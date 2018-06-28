package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.data.criteria.Expression.ExpOperator;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.io.*;
import org.carlspring.strongbox.providers.datastore.StorageProviderRegistry;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.repository.RepositoryProvider;
import org.carlspring.strongbox.providers.repository.RepositoryProviderRegistry;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.io.output.CountingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * @author carlspring
 */
@Transactional
public abstract class AbstractRepositoryProvider implements RepositoryProvider, RepositoryStreamCallback
{

    public static final int CACHE_INVALIDATE_INTERVAL = 60000;
    private static final Logger logger = LoggerFactory.getLogger(AbstractRepositoryProvider.class);
    @Inject
    protected RepositoryProviderRegistry repositoryProviderRegistry;

    @Inject
    protected LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    protected StorageProviderRegistry storageProviderRegistry;

    @Inject
    protected ConfigurationManager configurationManager;

    @Inject
    protected ArtifactEntryService artifactEntryService;
    
    @Inject
    protected ArtifactTagService artifactTagService;
    
    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    public RepositoryProviderRegistry getRepositoryProviderRegistry()
    {
        return repositoryProviderRegistry;
    }

    public void setRepositoryProviderRegistry(RepositoryProviderRegistry repositoryProviderRegistry)
    {
        this.repositoryProviderRegistry = repositoryProviderRegistry;
    }

    public LayoutProviderRegistry getLayoutProviderRegistry()
    {
        return layoutProviderRegistry;
    }

    public void setLayoutProviderRegistry(LayoutProviderRegistry layoutProviderRegistry)
    {
        this.layoutProviderRegistry = layoutProviderRegistry;
    }

    public StorageProviderRegistry getStorageProviderRegistry()
    {
        return storageProviderRegistry;
    }

    public void setStorageProviderRegistry(StorageProviderRegistry storageProviderRegistry)
    {
        this.storageProviderRegistry = storageProviderRegistry;
    }

    public ConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }
    
    @Override
    public RepositoryInputStream getInputStream(Path path)
        throws IOException
    {
        if (path == null)
        {
            return null;
        }
        Assert.isInstanceOf(RepositoryPath.class, path);
        RepositoryPath repositoryPath = (RepositoryPath) path;

        return decorate((RepositoryPath) path,
                        getInputStreamInternal(repositoryPath));

    }

    protected abstract InputStream getInputStreamInternal(RepositoryPath repositoryPath)
        throws IOException;

    protected RepositoryInputStream decorate(RepositoryPath repositoryPath,
                                             InputStream is)
    {
        if (is == null || is instanceof RepositoryInputStream)
        {
            return (RepositoryInputStream) is;
        }

        return RepositoryInputStream.of(repositoryPath, is).with(this);
    }

    @Override
    public RepositoryOutputStream getOutputStream(Path path)
        throws IOException
    {
        Assert.isInstanceOf(RepositoryPath.class, path);
        OutputStream os = getOutputStreamInternal((RepositoryPath) path);
        
        return decorate((RepositoryPath) path, os);
    }
    
    protected abstract OutputStream getOutputStreamInternal(RepositoryPath repositoryPath)
        throws IOException;

    protected final RepositoryOutputStream decorate(RepositoryPath repositoryPath,
                                                    OutputStream os) throws IOException
    {
        if (os == null || os instanceof RepositoryOutputStream)
        {
            return (RepositoryOutputStream) os;
        }

        return RepositoryOutputStream.of(repositoryPath, os).with(this);
    }

    @Override
    public void onBeforeWrite(RepositoryStreamContext ctx) throws IOException
    {
        RepositoryPath repositoryPath = (RepositoryPath) ctx.getPath();
        logger.debug(String.format("Writing [%s]", repositoryPath));
        
        if (!RepositoryFiles.isArtifact(repositoryPath))
        {
            return;
        }

        Repository repository = repositoryPath.getRepository();
        String storageId = repository.getStorage().getId();
        String repositoryId = repository.getId();

        ArtifactEntry artifactEntry = provideArtifactEntry(repositoryPath);
        Date now = new Date();
        
        if (artifactEntry.getUuid() != null && now.getTime() - artifactEntry.getLastUpdated().getTime() < 60000)
        {
            return;
        }
        
        artifactEntry.setStorageId(storageId);
        artifactEntry.setRepositoryId(repositoryId);

        ArtifactOutputStream aos = StreamUtils.findSource(ArtifactOutputStream.class, (OutputStream) ctx);
        ArtifactCoordinates coordinates = aos.getCoordinates();
        artifactEntry.setArtifactCoordinates(coordinates);

        artifactEntry.setLastUpdated(now);
        artifactEntry.setLastUsed(now);

        artifactEntry = artifactEntryService.save(artifactEntry, true);
        
        repositoryPath.artifactEntry = artifactEntry;
    }

    @Override
    public void onAfterClose(RepositoryStreamContext ctx) throws IOException
    {
        RepositoryPath repositoryPath = (RepositoryPath) ctx.getPath();
        logger.debug(String.format("Closing [%s]", repositoryPath));
        
        if (!RepositoryFiles.isArtifact(repositoryPath) || !Files.exists(repositoryPath))
        {
            return;
        }
        ArtifactEntry artifactEntry = provideArtifactEntry(repositoryPath);
        Assert.notNull(artifactEntry.getUuid(),
                       String.format("Invalid [%s] for [%s]", ArtifactEntry.class.getSimpleName(),
                                     repositoryPath));

        CountingOutputStream cos = StreamUtils.findSource(CountingOutputStream.class, (OutputStream) ctx);
        long size = cos.getByteCount();
        if (size != artifactEntry.getSizeInBytes())
        {
            artifactEntry.setSizeInBytes(size);

            artifactEntryService.save(artifactEntry);
        }
    }

    @Override
    public void onBeforeRead(RepositoryStreamContext ctx) throws IOException
    {
        RepositoryPath repositoryPath = (RepositoryPath) ctx.getPath();
        logger.debug(String.format("Reading %s", repositoryPath));
        
        if (!RepositoryFiles.isArtifact(repositoryPath))
        {
            return;
        }
        ArtifactEntry artifactEntry = provideArtifactEntry(repositoryPath);

        Assert.notNull(artifactEntry.getUuid(),
                       String.format("Invalid [%s] for [%s]",
                                     ArtifactEntry.class.getSimpleName(),
                                     ctx.getPath()));

        Date now = new Date();
        
        if (artifactEntry.getUuid() != null
                && now.getTime() - artifactEntry.getLastUsed().getTime() < CACHE_INVALIDATE_INTERVAL)
        {
            return;
        }
        
        artifactEntry.setLastUsed(now);
        artifactEntry.setDownloadCount(artifactEntry.getDownloadCount() + 1);

        artifactEntryService.save(artifactEntry);
    }

    protected ArtifactEntry provideArtifactEntry(RepositoryPath repositoryPath) throws IOException
    {
        return Optional.ofNullable(repositoryPath.getArtifactEntry())
                       .map(e -> artifactEntryService.lockOne(e.getObjectId()))
                       .orElse(new ArtifactEntry());
    }
    
    @Override
    public RepositoryPath fetchPath(Path repositoryPath)
        throws IOException
    {
        return fetchPath((RepositoryPath)repositoryPath);
    }

    protected abstract RepositoryPath fetchPath(RepositoryPath repositoryPath) throws IOException;
    
    protected Predicate createPredicate(String storageId,
                                        String repositoryId,
                                        Predicate predicate)
    {
        Predicate result = Predicate.of(ExpOperator.EQ.of("storageId",
                                                          storageId))
                                    .and(Predicate.of(ExpOperator.EQ.of("repositoryId",
                                                                        repositoryId)));
        if (predicate.isEmpty())
        {
            return result;
        }
        return result.and(predicate);
    }

    protected Selector<ArtifactEntry> createSelector(String storageId,
                                                     String repositoryId,
                                                     Predicate p)
    {
        Selector<ArtifactEntry> selector = new Selector<>(ArtifactEntry.class);
        selector.where(createPredicate(storageId, repositoryId, p));
        
        return selector;
    }
    
}
