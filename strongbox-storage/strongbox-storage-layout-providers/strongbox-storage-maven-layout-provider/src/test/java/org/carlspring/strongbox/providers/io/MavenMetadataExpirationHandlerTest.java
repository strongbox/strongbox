package org.carlspring.strongbox.providers.io;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.carlspring.strongbox.client.CloseableRestResponse;
import org.carlspring.strongbox.client.RemoteRepositoryRetryArtifactDownloadConfiguration;
import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.repository.ProxyRepositoryProvider;
import org.carlspring.strongbox.providers.repository.proxied.RestArtifactResolverFactory;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.checksum.ChecksumCacheManager;
import org.carlspring.strongbox.storage.metadata.MetadataMerger;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({ "MockedRestArtifactResolverTestConfig",
                  "test" })
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class MavenMetadataExpirationHandlerTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_HOSTED = "mvn-local-repo-snapshots";

    private static final String REPOSITORY_PROXY = "mvn-proxy-repo-snapshots";

    private String groupId = "pl.fuss.maven.metadata";

    private String artifactId = "maven-metadata-exp";

    private MetadataMerger metadataMerger;

    private MavenArtifactGenerator mavenArtifactGenerator;

    @Inject
    private ProxyRepositoryProvider proxyRepositoryProvider;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Inject
    private RestArtifactResolverFactory artifactResolverFactory;

    @Inject
    private ChecksumCacheManager checksumCacheManager;

    @Inject
    private ArtifactManagementService artifactManagementService;

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        final Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_HOSTED, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_PROXY, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Before
    public void initialize()
            throws Exception
    {
        final MutableRepository hostedRepository = createRepository(STORAGE0, REPOSITORY_HOSTED,
                                                                    RepositoryPolicyEnum.SNAPSHOT.getPolicy(), false);

        final Artifact snapshotArtifact = createTimestampedSnapshotArtifact(hostedRepository.getBasedir(), groupId,
                                                                            artifactId, "1.0", 1);

        metadataMerger = new MetadataMerger();
        mavenArtifactGenerator = new MavenArtifactGenerator(hostedRepository.getBasedir());

        final Metadata versionLevelMetadata = metadataMerger.updateMetadataAtVersionLevel(snapshotArtifact, null);
        final String versionLevelMetadataPath = ArtifactUtils.getVersionLevelMetadataPath(snapshotArtifact);
        mavenArtifactGenerator.createMetadata(versionLevelMetadata, versionLevelMetadataPath);

        final RepositoryPath versionLevelMetadataRepositoryPath = resolvePath(REPOSITORY_HOSTED, true,
                                                                              "maven-metadata.xml");
        /* TODO FIXME fulfill the checksums
        try (InputStream is = hostedRepositoryProvider.getInputStream(versionLevelMetadataRepositoryPath))
        {
            artifactManagementService.store(versionLevelMetadataRepositoryPath, is);
        }
        */

        final Metadata artifactLevelMetadata = metadataMerger.updateMetadataAtArtifactLevel(snapshotArtifact, null);
        final String artifactLevelMetadataPath = ArtifactUtils.getArtifactLevelMetadataPath(snapshotArtifact);
        mavenArtifactGenerator.createMetadata(artifactLevelMetadata, artifactLevelMetadataPath);

        final RepositoryPath artifactLevelMetadataRepositoryPath = resolvePath(REPOSITORY_HOSTED, false,
                                                                               "maven-metadata.xml");
        /* TODO FIXME fulfill the checksums
        try (InputStream is = hostedRepositoryProvider.getInputStream(artifactLevelMetadataRepositoryPath))
        {
            artifactManagementService.store(artifactLevelMetadataRepositoryPath, is);
        }
        */

        createProxyRepository(STORAGE0,
                              REPOSITORY_PROXY,
                              "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_HOSTED + "/");

        mockResolvingProxiedRemoteArtifactsToHostedRepository();
    }

    @Test
    public void expiredMetadataShouldFetchUpdatedVersionWithinProxiedRepository()
            throws IOException
    {
        final RepositoryPath hostedPath = resolvePath(REPOSITORY_HOSTED, true, "maven-metadata.xml");
        assertNotNull(checksumCacheManager.getArtifactChecksum(hostedPath, "sha1"));

        final RepositoryPath proxiedPath = resolvePath(REPOSITORY_PROXY, true, "maven-metadata.xml");
        assertNull(checksumCacheManager.getArtifactChecksum(proxiedPath, "sha1"));

        assertFalse(RepositoryFiles.artifactExists(proxiedPath));

        final RepositoryPath repositoryPath = proxyRepositoryProvider.fetchPath(proxiedPath);
        assertTrue(RepositoryFiles.artifactExists(proxiedPath));

        assertNotNull(checksumCacheManager.getArtifactChecksum(proxiedPath, "sha1"));


        repositoryPath.getRepository();
    }

    @After
    public void removeRepositories()
            throws Exception
    {
        closeIndexersForRepository(STORAGE0, REPOSITORY_HOSTED);
        closeIndexersForRepository(STORAGE0, REPOSITORY_PROXY);
        removeRepositories(getRepositoriesToClean());
        cleanUp();
    }

    private void mockResolvingProxiedRemoteArtifactToHostedRepository(final RestArtifactResolver artifactResolver,
                                                                      final boolean versionLevel,
                                                                      final String filename)
    {
        final RepositoryPath hostedRepositoryPath = resolvePath(REPOSITORY_HOSTED, versionLevel, filename);
        final Response response = Mockito.mock(Response.class);
        Mockito.when(response.getEntity()).thenAnswer(
                invocation -> hostedRepositoryProvider.getInputStream(hostedRepositoryPath));
        Mockito.when(response.readEntity(InputStream.class)).thenAnswer(
                invocation -> hostedRepositoryProvider.getInputStream(hostedRepositoryPath));
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

    private void mockResolvingProxiedRemoteArtifactsToHostedRepository()
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


    private RepositoryPath resolvePath(final String repositoryId,
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


}
