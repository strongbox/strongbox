package org.carlspring.strongbox.event.artifact;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.repository.proxied.LocalStorageProxyRepositoryArtifactResolver;
import org.carlspring.strongbox.providers.repository.proxied.ProxyRepositoryArtifactResolver;
import org.carlspring.strongbox.providers.repository.proxied.SimpleProxyRepositoryArtifactResolver;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.maven.artifact.Artifact;
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
    public void handle(final ArtifactEvent event)
    {
        final Repository repository = getRepository(event);

        if (!RepositoryLayoutEnum.MAVEN_2.getLayout().equals(repository.getLayout()))
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

    private void resolveArtifactMetadataAtArtifactIdLevel(final ArtifactEvent event)
    {
        try
        {
            final Repository repository = getRepository(event);
            final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

            final RepositoryPath repositoryAbsolutePath = layoutProvider.resolve(repository);
            final RepositoryPath artifactAbsolutePath = repositoryAbsolutePath.resolve(event.getPath());
            final RepositoryPath artifactBaseAbsolutePath = artifactAbsolutePath.getParent();

            final RepositoryPath metadataAbsolutePath = (RepositoryPath) MetadataHelper.getMetadataPath(
                    artifactBaseAbsolutePath,
                    null,
                    MetadataType.PLUGIN_GROUP_LEVEL);
            final Path metadataRelativePath = metadataAbsolutePath.relativize();

            try
            {
                mavenMetadataManager.readMetadata(artifactBaseAbsolutePath.getParent());
            }
            catch (final FileNotFoundException ex)
            {
                downloadArtifactMetadataAtArtifactIdLevelFromRemote(event, metadataRelativePath);
                return;
            }

            downloadArtifactMetadataAtArtifactIdLevelFromRemoteAndMergeWithLocal(event, artifactAbsolutePath,
                                                                                 metadataRelativePath);
        }
        catch (Exception e)
        {
            logger.error("Unable to resolve artifact metadata of file " + event.getPath() + " of repository " +
                         event.getRepositoryId(), e);
        }
    }

    private void downloadArtifactMetadataAtArtifactIdLevelFromRemote(final ArtifactEvent event,
                                                                     final Path metadataRelativePath)
            throws Exception
    {
        getMetadataInputStreamWithCallback(localStorageProxyRepositoryArtifactResolver, metadataRelativePath, event,
                                           IOUtils::closeQuietly);
    }

    private void downloadArtifactMetadataAtArtifactIdLevelFromRemoteAndMergeWithLocal(final ArtifactEvent event,
                                                                                      final Path artifactAbsolutePath,
                                                                                      final Path metadataRelativePath)
            throws Exception
    {

        final MutableObject<Exception> operationException = new MutableObject<>();

        getMetadataInputStreamWithCallback(simpleProxyRepositoryArtifactResolver, metadataRelativePath, event, is ->
        {
            final Artifact localArtifact = ArtifactUtils.convertPathToArtifact(event.getPath());
            localArtifact.setFile(artifactAbsolutePath.toFile());

            try
            {
                final Metadata metadata = artifactMetadataService.getMetadata(is);
                artifactMetadataService.mergeMetadata(event.getStorageId(), event.getRepositoryId(), localArtifact,
                                                      metadata);
            }
            catch (final Exception e)
            {
                operationException.setValue(e);
            }
            finally
            {
                IOUtils.closeQuietly(is);
            }
        });

        if (operationException.getValue() != null)
        {
            throw operationException.getValue();
        }
    }

    private void getMetadataInputStreamWithCallback(final ProxyRepositoryArtifactResolver proxyRepositoryArtifactResolver,
                                                    final Path metadataPath,
                                                    final ArtifactEvent event,
                                                    final Consumer<InputStream> callback)
            throws Exception
    {

        final InputStream metadataIs = proxyRepositoryArtifactResolver.getInputStream(event.getStorageId(),
                                                                                      event.getRepositoryId(),
                                                                                      FilenameUtils.separatorsToUnix(
                                                                                              metadataPath.toString()));
        callback.accept(metadataIs);
    }
}
