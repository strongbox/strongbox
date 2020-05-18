package org.carlspring.strongbox.providers.io;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.strongbox.artifact.MavenRepositoryArtifact;
import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.carlspring.strongbox.client.CloseableRestResponse;
import org.carlspring.strongbox.client.RemoteRepositoryRetryArtifactDownloadConfiguration;
import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.providers.layout.LayoutFileSystemProvider;
import org.carlspring.strongbox.providers.repository.proxied.RestArtifactResolverFactory;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.metadata.MetadataMerger;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils;
import org.carlspring.strongbox.util.LocalDateTimeInstance;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.index.artifact.Gav;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.mockito.ArgumentMatchers.*;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
abstract class BaseMavenMetadataExpirationTest
{

    protected static final String STORAGE0 = "storage0";

    protected final Logger logger = LoggerFactory.getLogger(BaseMavenMetadataExpirationTest.class);

    protected String groupId = "pl.fuss.maven.metadata";

    protected String artifactId = "maven-metadata-exp";

    protected MutableObject<Metadata> versionLevelMetadata = new MutableObject<>();

    protected MutableObject<Metadata> artifactLevelMetadata = new MutableObject<>();

    private MetadataMerger metadataMerger = new MetadataMerger();

    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    @Inject
    protected RestArtifactResolverFactory artifactResolverFactory;

    @Inject
    protected ArtifactManagementService artifactManagementService;

    @Inject
    protected ConfigurationManagementService configurationManagementService;

    protected void mockHostedRepositoryMetadataUpdate(final String hostedRepositoryId,
                                                      final String localSourceRepositoryId,
                                                      final MutableObject<Metadata> versionLevelMetadata,
                                                      final MutableObject<Metadata> artifactLevelMetadata)
            throws Exception
    {
        mockLocalRepositoryTestMetadataUpdate(localSourceRepositoryId,
                                              versionLevelMetadata,
                                              artifactLevelMetadata);

        storeTestDataInHostedRepository(hostedRepositoryId,
                                        localSourceRepositoryId,
                                        true,
                                        "maven-metadata.xml");

        storeTestDataInHostedRepository(hostedRepositoryId,
                                        localSourceRepositoryId,
                                        true,
                                        "maven-metadata.xml.sha1");

        storeTestDataInHostedRepository(hostedRepositoryId,
                                        localSourceRepositoryId,
                                        true,
                                        "maven-metadata.xml.md5");

        storeTestDataInHostedRepository(hostedRepositoryId,
                                        localSourceRepositoryId,
                                        false,
                                        "maven-metadata.xml");

        storeTestDataInHostedRepository(hostedRepositoryId,
                                        localSourceRepositoryId,
                                        false,
                                        "maven-metadata.xml.sha1");

        storeTestDataInHostedRepository(hostedRepositoryId,
                                        localSourceRepositoryId,
                                        false,
                                        "maven-metadata.xml.md5");
    }

    protected void mockLocalRepositoryTestMetadataUpdate(String localRepositoryId,
                                                         MutableObject<Metadata> versionLevelMetadata,
                                                         MutableObject<Metadata> artifactLevelMetadata)
            throws Exception
    {
        final Repository repository = getConfiguration().getStorage(STORAGE0).getRepository(localRepositoryId);
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

        Artifact snapshotArtifact = new MavenRepositoryArtifact(new Gav(groupId, artifactId,
                                                                        "1.0-20131004.115330-1"));
        MavenArtifactGenerator mavenArtifactGenerator = new MavenArtifactGenerator(repositoryPath);

        versionLevelMetadata.setValue(
                metadataMerger.updateMetadataAtVersionLevel(snapshotArtifact, versionLevelMetadata.getValue()));
        final String versionLevelMetadataPath = MavenArtifactTestUtils.getVersionLevelMetadataPath(snapshotArtifact);
        mavenArtifactGenerator.createMetadata(versionLevelMetadata.getValue(), versionLevelMetadataPath);

        artifactLevelMetadata.setValue(
                metadataMerger.updateMetadataAtArtifactLevel(snapshotArtifact, artifactLevelMetadata.getValue()));
        final String artifactLevelMetadataPath = MavenArtifactTestUtils.getArtifactLevelMetadataPath(snapshotArtifact);
        mavenArtifactGenerator.createMetadata(artifactLevelMetadata.getValue(), artifactLevelMetadataPath);
    }

    protected void storeTestDataInHostedRepository(final String hostedRepositoryId,
                                                   final String localSourceRepositoryId,
                                                   final boolean versionLevel,
                                                   final String filename)
            throws Exception
    {
        final RepositoryPath hostedPath = resolvePath(hostedRepositoryId,
                                                      versionLevel,
                                                      filename);

        final RepositoryPath testDataSourcePath = resolvePath(localSourceRepositoryId,
                                                              versionLevel,
                                                              filename);

        try (InputStream is = Files.newInputStream(testDataSourcePath))
        {
            artifactManagementService.store(hostedPath, is);
        }
    }

    protected void mockResolvingProxiedRemoteArtifactsToHostedRepository(final String hostedRepositoryId)
            throws IOException
    {
        final RemoteRepositoryRetryArtifactDownloadConfiguration configuration = getConfiguration()
                                                                                         .getRemoteRepositoriesConfiguration()
                                                                                         .getRemoteRepositoryRetryArtifactDownloadConfiguration();

        final RestArtifactResolver artifactResolver = Mockito.mock(RestArtifactResolver.class);

        mockResolvingProxiedRemoteArtifactToHostedRepository(artifactResolver,
                                                             true,
                                                             "maven-metadata.xml");

        mockResolvingProxiedRemoteArtifactToHostedRepository(artifactResolver,
                                                             true,
                                                             "maven-metadata.xml.sha1");

        mockResolvingProxiedRemoteArtifactToHostedRepository(artifactResolver,
                                                             true,
                                                             "maven-metadata.xml.md5");

        mockResolvingProxiedRemoteArtifactToHostedRepository(artifactResolver,
                                                             false,
                                                             "maven-metadata.xml");

        mockResolvingProxiedRemoteArtifactToHostedRepository(artifactResolver,
                                                             false,
                                                             "maven-metadata.xml.sha1");

        mockResolvingProxiedRemoteArtifactToHostedRepository(artifactResolver,
                                                             false,
                                                             "maven-metadata.xml.md5");

        Mockito.when(artifactResolver.getConfiguration()).thenReturn(configuration);
        Mockito.when(artifactResolver.isAlive()).thenReturn(true);

        Mockito.when(artifactResolverFactory.newInstance(argThat(
                remoteRepository ->
                        remoteRepository != null &&
                        remoteRepository.getUrl() != null &&
                        remoteRepository.getUrl().contains(hostedRepositoryId)))).thenReturn(artifactResolver);
    }

    private void mockResolvingProxiedRemoteArtifactToHostedRepository(final RestArtifactResolver artifactResolver,
                                                                      final boolean versionLevel,
                                                                      final String filename)
            throws IOException
    {
        final RepositoryPath hostedRepositoryPath = resolvePath(getRepositoryHostedId(),
                                                                versionLevel,
                                                                filename);
        final Response response = Mockito.mock(Response.class);
        Mockito.when(response.getEntity()).thenAnswer(
                invocation -> new Object());
        Mockito.when(response.readEntity(InputStream.class)).thenAnswer(
                invocation -> Files.newInputStream(hostedRepositoryPath));
        Mockito.when(response.getStatus()).thenReturn(200);

        final CloseableRestResponse restResponse = Mockito.mock(CloseableRestResponse.class);
        Mockito.when(restResponse.getResponse()).thenReturn(response);

        final RepositoryPath proxiedRepositoryPath = resolvePath(getRepositoryProxyId(),
                                                                 versionLevel,
                                                                 filename);

        final String proxiedPathRelativized = FilenameUtils.separatorsToUnix(
                RepositoryFiles.relativizePath(proxiedRepositoryPath));

        logger.debug("Mocking proxiedPathRelativized {}. Client = {}. Rest response = {}", proxiedPathRelativized,
                    artifactResolver, restResponse);
        Mockito.when(artifactResolver.get(eq(proxiedPathRelativized))).thenReturn(restResponse);
        Mockito.when(artifactResolver.get(eq(proxiedPathRelativized), any(Long.class))).thenReturn(restResponse);
        Mockito.when(artifactResolver.head(eq(proxiedPathRelativized))).thenReturn(restResponse);
    }

    protected abstract String getRepositoryHostedId();

    protected abstract String getRepositoryProxyId();


    protected RepositoryPath resolvePath(final String repositoryId,
                                         final boolean versionLevel,
                                         final String filename)
    {
        final Repository repository = getConfiguration().getStorage(STORAGE0).getRepository(repositoryId);
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);
        repositoryPath = repositoryPath.resolve(groupId.replaceAll("\\.", "/"));
        repositoryPath = repositoryPath.resolve(artifactId);
        if (versionLevel)
        {
            repositoryPath = repositoryPath.resolve("1.0-SNAPSHOT");
        }
        return repositoryPath.resolve(filename);
    }

    protected FileTime oneHourAgo()
    {
        LocalDateTime dateTime = LocalDateTimeInstance.now().minusHours(1).withNano(0);
        Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();

        return FileTime.from(instant);
    }

    protected RepositoryPath resolveSiblingChecksum(final RepositoryPath repositoryPath,
                                                    final EncryptionAlgorithmsEnum checksumAlgorithm)
    {
        LayoutFileSystemProvider provider = repositoryPath.getFileSystem().provider();
        return provider.getChecksumPath(repositoryPath, checksumAlgorithm.getAlgorithm());
    }

    protected String readChecksum(final RepositoryPath checksumRepositoryPath)
            throws IOException
    {
        if (Files.notExists(checksumRepositoryPath))
        {
            return null;
        }

        return Files.readAllLines(checksumRepositoryPath).stream().findFirst().orElse(null);
    }

    protected FileTime readLastModifiedTime(final RepositoryPath repositoryPath)
            throws IOException
    {
        return Files.getLastModifiedTime(repositoryPath);
    }

    protected Configuration getConfiguration()
    {
        return configurationManagementService.getConfiguration();
    }
}
