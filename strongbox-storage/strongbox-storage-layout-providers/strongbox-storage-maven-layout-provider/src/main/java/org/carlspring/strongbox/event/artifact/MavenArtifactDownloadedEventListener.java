package org.carlspring.strongbox.event.artifact;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.repository.proxied.LocalStorageProxyRepositoryArtifactResolver;
import org.carlspring.strongbox.providers.repository.proxied.ProxyRepositoryArtifactResolver;
import org.carlspring.strongbox.providers.repository.proxied.SimpleProxyRepositoryArtifactResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.index.locator.Locator;
import org.apache.maven.index.locator.MetadataLocator;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenArtifactDownloadedEventListener
        implements ArtifactEventListener
{

    private static final Logger logger = LoggerFactory.getLogger(MavenArtifactDownloadedEventListener.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private ArtifactResolutionService artifactResolutionService;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    @SimpleProxyRepositoryArtifactResolver.SimpleProxyRepositoryArtifactResolverQualifier
    private ProxyRepositoryArtifactResolver simpleProxyRepositoryArtifactResolver;

    @Inject
    @LocalStorageProxyRepositoryArtifactResolver.LocalStorageProxyRepositoryArtifactResolverQualifier
    private ProxyRepositoryArtifactResolver localStorageProxyRepositoryArtifactResolver;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    private Locator metadataLocator = new MetadataLocator();

    @Override
    public void handle(ArtifactEvent event)
    {
        if (event.getType() != ArtifactEventTypeEnum.EVENT_ARTIFACT_FILE_DOWNLOADED.getType())
        {
            return;
        }

        Repository repository = getRepository(event);

        if (!RepositoryLayoutEnum.MAVEN_2.getLayout().equals(repository.getLayout()))
        {
            return;
        }

        resolveArtifactMetadataFile(event);
    }


    private void resolveArtifactMetadataFile(ArtifactEvent event)
    {
        try
        {
            final File metadataFile = metadataLocator.locate(new File(event.getPath()));

            try
            {
                artifactMetadataService.getMetadata(event.getStorageId(), event.getRepositoryId(),
                                                    metadataFile.getParent());
            }
            catch (NoSuchFileException ex)
            {
                downloadArtifactMetadataFromRemote(event, metadataFile);
                return;
            }

            downloadArtifactMetadataFromRemoteAndMergeWithLocal(event, metadataFile);
        }
        catch (Exception e)
        {
            logger.error("Unable to resolve artifact metadata of file " + event.getPath() + " of repository " +
                         event.getRepositoryId(), e);
        }
    }

    private void downloadArtifactMetadataFromRemote(ArtifactEvent event,
                                                    File metadataFile)
            throws IOException, ArtifactTransportException, NoSuchAlgorithmException, ProviderImplementationException
    {
        final InputStream metadataIs = localStorageProxyRepositoryArtifactResolver.getInputStream(event.getStorageId(),
                                                                                                  event.getRepositoryId(),
                                                                                                  FilenameUtils.separatorsToUnix(
                                                                                                          metadataFile.getPath()));
        IOUtils.closeQuietly(metadataIs);
    }

    private void downloadArtifactMetadataFromRemoteAndMergeWithLocal(ArtifactEvent event,
                                                                     File metadataFile)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException, ProviderImplementationException, ArtifactTransportException
    {
        final Repository repository = getRepository(event);
        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        final Artifact localArtifact = ArtifactUtils.convertPathToArtifact(event.getPath());
        localArtifact.setFile(layoutProvider.resolve(repository).resolve(event.getPath()).toFile());

        try (final InputStream remoteMetadataIs = simpleProxyRepositoryArtifactResolver.getInputStream(
                event.getStorageId(), event.getRepositoryId(), FilenameUtils.separatorsToUnix(metadataFile.getPath())))
        {
            final Metadata metadata = artifactMetadataService.getMetadata(remoteMetadataIs);
            artifactMetadataService.mergeMetadata(event.getStorageId(), event.getRepositoryId(), localArtifact,
                                                  metadata);
        }
    }

    private Repository getRepository(ArtifactEvent event)
    {
        return configurationManager.getConfiguration().getStorage(event.getStorageId()).getRepository(
                event.getRepositoryId());
    }
}
