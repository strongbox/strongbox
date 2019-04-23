package org.carlspring.strongbox.providers.io;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.repository.GroupRepositoryProvider;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.MavenRepositorySetup;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;

import javax.inject.Inject;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class MavenMetadataExpirationSingleGroupCaseTest
        extends BaseMavenMetadataExpirationTest
{

    private static final String REPOSITORY_GROUP = "mvn-group-repo-snapshots";

    @Inject
    private GroupRepositoryProvider groupRepositoryProvider;

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void groupRepositoryVersionLevelMetadataShouldBeRefreshedAsItsSingleProxySubrepository(@TestRepository(storage = STORAGE0,
                                                                                                                  repository = REPOSITORY_LOCAL_SOURCE,
                                                                                                                  layout = Maven2LayoutProvider.ALIAS,
                                                                                                                  policy = RepositoryPolicyEnum.SNAPSHOT)
                                                                                                  Repository localRepository,
                                                                                                  @TestRepository(storage = STORAGE0,
                                                                                                                  repository = REPOSITORY_HOSTED + "-groupRepositoryVersionLevelMetadataShouldBeRefreshedAsItsSingleProxySubrepository",
                                                                                                                  layout = Maven2LayoutProvider.ALIAS,
                                                                                                                  policy = RepositoryPolicyEnum.SNAPSHOT)
                                                                                                  Repository hostedRepository,
                                                                                                  @TestRepository.Remote(url = "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_HOSTED + "/")
                                                                                                  @TestRepository(storage = STORAGE0,
                                                                                                                  repository = REPOSITORY_PROXY + "-groupRepositoryVersionLevelMetadataShouldBeRefreshedAsItsSingleProxySubrepository",
                                                                                                                  layout = Maven2LayoutProvider.ALIAS,
                                                                                                                  setup = MavenRepositorySetup.MavenRepositorySetupWithProxyType.class)
                                                                                                  Repository proxyRepository,
                                                                                                  @TestRepository.Group({ REPOSITORY_PROXY + "-groupRepositoryVersionLevelMetadataShouldBeRefreshedAsItsSingleProxySubrepository" })
                                                                                                  @TestRepository(storage = STORAGE0,
                                                                                                                  repository = REPOSITORY_GROUP,
                                                                                                                  layout = Maven2LayoutProvider.ALIAS,
                                                                                                                  policy = RepositoryPolicyEnum.SNAPSHOT,
                                                                                                                  setup = MavenRepositorySetup.MavenRepositorySetupWithGroupType.class)
                                                                                                  Repository groupRepository,
                                                                                                  TestInfo testInfo)
            throws Exception
    {
        mockHostedRepositoryMetadataUpdate(hostedRepository.getId(),
                                           localRepository.getId(),
                                           versionLevelMetadata,
                                           artifactLevelMetadata);

        mockResolvingProxiedRemoteArtifactsToHostedRepository(testInfo);

        final RepositoryPath hostedPath = resolvePath(hostedRepository.getId(),
                                                      true,
                                                      "maven-metadata.xml");
        String sha1HostedPathChecksum = readChecksum(resolveSiblingChecksum(hostedPath, EncryptionAlgorithmsEnum.SHA1));
        assertNotNull(sha1HostedPathChecksum);

        final RepositoryPath proxyPath = resolvePath(proxyRepository.getId(),
                                                     true,
                                                     "maven-metadata.xml");
        final RepositoryPath groupPath = resolvePath(groupRepository.getId(),
                                                     true,
                                                     "maven-metadata.xml");
        String sha1ProxyPathChecksum = readChecksum(resolveSiblingChecksum(proxyPath, EncryptionAlgorithmsEnum.SHA1));
        assertNull(sha1ProxyPathChecksum);

        assertFalse(RepositoryFiles.artifactExists(groupPath));

        RepositoryPath resolvedGroupPath = groupRepositoryProvider.fetchPath(groupPath);

        sha1ProxyPathChecksum = readChecksum(resolveSiblingChecksum(proxyPath, EncryptionAlgorithmsEnum.SHA1));
        assertNotNull(sha1ProxyPathChecksum);
        assertEquals(sha1ProxyPathChecksum, sha1HostedPathChecksum);

        String calculatedGroupPathChecksum = calculateChecksum(resolvedGroupPath,
                                                               EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertEquals(sha1ProxyPathChecksum, calculatedGroupPathChecksum);

        Files.setLastModifiedTime(proxyPath, oneHourAgo());

        sha1ProxyPathChecksum = readChecksum(resolveSiblingChecksum(proxyPath, EncryptionAlgorithmsEnum.SHA1));
        assertEquals(sha1ProxyPathChecksum, calculatedGroupPathChecksum);

        mockHostedRepositoryMetadataUpdate(hostedRepository.getId(),
                                           localRepository.getId(),
                                           versionLevelMetadata,
                                           artifactLevelMetadata);

        sha1HostedPathChecksum = readChecksum(resolveSiblingChecksum(hostedPath,
                                                                     EncryptionAlgorithmsEnum.SHA1));
        final String calculatedHostedPathChecksum = calculateChecksum(hostedPath,
                                                                      EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertEquals(sha1HostedPathChecksum, calculatedHostedPathChecksum);
        assertNotEquals(calculatedHostedPathChecksum, sha1ProxyPathChecksum);

        Files.setLastModifiedTime(proxyPath, oneHourAgo());

        resolvedGroupPath = groupRepositoryProvider.fetchPath(groupPath);
        sha1ProxyPathChecksum = readChecksum(resolveSiblingChecksum(proxyPath,
                                                                    EncryptionAlgorithmsEnum.SHA1));
        assertEquals(sha1ProxyPathChecksum, calculatedHostedPathChecksum);
        calculatedGroupPathChecksum = calculateChecksum(resolvedGroupPath,
                                                        EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertEquals(sha1ProxyPathChecksum, calculatedGroupPathChecksum);
    }
}
