package org.carlspring.strongbox.providers.io;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.repository.ProxyRepositoryProvider;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.carlspring.strongbox.util.MessageDigestUtils.calculateChecksum;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles({ "MockedRestArtifactResolverTestConfig",
                  "test" })
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class MavenMetadataExpirationProxyCaseTest
        extends BaseMavenMetadataExpirationTest
{

    @Inject
    private ProxyRepositoryProvider proxyRepositoryProvider;

    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean(REPOSITORY_HOSTED,
                                       REPOSITORY_PROXY,
                                       REPOSITORY_LOCAL_SOURCE));
    }

    private static Set<MutableRepository> getRepositoriesToClean(String... repositoryId)
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();

        Arrays.asList(repositoryId).forEach(
                r -> repositories.add(createRepositoryMock(STORAGE0, r, Maven2LayoutProvider.ALIAS))
        );
        return repositories;
    }

    @BeforeEach
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
        assertEquals(sha1ProxiedPathChecksum, sha1HostedPathChecksum);

        String calculatedProxiedPathChecksum = calculateChecksum(proxiedPath,
                                                                 EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertEquals(sha1ProxiedPathChecksum, calculatedProxiedPathChecksum);

        Files.setLastModifiedTime(proxiedPath, oneHourAgo());

        proxyRepositoryProvider.fetchPath(proxiedPath);
        sha1ProxiedPathChecksum = readChecksum(resolveSiblingChecksum(proxiedPath, EncryptionAlgorithmsEnum.SHA1));
        assertEquals(sha1ProxiedPathChecksum, calculatedProxiedPathChecksum);

        mockHostedRepositoryMetadataUpdate(localSourceRepository,
                                           REPOSITORY_HOSTED,
                                           REPOSITORY_LOCAL_SOURCE,
                                           versionLevelMetadata,
                                           artifactLevelMetadata);

        sha1HostedPathChecksum = readChecksum(resolveSiblingChecksum(hostedPath, EncryptionAlgorithmsEnum.SHA1));
        final String calculatedHostedPathChecksum = calculateChecksum(hostedPath,
                                                                      EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertEquals(sha1HostedPathChecksum, calculatedHostedPathChecksum);
        assertNotEquals(calculatedHostedPathChecksum, sha1ProxiedPathChecksum);

        Files.setLastModifiedTime(proxiedPath, oneHourAgo());

        proxyRepositoryProvider.fetchPath(proxiedPath);
        sha1ProxiedPathChecksum = readChecksum(resolveSiblingChecksum(proxiedPath, EncryptionAlgorithmsEnum.SHA1));
        assertEquals(sha1ProxiedPathChecksum, calculatedHostedPathChecksum);
        calculatedProxiedPathChecksum = calculateChecksum(proxiedPath,
                                                          EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertEquals(sha1ProxiedPathChecksum, calculatedProxiedPathChecksum);
    }

    @AfterEach
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
