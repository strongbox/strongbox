package org.carlspring.strongbox.providers.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.io.output.CountingOutputStream;
import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.data.criteria.Expression.ExpOperator;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.domain.ArtifactIdGroup;
import org.carlspring.strongbox.domain.ArtifactIdGroupEntity;
import org.carlspring.strongbox.domain.ArtifactTagEntity;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.io.LayoutOutputStream;
import org.carlspring.strongbox.io.RepositoryStreamCallback;
import org.carlspring.strongbox.io.RepositoryStreamReadContext;
import org.carlspring.strongbox.io.RepositoryStreamWriteContext;
import org.carlspring.strongbox.io.StreamUtils;
import org.carlspring.strongbox.providers.io.RepositoryStreamSupport.RepositoryInputStream;
import org.carlspring.strongbox.providers.io.RepositoryStreamSupport.RepositoryOutputStream;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.repository.RepositoryProvider;
import org.carlspring.strongbox.providers.repository.RepositoryProviderRegistry;
import org.carlspring.strongbox.repositories.ArtifactIdGroupRepository;
import org.carlspring.strongbox.repositories.ArtifactRepository;
import org.carlspring.strongbox.services.ArtifactIdGroupService;
import org.carlspring.strongbox.services.ArtifactTagService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

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
    protected ConfigurationManager configurationManager;

    @Inject
    protected ArtifactRepository artifactRepository;
    
    @Inject
    private ArtifactIdGroupService artifactIdGroupService;
    
    @Inject
    private ArtifactIdGroupRepository artifactIdGroupRepository;
    
    @Inject
    protected ArtifactEventListenerRegistry artifactEventListenerRegistry;

    @Inject
    protected ApplicationEventPublisher eventPublisher;
    
    @Inject
    private RepositoryPathLock repositoryPathLock;
    
    @Inject
    private PlatformTransactionManager transactionManager;
    
    @Inject
    private ArtifactTagService artifactTagService;
    
    protected Configuration getConfiguration()
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
                                             InputStream is) throws IOException
    {
        if (is instanceof RepositoryInputStream)
        {
            return (RepositoryInputStream) is;
        }

        return new RepositoryStreamSupport(repositoryPathLock.lock(repositoryPath), this, transactionManager).
               new RepositoryInputStream(repositoryPath, is);
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

        return new RepositoryStreamSupport(repositoryPathLock.lock(repositoryPath), this, transactionManager).
               new RepositoryOutputStream(repositoryPath, os);
    }

    @Override
    public void onBeforeWrite(RepositoryStreamWriteContext ctx) throws IOException
    {
        RepositoryPath repositoryPath = (RepositoryPath) ctx.getPath();
        logger.debug("Writing [{}]", repositoryPath);
        
        if (!RepositoryFiles.isArtifact(repositoryPath))
        {
            return;
        }

        Repository repository = repositoryPath.getRepository();
        String storageId = repository.getStorage().getId();
        String repositoryId = repository.getId();

        Artifact artifactEntry = provideArtifact(repositoryPath);
        if (!shouldStoreArtifact(artifactEntry))
        {
            return;
        }
        
        artifactEntry.setStorageId(storageId);
        artifactEntry.setRepositoryId(repositoryId);

        ArtifactCoordinates coordinates = RepositoryFiles.readCoordinates(repositoryPath);
        artifactEntry.setArtifactCoordinates(coordinates);

        LocalDateTime now = LocalDateTime.now();
        artifactEntry.setCreated(now);
        artifactEntry.setLastUpdated(now);
        artifactEntry.setLastUsed(now);

        repositoryPath.artifact = artifactEntry;
    }

    @Override
    public void onAfterWrite(RepositoryStreamWriteContext ctx) throws IOException
    {
        RepositoryPath repositoryPath = (RepositoryPath) ctx.getPath();
        logger.debug("Complete writing [{}]", repositoryPath);
        
        if (RepositoryFiles.isArtifact(repositoryPath)) {
            if (ctx.getArtifactExists())
            {
                artifactEventListenerRegistry.dispatchArtifactUpdatedEvent(repositoryPath);
            }
            else
            {
                artifactEventListenerRegistry.dispatchArtifactStoredEvent(repositoryPath);
            }            
        } else if (RepositoryFiles.isMetadata(repositoryPath))
        {
            artifactEventListenerRegistry.dispatchArtifactMetadataStoredEvent(repositoryPath);
        }
    }

    @Override
    public void onBeforeRead(RepositoryStreamReadContext ctx)
        throws IOException
    {
        RepositoryPath repositoryPath = (RepositoryPath) ctx.getPath();
        logger.debug("Reading {}", repositoryPath);

        if (!RepositoryFiles.isArtifact(repositoryPath))
        {
            return;
        }
        
        artifactEventListenerRegistry.dispatchArtifactDownloadingEvent(repositoryPath);
    }
    
    @Override
    public void onAfterRead(RepositoryStreamReadContext ctx)
    {
        RepositoryPath repositoryPath = (RepositoryPath) ctx.getPath();
        logger.debug("Complete reading [{}]", repositoryPath);
        
        artifactEventListenerRegistry.dispatchArtifactDownloadedEvent(repositoryPath);
    }

    @Override
    public void commit(RepositoryStreamWriteContext ctx) throws IOException
    {
        RepositoryPath repositoryPath = (RepositoryPath) ctx.getPath();
        Artifact artifact = repositoryPath.getArtifactEntry();
        if (artifact == null)
        {
            return;
        }

        Repository repository = repositoryPath.getRepository();
        Storage storage = repository.getStorage();
        ArtifactCoordinates coordinates = RepositoryFiles.readCoordinates(repositoryPath);
        
        CountingOutputStream cos = StreamUtils.findSource(CountingOutputStream.class, ctx.getStream());
        artifact.setSizeInBytes(cos.getByteCount());

        LayoutOutputStream los = StreamUtils.findSource(LayoutOutputStream.class, ctx.getStream());
        artifact.setChecksums(los.getDigestMap());
        
        ArtifactTag lastVersionTag = artifactTagService.findOneOrCreate(ArtifactTagEntity.LAST_VERSION);

        ArtifactIdGroup artifactGroup = artifactIdGroupRepository.findArtifactsGroupWithTag(storage.getId(),
                                                                                            repository.getId(),
                                                                                            coordinates.getId(),
                                                                                            Optional.of(lastVersionTag))
                                                                 .orElseGet(() -> new ArtifactIdGroupEntity(storage.getId(),
                                                                                                            repository.getId(),
                                                                                                            coordinates.getId()));
        ArtifactCoordinates lastVersion = artifactIdGroupService.addArtifactToGroup(artifactGroup, artifact);
        logger.debug("Last version for group [{}] is [{}] with [{}]",
                     artifactGroup.getName(),
                     lastVersion.getVersion(),
                     lastVersion.getPath());
        
        artifactIdGroupRepository.merge(artifactGroup);
    }

    protected Artifact provideArtifact(RepositoryPath repositoryPath) throws IOException
    {
        return Optional.ofNullable(repositoryPath.getArtifactEntry())
                       .orElse(new ArtifactEntity(repositoryPath.getStorageId(), repositoryPath.getRepositoryId(),
                               RepositoryFiles.readCoordinates(repositoryPath)));
    }
    
    protected boolean shouldStoreArtifact(Artifact artifactEntry)
    {
        return artifactEntry.getNativeId() == null;
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

    protected Selector<ArtifactEntity> createSelector(String storageId,
                                                     String repositoryId,
                                                     Predicate p)
    {
        Selector<ArtifactEntity> selector = new Selector<>(ArtifactEntity.class);
        selector.where(createPredicate(storageId, repositoryId, p));
        
        return selector;
    }

}
