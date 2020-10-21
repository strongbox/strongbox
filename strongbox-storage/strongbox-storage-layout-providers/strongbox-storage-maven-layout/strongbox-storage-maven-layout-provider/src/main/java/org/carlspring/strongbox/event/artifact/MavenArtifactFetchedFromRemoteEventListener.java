package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.event.AsyncEventListener;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathLock;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.repository.proxied.ProxyRepositoryArtifactResolver;
import org.carlspring.strongbox.providers.repository.proxied.ProxyRepositoryInputStream;
import org.carlspring.strongbox.providers.repository.proxied.RestArtifactResolverFactory;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;

import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.Lock;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenArtifactFetchedFromRemoteEventListener
        extends BaseMavenArtifactEventListener
{

    @Inject
    private ProxyRepositoryArtifactResolver proxyRepositoryArtifactResolver;

    @Inject
    protected RepositoryPathLock repositoryPathLock;
    
    @Inject
    protected RestArtifactResolverFactory restArtifactResolverFactory;
    
    @AsyncEventListener
    public void handle(final ArtifactEvent<RepositoryPath> event)
    {
        final Repository repository = getRepository(event);

        if (!Maven2LayoutProvider.ALIAS.equals(repository.getLayout()))
        {
            return;
        }

        if (event.getType() != ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_FETCHED_FROM_REMOTE.getType())
        {
            return;
        }

        resolveArtifactMetadataAtArtifactIdLevel(event);
        updateMetadataInGroupsContainingRepository(event, path -> path.getParent().getParent());
    }

    private void resolveArtifactMetadataAtArtifactIdLevel(final ArtifactEvent<RepositoryPath> event)
    {
        try
        {
            final RepositoryPath artifactAbsolutePath = event.getPath().toAbsolutePath();
            final RepositoryPath artifactBaseAbsolutePath = artifactAbsolutePath.getParent();

            final RepositoryPath metadataAbsolutePath = (RepositoryPath) MetadataHelper.getMetadataPath(
                    artifactBaseAbsolutePath,
                    null,
                    MetadataType.PLUGIN_GROUP_LEVEL);
            try
            {
                mavenMetadataManager.readMetadata(artifactBaseAbsolutePath.getParent());
            }
            catch (final FileNotFoundException ex)
            {
                downloadArtifactMetadataAtArtifactIdLevelFromRemote(metadataAbsolutePath);
                return;
            }

            downloadArtifactMetadataAtArtifactIdLevelFromRemoteAndMergeWithLocal(artifactAbsolutePath,
                                                                                 metadataAbsolutePath);
        }
        catch (Exception e)
        {
            logger.error("Unable to resolve artifact metadata of file {} of repository {}",
                         event.getPath(), getRepository(event).getId(), e);
        }
    }

    private void downloadArtifactMetadataAtArtifactIdLevelFromRemote(RepositoryPath metadataRelativePath)
            throws Exception
    {
        proxyRepositoryArtifactResolver.fetchRemoteResource(metadataRelativePath);
    }

    private void downloadArtifactMetadataAtArtifactIdLevelFromRemoteAndMergeWithLocal(final RepositoryPath artifactAbsolutePath,
                                                                                      final RepositoryPath metadataPath)
            throws Exception
    {
        Repository repository = metadataPath.getRepository();
        RemoteRepository remoteRepository = repository.getRemoteRepository();
        RestArtifactResolver client = restArtifactResolverFactory.newInstance(remoteRepository);
        
        Lock lock = repositoryPathLock.lock(metadataPath).writeLock();
        lock.lock();

        try (InputStream is = new BufferedInputStream(new ProxyRepositoryInputStream(client, metadataPath)))
        {
            mergeMetadata(artifactAbsolutePath, is);
        } 
        finally
        {
            lock.unlock();
        }
        
    }

    private void mergeMetadata(RepositoryPath repositoryPath,
                               InputStream remoteMetadataIs)
        throws IOException,
        NoSuchAlgorithmException,
        XmlPullParserException,
        ProviderImplementationException
    {
        MavenArtifact localArtifact = MavenArtifactUtils.convertPathToArtifact(repositoryPath);
        Metadata metadata = artifactMetadataService.getMetadata(remoteMetadataIs);
        
        artifactMetadataService.mergeMetadata(localArtifact, metadata);

    }

}
