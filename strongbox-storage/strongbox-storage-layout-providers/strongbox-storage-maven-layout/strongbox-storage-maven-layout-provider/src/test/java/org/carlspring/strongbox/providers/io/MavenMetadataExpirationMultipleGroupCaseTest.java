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
import static org.assertj.core.api.Assertions.assertThat;
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

    private static final String REPOSITORY_LOCAL_SOURCE = "mmemgc-local-source-repo-snapshots";

    private static final String REPOSITORY_HOSTED = "mmemgc-hosted-repo-snapshots";

    private static final String REPOSITORY_PROXY = "mmemgc-proxy-repo-snapshots";

    private static final String PROXY_REPOSITORY_URL =
            "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_HOSTED + "/";

    private static final String REPOSITORY_GROUP = "mmemgc-group-repo-snapshots";

    private static final String REPOSITORY_HOSTED_YAHR = "mmemgc-hosted-yahr-repo-snapshots";

    private static final String REPOSITORY_LOCAL_YAHR_SOURCE = "mmemgc-local-yahr-source-repo-snapshots";

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
            @Group(repositories = { REPOSITORY_PROXY,
                                    REPOSITORY_HOSTED_YAHR })
            @MavenRepository(repositoryId = REPOSITORY_GROUP)
            Repository groupRepository)
            throws Exception
    {
        mockHostedRepositoryMetadataUpdate(hostedRepository.getId(),
                                           localSourceRepository.getId(),
                                           versionLevelMetadata,
                                           artifactLevelMetadata);

        mockHostedRepositoryMetadataUpdate(hostedYahrRepository.getId(),
                                           localYahrSourceRepository.getId(),
                                           versionLevelMetadataYahr,
                                           artifactLevelMetadataYahr);

        mockResolvingProxiedRemoteArtifactsToHostedRepository(REPOSITORY_HOSTED);

        final RepositoryPath hostedPath = resolvePath(hostedRepository.getId(),
                                                      true,
                                                      "maven-metadata.xml");

        String sha1HostedPathChecksum = readChecksum(resolveSiblingChecksum(hostedPath, EncryptionAlgorithmsEnum.SHA1));
        assertThat(sha1HostedPathChecksum).isNotNull();


        final RepositoryPath proxyPath = resolvePath(proxyRepository.getId(),
                                                     true,
                                                     "maven-metadata.xml");

        final RepositoryPath groupPath = resolvePath(groupRepository.getId(),
                                                     true,
                                                     "maven-metadata.xml");

        String sha1ProxyPathChecksum = readChecksum(resolveSiblingChecksum(proxyPath, EncryptionAlgorithmsEnum.SHA1));
        assertThat(sha1ProxyPathChecksum).isNull();

        assertThat(RepositoryFiles.artifactExists(groupPath)).isFalse();

        RepositoryPath resolvedGroupPath = groupRepositoryProvider.fetchPath(groupPath);
        assertThat(RepositoryFiles.artifactExists(resolvedGroupPath)).isTrue();

        sha1ProxyPathChecksum = readChecksum(resolveSiblingChecksum(proxyPath, EncryptionAlgorithmsEnum.SHA1));
        assertThat(sha1ProxyPathChecksum).isNotNull();
        assertThat(sha1HostedPathChecksum).isEqualTo(sha1ProxyPathChecksum);

        String calculatedGroupPathChecksum = calculateChecksum(resolvedGroupPath,
                                                               EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertThat(calculatedGroupPathChecksum).isEqualTo(sha1ProxyPathChecksum);

        Files.setLastModifiedTime(proxyPath, oneHourAgo());

        groupRepositoryProvider.fetchPath(groupPath);
        sha1ProxyPathChecksum = readChecksum(resolveSiblingChecksum(proxyPath, EncryptionAlgorithmsEnum.SHA1));
        assertThat(calculatedGroupPathChecksum).isEqualTo(sha1ProxyPathChecksum);

        mockHostedRepositoryMetadataUpdate(hostedRepository.getId(),
                                           localSourceRepository.getId(),
                                           versionLevelMetadata,
                                           artifactLevelMetadata);

        sha1HostedPathChecksum = readChecksum(resolveSiblingChecksum(hostedPath, EncryptionAlgorithmsEnum.SHA1));
        final String calculatedHostedPathChecksum = calculateChecksum(hostedPath,
                                                                      EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertThat(calculatedHostedPathChecksum).isEqualTo(sha1HostedPathChecksum);
        assertThat(sha1ProxyPathChecksum).isNotEqualTo(calculatedHostedPathChecksum);

        mockHostedRepositoryMetadataUpdate(hostedYahrRepository.getId(),
                                           localYahrSourceRepository.getId(),
                                           versionLevelMetadataYahr,
                                           artifactLevelMetadataYahr);

        Files.setLastModifiedTime(proxyPath, oneHourAgo());

        resolvedGroupPath = groupRepositoryProvider.fetchPath(groupPath);

        sha1ProxyPathChecksum = readChecksum(resolveSiblingChecksum(proxyPath, EncryptionAlgorithmsEnum.SHA1));
        assertThat(calculatedHostedPathChecksum).isEqualTo(sha1ProxyPathChecksum);
        calculatedGroupPathChecksum = calculateChecksum(resolvedGroupPath,
                                                        EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        assertThat(sha1ProxyPathChecksum).isEqualTo(calculatedGroupPathChecksum);
    }

    @Override
    protected String getRepositoryHostedId()
    {
        return REPOSITORY_HOSTED;
    }

    @Override
    protected String getRepositoryProxyId()
    {
        return REPOSITORY_PROXY;
    }
}
