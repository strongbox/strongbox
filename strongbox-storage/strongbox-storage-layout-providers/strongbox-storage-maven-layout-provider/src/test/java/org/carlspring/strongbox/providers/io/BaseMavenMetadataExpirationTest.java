package org.carlspring.strongbox.providers.io;

import org.carlspring.maven.commons.util.ArtifactUtils;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.mockito.Mockito;
import org.springframework.cache.CacheManager;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * @author Przemyslaw Fusik
 */
public class BaseMavenMetadataExpirationTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    protected static final String REPOSITORY_LOCAL_SOURCE = "mvn-local-source-repo-snapshots";

    protected static final String REPOSITORY_HOSTED = "mvn-hosted-repo-snapshots";

    protected static final String REPOSITORY_PROXY = "mvn-proxy-repo-snapshots";

    protected String groupId = "pl.fuss.maven.metadata";

    protected String artifactId = "maven-metadata-exp";

    protected MetadataMerger metadataMerger;

    protected MavenArtifactGenerator mavenArtifactGenerator;

    protected Metadata versionLevelMetadata;

    protected Metadata artifactLevelMetadata;

    protected Artifact snapshotArtifact;

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

    protected void mockHostedRepositoryMetadataUpdate()
            throws Exception
    {
        mockLocalRepositoryTestMetadataUpdate();

        storeTestDataInHostedRepository(true, "maven-metadata.xml");
        storeTestDataInHostedRepository(true, "maven-metadata.xml.sha1");
        storeTestDataInHostedRepository(true, "maven-metadata.xml.md5");
        storeTestDataInHostedRepository(false, "maven-metadata.xml");
        storeTestDataInHostedRepository(false, "maven-metadata.xml.sha1");
        storeTestDataInHostedRepository(false, "maven-metadata.xml.md5");
    }

    protected void mockLocalRepositoryTestMetadataUpdate()
            throws Exception
    {
        snapshotArtifact = snapshotArtifact != null ? snapshotArtifact :
                           createTimestampedSnapshotArtifact(localSourceRepository.getBasedir(),
                                                             groupId,
                                                             artifactId,
                                                             "1.0",
                                                             1);

        metadataMerger = new MetadataMerger();
        mavenArtifactGenerator = new MavenArtifactGenerator(localSourceRepository.getBasedir());

        versionLevelMetadata = metadataMerger.updateMetadataAtVersionLevel(snapshotArtifact, versionLevelMetadata);
        final String versionLevelMetadataPath = ArtifactUtils.getVersionLevelMetadataPath(snapshotArtifact);
        mavenArtifactGenerator.createMetadata(versionLevelMetadata, versionLevelMetadataPath);

        artifactLevelMetadata = metadataMerger.updateMetadataAtArtifactLevel(snapshotArtifact, artifactLevelMetadata);
        final String artifactLevelMetadataPath = ArtifactUtils.getArtifactLevelMetadataPath(snapshotArtifact);
        mavenArtifactGenerator.createMetadata(artifactLevelMetadata, artifactLevelMetadataPath);
    }

    protected void storeTestDataInHostedRepository(final boolean versionLevel,
                                                   final String filename)
            throws Exception
    {
        final RepositoryPath hostedPath = resolvePath(REPOSITORY_HOSTED, versionLevel, filename);

        final RepositoryPath testDataSourcePath = resolvePath(REPOSITORY_LOCAL_SOURCE, versionLevel, filename);

        try (InputStream is = Files.newInputStream(testDataSourcePath))
        {
            artifactManagementService.store(hostedPath, is);
        }
    }

    protected void mockResolvingProxiedRemoteArtifactToHostedRepository(final RestArtifactResolver artifactResolver,
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

    protected void mockResolvingProxiedRemoteArtifactsToHostedRepository()
            throws IOException
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
