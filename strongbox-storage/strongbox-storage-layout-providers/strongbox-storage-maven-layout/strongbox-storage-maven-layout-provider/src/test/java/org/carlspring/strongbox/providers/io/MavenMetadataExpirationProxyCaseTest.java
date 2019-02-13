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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.carlspring.strongbox.util.MessageDigestUtils.calculateChecksum;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles({ "MockedRestArtifactResolverTestConfig",
                  "test" })
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class MavenMetadataExpirationProxyCaseTest
        extends BaseMavenMetadataExpirationTest
{

    @Inject
    private ProxyRepositoryProvider proxyRepositoryProvider;

    @BeforeEach
    public void initialize(TestInfo testInfo)
            throws Exception
    {
        localSourceRepository = createRepository(STORAGE0,
                                                 getRepositoryName(REPOSITORY_LOCAL_SOURCE, testInfo),
                                                 RepositoryPolicyEnum.SNAPSHOT.getPolicy(),
                                                 false);

        createRepository(STORAGE0,
                         getRepositoryName(REPOSITORY_HOSTED, testInfo),
                         RepositoryPolicyEnum.SNAPSHOT.getPolicy(),
                         false);

        mockHostedRepositoryMetadataUpdate(localSourceRepository,
                                           getRepositoryName(REPOSITORY_HOSTED, testInfo),
                                           getRepositoryName(REPOSITORY_LOCAL_SOURCE, testInfo),
                                           versionLevelMetadata,
                                           artifactLevelMetadata);

        createProxyRepository(STORAGE0,
                              getRepositoryName(REPOSITORY_PROXY, testInfo),
                              "http://localhost:48080/storages/" + STORAGE0 + "/" +
                              getRepositoryName(REPOSITORY_HOSTED, testInfo) + "/");

        mockResolvingProxiedRemoteArtifactsToHostedRepository(testInfo);
    }

    @Test
    public void expiredProxyRepositoryMetadataPathShouldBeRefetched(TestInfo testInfo)
            throws Exception
    {
        final RepositoryPath hostedPath = resolvePath(getRepositoryName(REPOSITORY_HOSTED, testInfo),
                                                      true,
                                                      "maven-metadata.xml");
        String sha1HostedPathChecksum = readChecksum(resolveSiblingChecksum(hostedPath, EncryptionAlgorithmsEnum.SHA1));
        assertNotNull(sha1HostedPathChecksum);

        final RepositoryPath proxiedPath = resolvePath(getRepositoryName(REPOSITORY_PROXY, testInfo),
                                                       true,
                                                       "maven-metadata.xml");
        String sha1ProxiedPathChecksum = readChecksum(resolveSiblingChecksum(proxiedPath,
                                                                             EncryptionAlgorithmsEnum.SHA1));
        assertNull(sha1ProxiedPathChecksum);

        assertFalse(RepositoryFiles.artifactExists(proxiedPath));

        proxyRepositoryProvider.fetchPath(proxiedPath);
        assertTrue(RepositoryFiles.artifactExists(proxiedPath));

        sha1ProxiedPathChecksum = readChecksum(resolveSiblingChecksum(proxiedPath,
                                                                      EncryptionAlgorithmsEnum.SHA1));
        assertNotNull(sha1ProxiedPathChecksum);
        assertEquals(sha1ProxiedPathChecksum, sha1HostedPathChecksum);

        String calculatedProxiedPathChecksum = calculateChecksum(proxiedPath,
                                                                 EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertEquals(sha1ProxiedPathChecksum, calculatedProxiedPathChecksum);

        Files.setLastModifiedTime(proxiedPath, oneHourAgo());

        proxyRepositoryProvider.fetchPath(proxiedPath);
        sha1ProxiedPathChecksum = readChecksum(resolveSiblingChecksum(proxiedPath,
                                                                      EncryptionAlgorithmsEnum.SHA1));
        assertEquals(sha1ProxiedPathChecksum, calculatedProxiedPathChecksum);

        mockHostedRepositoryMetadataUpdate(localSourceRepository,
                                           getRepositoryName(REPOSITORY_HOSTED, testInfo),
                                           getRepositoryName(REPOSITORY_LOCAL_SOURCE, testInfo),
                                           versionLevelMetadata,
                                           artifactLevelMetadata);

        sha1HostedPathChecksum = readChecksum(resolveSiblingChecksum(hostedPath,
                                                                     EncryptionAlgorithmsEnum.SHA1));
        final String calculatedHostedPathChecksum = calculateChecksum(hostedPath,
                                                                      EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertEquals(sha1HostedPathChecksum, calculatedHostedPathChecksum);
        assertNotEquals(calculatedHostedPathChecksum, sha1ProxiedPathChecksum);

        Files.setLastModifiedTime(proxiedPath, oneHourAgo());

        proxyRepositoryProvider.fetchPath(proxiedPath);
        sha1ProxiedPathChecksum = readChecksum(resolveSiblingChecksum(proxiedPath,
                                                                      EncryptionAlgorithmsEnum.SHA1));
        assertEquals(sha1ProxiedPathChecksum, calculatedHostedPathChecksum);
        calculatedProxiedPathChecksum = calculateChecksum(proxiedPath,
                                                          EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertEquals(sha1ProxiedPathChecksum, calculatedProxiedPathChecksum);
    }

    @AfterEach
    public void removeRepositories(TestInfo testInfo)
            throws Exception
    {
        removeRepositories(getRepositories(testInfo));
    }

    private Set<MutableRepository> getRepositories(TestInfo testInfo)
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName(REPOSITORY_HOSTED, testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName(REPOSITORY_PROXY, testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName(REPOSITORY_LOCAL_SOURCE, testInfo),
                                              Maven2LayoutProvider.ALIAS));
        return repositories;
    }


}
