package org.carlspring.strongbox.providers.io;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.repository.ProxyRepositoryProvider;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.carlspring.strongbox.util.MessageDigestUtils.calculateChecksum;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({ "MockedRestArtifactResolverTestConfig",
                  "test" })
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class MavenMetadataExpirationProxyCaseTest
        extends BaseMavenMetadataExpirationTest
{

    @Inject
    private ProxyRepositoryProvider proxyRepositoryProvider;

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
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LOCAL_SOURCE, Maven2LayoutProvider.ALIAS));

        return repositories;
    }


    @Before
    public void initialize()
            throws Exception
    {
        localSourceRepository = createRepository(STORAGE0,
                                                 REPOSITORY_LOCAL_SOURCE,
                                                 RepositoryPolicyEnum.SNAPSHOT.getPolicy(),
                                                 false);

        createRepository(STORAGE0,
                         REPOSITORY_HOSTED,
                         RepositoryPolicyEnum.SNAPSHOT.getPolicy(),
                         false);

        mockHostedRepositoryMetadataUpdate(localSourceRepository,
                                           REPOSITORY_HOSTED,
                                           REPOSITORY_LOCAL_SOURCE,
                                           versionLevelMetadata,
                                           artifactLevelMetadata);

        createProxyRepository(STORAGE0,
                              REPOSITORY_PROXY,
                              "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_HOSTED + "/");

        mockResolvingProxiedRemoteArtifactsToHostedRepository();
    }

    @Test
    public void expiredProxyRepositoryMetadataPathShouldBeRefetched()
            throws Exception
    {
        final RepositoryPath hostedPath = resolvePath(REPOSITORY_HOSTED, true, "maven-metadata.xml");
        String sha1HostedPathChecksum = readChecksum(resolveSiblingChecksum(hostedPath, EncryptionAlgorithmsEnum.SHA1));
        assertNotNull(sha1HostedPathChecksum);

        final RepositoryPath proxiedPath = resolvePath(REPOSITORY_PROXY, true, "maven-metadata.xml");
        String sha1ProxiedPathChecksum = readChecksum(
                resolveSiblingChecksum(proxiedPath, EncryptionAlgorithmsEnum.SHA1));
        assertNull(sha1ProxiedPathChecksum);

        assertFalse(RepositoryFiles.artifactExists(proxiedPath));

        proxyRepositoryProvider.fetchPath(proxiedPath);
        assertTrue(RepositoryFiles.artifactExists(proxiedPath));

        sha1ProxiedPathChecksum = readChecksum(resolveSiblingChecksum(proxiedPath, EncryptionAlgorithmsEnum.SHA1));
        assertNotNull(sha1ProxiedPathChecksum);
        assertThat(sha1ProxiedPathChecksum, equalTo(sha1HostedPathChecksum));

        String calculatedProxiedPathChecksum = calculateChecksum(proxiedPath,
                                                                 EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertThat(sha1ProxiedPathChecksum, equalTo(calculatedProxiedPathChecksum));

        Files.setLastModifiedTime(proxiedPath, oneHourAgo());

        proxyRepositoryProvider.fetchPath(proxiedPath);
        sha1ProxiedPathChecksum = readChecksum(resolveSiblingChecksum(proxiedPath, EncryptionAlgorithmsEnum.SHA1));
        assertThat(sha1ProxiedPathChecksum, equalTo(calculatedProxiedPathChecksum));

        mockHostedRepositoryMetadataUpdate(localSourceRepository,
                                           REPOSITORY_HOSTED,
                                           REPOSITORY_LOCAL_SOURCE,
                                           versionLevelMetadata,
                                           artifactLevelMetadata);

        sha1HostedPathChecksum = readChecksum(resolveSiblingChecksum(hostedPath, EncryptionAlgorithmsEnum.SHA1));
        final String calculatedHostedPathChecksum = calculateChecksum(hostedPath,
                                                                      EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertThat(sha1HostedPathChecksum, equalTo(calculatedHostedPathChecksum));
        assertThat(calculatedHostedPathChecksum, not(equalTo(sha1ProxiedPathChecksum)));

        Files.setLastModifiedTime(proxiedPath, oneHourAgo());

        proxyRepositoryProvider.fetchPath(proxiedPath);
        sha1ProxiedPathChecksum = readChecksum(resolveSiblingChecksum(proxiedPath, EncryptionAlgorithmsEnum.SHA1));
        assertThat(sha1ProxiedPathChecksum, equalTo(calculatedHostedPathChecksum));
        calculatedProxiedPathChecksum = calculateChecksum(proxiedPath,
                                                          EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertThat(sha1ProxiedPathChecksum, equalTo(calculatedProxiedPathChecksum));
    }

    @After
    public void removeRepositories()
            throws Exception
    {
        closeIndexersForRepository(STORAGE0, REPOSITORY_HOSTED);
        closeIndexersForRepository(STORAGE0, REPOSITORY_LOCAL_SOURCE);
        closeIndexersForRepository(STORAGE0, REPOSITORY_PROXY);
        removeRepositories(getRepositoriesToClean());
        cleanUp();
    }


}
