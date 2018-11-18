package org.carlspring.strongbox.providers.io;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.artifact.MavenRepositoryArtifact;
import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.carlspring.strongbox.client.CloseableRestResponse;
import org.carlspring.strongbox.client.RemoteRepositoryRetryArtifactDownloadConfiguration;
import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.providers.repository.proxied.RestArtifactResolverFactory;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.metadata.MetadataMerger;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
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
import org.mockito.Mockito;
import org.springframework.cache.CacheManager;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * @author Przemyslaw Fusik
 */
abstract class BaseMavenMetadataExpirationTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    protected static final String REPOSITORY_LOCAL_SOURCE = "mvn-local-source-repo-snapshots";

    protected static final String REPOSITORY_HOSTED = "mvn-hosted-repo-snapshots";

    protected static final String REPOSITORY_PROXY = "mvn-proxy-repo-snapshots";

    protected String groupId = "pl.fuss.maven.metadata";

    protected String artifactId = "maven-metadata-exp";

    private MetadataMerger metadataMerger = new MetadataMerger();

    protected MutableObject<Metadata> versionLevelMetadata = new MutableObject<>();

    protected MutableObject<Metadata> artifactLevelMetadata = new MutableObject<>();

    protected MutableRepository localSourceRepository;

    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    @Inject
    protected RestArtifactResolverFactory artifactResolverFactory;

    @Inject
    protected ChecksumCacheManager checksumCacheManager;

    @Inject
    protected ArtifactManagementService artifactManagementService;

    @Inject
    protected CacheManager cacheManager;

    protected void mockHostedRepositoryMetadataUpdate(final MutableRepository localSourceRepository,
                                                      final String hostedRepositoryId,
                                                      final String localSourceRepositoryId,
                                                      final MutableObject<Metadata> versionLevelMetadata,
                                                      final MutableObject<Metadata> artifactLevelMetadata)
            throws Exception
    {
        mockLocalRepositoryTestMetadataUpdate(localSourceRepository, versionLevelMetadata, artifactLevelMetadata);

        storeTestDataInHostedRepository(hostedRepositoryId, localSourceRepositoryId, true, "maven-metadata.xml");
        storeTestDataInHostedRepository(hostedRepositoryId, localSourceRepositoryId, true, "maven-metadata.xml.sha1");
        storeTestDataInHostedRepository(hostedRepositoryId, localSourceRepositoryId, true, "maven-metadata.xml.md5");
        storeTestDataInHostedRepository(hostedRepositoryId, localSourceRepositoryId, false, "maven-metadata.xml");
        storeTestDataInHostedRepository(hostedRepositoryId, localSourceRepositoryId, false, "maven-metadata.xml.sha1");
        storeTestDataInHostedRepository(hostedRepositoryId, localSourceRepositoryId, false, "maven-metadata.xml.md5");
    }

    protected void mockLocalRepositoryTestMetadataUpdate(MutableRepository localSourceRepository,
                                                         MutableObject<Metadata> versionLevelMetadata,
                                                         MutableObject<Metadata> artifactLevelMetadata)
            throws Exception
    {
        Artifact snapshotArtifact = new MavenRepositoryArtifact(groupId, artifactId,
                                                                MavenArtifactUtils.getSnapshotBaseVersion("1.0"));
        MavenArtifactGenerator mavenArtifactGenerator = new MavenArtifactGenerator(localSourceRepository.getBasedir());

        versionLevelMetadata.setValue(
                metadataMerger.updateMetadataAtVersionLevel(snapshotArtifact, versionLevelMetadata.getValue()));
        final String versionLevelMetadataPath = ArtifactUtils.getVersionLevelMetadataPath(snapshotArtifact);
        mavenArtifactGenerator.createMetadata(versionLevelMetadata.getValue(), versionLevelMetadataPath);

        artifactLevelMetadata.setValue(
                metadataMerger.updateMetadataAtArtifactLevel(snapshotArtifact, artifactLevelMetadata.getValue()));
        final String artifactLevelMetadataPath = ArtifactUtils.getArtifactLevelMetadataPath(snapshotArtifact);
        mavenArtifactGenerator.createMetadata(artifactLevelMetadata.getValue(), artifactLevelMetadataPath);
    }

    protected void storeTestDataInHostedRepository(final String hostedRepositoryId,
                                                   final String localSourceRepositoryId,
                                                   final boolean versionLevel,
                                                   final String filename)
            throws Exception
    {
        final RepositoryPath hostedPath = resolvePath(hostedRepositoryId, versionLevel, filename);

        final RepositoryPath testDataSourcePath = resolvePath(localSourceRepositoryId, versionLevel, filename);

        try (InputStream is = Files.newInputStream(testDataSourcePath))
        {
            artifactManagementService.store(hostedPath, is);
        }
    }

    protected void mockResolvingProxiedRemoteArtifactsToHostedRepository()
    {
        final RemoteRepositoryRetryArtifactDownloadConfiguration configuration = configurationManager.getConfiguration()
                                                                                                     .getRemoteRepositoriesConfiguration()
                                                                                                     .getRemoteRepositoryRetryArtifactDownloadConfiguration();

        final RestArtifactResolver artifactResolver = Mockito.mock(RestArtifactResolver.class);

        mockResolvingProxiedRemoteArtifactToHostedRepository(artifactResolver, true, "maven-metadata.xml");
        mockResolvingProxiedRemoteArtifactToHostedRepository(artifactResolver, true, "maven-metadata.xml.sha1");
        mockResolvingProxiedRemoteArtifactToHostedRepository(artifactResolver, true, "maven-metadata.xml.md5");

        mockResolvingProxiedRemoteArtifactToHostedRepository(artifactResolver, false, "maven-metadata.xml");
        mockResolvingProxiedRemoteArtifactToHostedRepository(artifactResolver, false, "maven-metadata.xml.sha1");
        mockResolvingProxiedRemoteArtifactToHostedRepository(artifactResolver, false, "maven-metadata.xml.md5");

        Mockito.when(artifactResolver.getConfiguration()).thenReturn(configuration);
        Mockito.when(artifactResolver.isAlive()).thenReturn(true);

        Mockito.when(artifactResolverFactory.newInstance(any(RemoteRepository.class))).thenReturn(artifactResolver);
    }

    private void mockResolvingProxiedRemoteArtifactToHostedRepository(final RestArtifactResolver artifactResolver,
                                                                      final boolean versionLevel,
                                                                      final String filename)
    {
        final RepositoryPath hostedRepositoryPath = resolvePath(REPOSITORY_HOSTED, versionLevel, filename);
        final Response response = Mockito.mock(Response.class);
        Mockito.when(response.getEntity()).thenAnswer(
                invocation -> new Object());
        Mockito.when(response.readEntity(InputStream.class)).thenAnswer(
                invocation -> Files.newInputStream(hostedRepositoryPath));
        Mockito.when(response.getStatus()).thenReturn(200);

        final CloseableRestResponse restResponse = Mockito.mock(CloseableRestResponse.class);
        Mockito.when(restResponse.getResponse()).thenReturn(response);

        final RepositoryPath proxiedRepositoryPath = resolvePath(REPOSITORY_PROXY, versionLevel, filename);
        final String proxiedPathRelativized = FilenameUtils.separatorsToUnix(
                proxiedRepositoryPath.relativize().toString());

        Mockito.when(artifactResolver.get(eq(proxiedPathRelativized))).thenReturn(restResponse);
        Mockito.when(artifactResolver.get(eq(proxiedPathRelativized), any(Long.class))).thenReturn(restResponse);
        Mockito.when(artifactResolver.head(eq(proxiedPathRelativized))).thenReturn(restResponse);
    }


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
        LocalDateTime dateTime = LocalDateTime.now().minusHours(1);
        Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
        return FileTime.from(instant);
    }
}
