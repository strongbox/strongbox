package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.repository.proxied.LocalStorageProxyRepositoryArtifactResolver;
import org.carlspring.strongbox.providers.repository.proxied.ProxyRepositoryArtifactResolver;
import org.carlspring.strongbox.providers.repository.proxied.SimpleProxyRepositoryArtifactResolver;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenArtifactFetchedFromRemoteEventListener
        extends BaseMavenArtifactEventListener
{

    @Inject
    @SimpleProxyRepositoryArtifactResolver.SimpleProxyRepositoryArtifactResolverQualifier
    private ProxyRepositoryArtifactResolver simpleProxyRepositoryArtifactResolver;

    @Inject
    @LocalStorageProxyRepositoryArtifactResolver.LocalStorageProxyRepositoryArtifactResolverQualifier
    private ProxyRepositoryArtifactResolver localStorageProxyRepositoryArtifactResolver;

    @Override
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
            logger.error("Unable to resolve artifact metadata of file " + event.getPath() + " of repository " +
                         getRepository(event).getId(), e);
        }
    }

    private void downloadArtifactMetadataAtArtifactIdLevelFromRemote(RepositoryPath metadataRelativePath)
            throws Exception
    {
        getMetadataInputStreamWithCallback(localStorageProxyRepositoryArtifactResolver, metadataRelativePath,
                                           IOUtils::closeQuietly);
    }

    private void downloadArtifactMetadataAtArtifactIdLevelFromRemoteAndMergeWithLocal(final RepositoryPath artifactAbsolutePath,
                                                                                      final RepositoryPath metadataRelativePath)
            throws Exception
    {

        final MutableObject<Exception> operationException = new MutableObject<>();
        final Repository repository = artifactAbsolutePath.getFileSystem().getRepository();
        final Storage storage = repository.getStorage();
        
        getMetadataInputStreamWithCallback(simpleProxyRepositoryArtifactResolver, metadataRelativePath, is ->
        {
            try
            {
                final MavenArtifact localArtifact = MavenArtifactUtils.convertPathToArtifact(RepositoryFiles.stringValue(artifactAbsolutePath));
                localArtifact.setPath(artifactAbsolutePath);

                final Metadata metadata = artifactMetadataService.getMetadata(is);
                artifactMetadataService.mergeMetadata(storage.getId(), repository.getId(), localArtifact,
                                                      metadata);
            }
            catch (final Exception e)
            {
                operationException.setValue(e);
            }
            finally
            {
                ResourceCloser.close(is, logger);
            }
        });

        if (operationException.getValue() != null)
        {
            throw operationException.getValue();
        }
    }

    private void getMetadataInputStreamWithCallback(final ProxyRepositoryArtifactResolver proxyRepositoryArtifactResolver,
                                                    final RepositoryPath metadataPath,
                                                    final Consumer<InputStream> callback)
            throws Exception
    {

        final InputStream metadataIs = proxyRepositoryArtifactResolver.getInputStream(metadataPath);
        callback.accept(metadataIs);
    }
}
