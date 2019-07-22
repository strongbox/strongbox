package org.carlspring.strongbox.providers.io;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.repository.GroupRepositoryProvider;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;

import javax.inject.Inject;
import java.nio.file.Files;

import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum.SNAPSHOT;
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
public class MavenMetadataExpirationMultipleGroupCaseTest
        extends BaseMavenMetadataExpirationTest
{

    private static final String REPOSITORY_GROUP = "mvn-group-repo-snapshots";

    private static final String REPOSITORY_HOSTED_YAHR = "mvn-hosted-yahr-repo-snapshots";

    private static final String REPOSITORY_LOCAL_YAHR_SOURCE = "mvn-local-yahr-source-repo-snapshots";

    @Inject
    private GroupRepositoryProvider groupRepositoryProvider;

    private MutableObject<Metadata> versionLevelMetadataYahr = new MutableObject<>();

    private MutableObject<Metadata> artifactLevelMetadataYahr = new MutableObject<>();

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void expiredGroupMetadataShouldFetchVersionLevelMetadataFromFirstMatch(
            @MavenRepository(repositoryId = REPOSITORY_HOSTED,
                             policy = SNAPSHOT)
            Repository hostedRepository,
            @MavenRepository(repositoryId = REPOSITORY_HOSTED_YAHR,
                             policy = SNAPSHOT)
            Repository hostedYahrRepository,
            @MavenRepository(repositoryId = REPOSITORY_LOCAL_SOURCE,
                             policy = SNAPSHOT)
            Repository localSourceRepository,
            @MavenRepository(repositoryId = REPOSITORY_LOCAL_YAHR_SOURCE,
                             policy = SNAPSHOT)
            Repository localYahrSourceRepository,
            @Remote(url = PROXY_REPOSITORY_URL)
            @MavenRepository(repositoryId = REPOSITORY_PROXY,
                             setup = MavenIndexedRepositorySetup.class)
            Repository proxyRepository,
            @Group(repositories = REPOSITORY_PROXY)
            @MavenRepository(repositoryId = REPOSITORY_GROUP)
            Repository groupRepository)
            throws Exception
    {
        mockHostedRepositoryMetadataUpdate(hostedRepository.getId(),
                                           localSourceRepository.getId(),
                                           versionLevelMetadata,
                                           artifactLevelMetadata);

        mockResolvingProxiedRemoteArtifactsToHostedRepository();

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
        assertTrue(RepositoryFiles.artifactExists(resolvedGroupPath));

        sha1ProxyPathChecksum = readChecksum(resolveSiblingChecksum(proxyPath, EncryptionAlgorithmsEnum.SHA1));
        assertNotNull(sha1ProxyPathChecksum);
        assertEquals(sha1ProxyPathChecksum, sha1HostedPathChecksum);

        String calculatedGroupPathChecksum = calculateChecksum(resolvedGroupPath,
                                                               EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertEquals(sha1ProxyPathChecksum, calculatedGroupPathChecksum);

        Files.setLastModifiedTime(proxyPath, oneHourAgo());

        groupRepositoryProvider.fetchPath(groupPath);
        sha1ProxyPathChecksum = readChecksum(resolveSiblingChecksum(proxyPath, EncryptionAlgorithmsEnum.SHA1));
        assertEquals(sha1ProxyPathChecksum, calculatedGroupPathChecksum);

        mockHostedRepositoryMetadataUpdate(hostedRepository.getId(),
                                           localSourceRepository.getId(),
                                           versionLevelMetadata,
                                           artifactLevelMetadata);

        sha1HostedPathChecksum = readChecksum(resolveSiblingChecksum(hostedPath, EncryptionAlgorithmsEnum.SHA1));
        final String calculatedHostedPathChecksum = calculateChecksum(hostedPath,
                                                                      EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertEquals(sha1HostedPathChecksum, calculatedHostedPathChecksum);
        assertNotEquals(calculatedHostedPathChecksum, sha1ProxyPathChecksum);

        mockHostedRepositoryMetadataUpdate(hostedYahrRepository.getId(),
                                           localYahrSourceRepository.getId(),
                                           versionLevelMetadataYahr,
                                           artifactLevelMetadataYahr);

        Files.setLastModifiedTime(proxyPath, oneHourAgo());

        resolvedGroupPath = groupRepositoryProvider.fetchPath(groupPath);

        sha1ProxyPathChecksum = readChecksum(resolveSiblingChecksum(proxyPath, EncryptionAlgorithmsEnum.SHA1));
        assertEquals(sha1ProxyPathChecksum, calculatedHostedPathChecksum);
        calculatedGroupPathChecksum = calculateChecksum(resolvedGroupPath,
                                                        EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        assertEquals(calculatedGroupPathChecksum, sha1ProxyPathChecksum);
    }
}
