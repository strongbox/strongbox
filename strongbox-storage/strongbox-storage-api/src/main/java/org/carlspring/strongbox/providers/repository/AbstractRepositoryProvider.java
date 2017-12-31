package org.carlspring.strongbox.providers.repository;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.io.output.CountingOutputStream;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.io.RepositoryInputStream;
import org.carlspring.strongbox.io.RepositoryOutputStream;
import org.carlspring.strongbox.io.RepositoryStreamCallback;
import org.carlspring.strongbox.io.RepositoryStreamContext;
import org.carlspring.strongbox.io.StreamUtils;
import org.carlspring.strongbox.providers.datastore.StorageProviderRegistry;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author carlspring
 */
public abstract class AbstractRepositoryProvider implements RepositoryProvider, RepositoryStreamCallback
{

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

    protected RepositoryInputStream decorate(String storageId,
                                             String repositoryId,
                                             String path,
                                             InputStream is)
    {
        if (is == null || is instanceof RepositoryInputStream)
        {
            return (RepositoryInputStream) is;
        }

        Repository repository = configurationManager.getRepository(storageId, repositoryId);

        return RepositoryInputStream.of(repository, path, is).with(this);
    }

    protected final RepositoryOutputStream decorate(String storageId,
                                                    String repositoryId,
                                                    String path,
                                                    OutputStream os)
    {
        if (os == null || os instanceof RepositoryOutputStream)
        {
            return (RepositoryOutputStream) os;
        }

        Repository repository = configurationManager.getRepository(storageId, repositoryId);

        return RepositoryOutputStream.of(repository, path, os).with(this);
    }

    @Override
    public void onBeforeWrite(RepositoryStreamContext ctx)
    {
        logger.debug(String.format("Writing [%s]", ctx.getPath()));
        
        Repository repository = ctx.getRepository();
        String storageId = repository.getStorage().getId();
        String repositoryId = repository.getId();

        ArtifactEntry artifactEntry = provideArtirfactEntry(storageId, repositoryId,
                                                            ctx.getPath());

        artifactEntry.setStorageId(storageId);
        artifactEntry.setRepositoryId(repositoryId);
        artifactEntry.setArtifactPath(ctx.getPath());

        ArtifactOutputStream aos = StreamUtils.findSource(ArtifactOutputStream.class, (OutputStream) ctx);
        ArtifactCoordinates coordinates = aos.getCoordinates();
        artifactEntry.setArtifactCoordinates(coordinates);

        Date now = new Date();
        artifactEntry.setLastUpdated(now);
        artifactEntry.setLastUsed(now);

        artifactEntryService.save(artifactEntry);
    }

    @Override
    public void onAfterClose(RepositoryStreamContext ctx)
    {
        logger.debug(String.format("Closing [%s]", ctx.getPath()));

        Repository repository = ctx.getRepository();
        String storageId = repository.getStorage().getId();
        String repositoryId = repository.getId();

        ArtifactEntry artifactEntry = provideArtirfactEntry(storageId, repositoryId,
                                                            ctx.getPath());

        CountingOutputStream cos = StreamUtils.findSource(CountingOutputStream.class, (OutputStream) ctx);
        artifactEntry.setSizeInBytes(cos.getByteCount());

        artifactEntryService.save(artifactEntry);
    }

    @Override
    public void onBeforeRead(RepositoryStreamContext ctx)
    {
        logger.debug(String.format("Reading [%s]", ctx.getPath()));

        Repository repository = ctx.getRepository();
        String storageId = repository.getStorage().getId();
        String repositoryId = repository.getId();

        ArtifactEntry artifactEntry = provideArtirfactEntry(storageId, repositoryId,
                                                            ctx.getPath());

        artifactEntry.setLastUsed(new Date());
        artifactEntry.setDownloadCount(artifactEntry.getDownloadCount() + 1);

        artifactEntryService.save(artifactEntry);
    }

    protected ArtifactEntry provideArtirfactEntry(String storageId,
                                                  String repositoryId,
                                                  String path)
    {
        ArtifactEntry artifactEntry = artifactEntryService.findOneArtifact(storageId,
                                                                           repositoryId,
                                                                           path)
                                                          .map(e -> artifactEntryService.lockOne(e.getObjectId()))
                                                          .orElse(new ArtifactEntry());

        return artifactEntry;
    }
}
