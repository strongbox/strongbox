package org.carlspring.strongbox.providers.io;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.repository.GroupRepositoryProvider;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
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
public class MavenMetadataExpirationMultipleGroupCaseTest
        extends BaseMavenMetadataExpirationTest
{

    private static final String REPOSITORY_GROUP = "mvn-group-repo-snapshots";

    private static final String REPOSITORY_HOSTED_YAHR = "mvn-hosted-yahr-repo-snapshots";

    private static final String REPOSITORY_LOCAL_YAHR_SOURCE = "mvn-local-yahr-source-repo-snapshots";

    @Inject
    private GroupRepositoryProvider groupRepositoryProvider;

    private MutableRepository localYahrSourceRepository;

    private MutableObject<Metadata> versionLevelMetadataYahr = new MutableObject<>();

    private MutableObject<Metadata> artifactLevelMetadataYahr = new MutableObject<>();

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

        localYahrSourceRepository = createRepository(STORAGE0,
                                                     getRepositoryName(REPOSITORY_LOCAL_YAHR_SOURCE, testInfo),
                                                     RepositoryPolicyEnum.SNAPSHOT.getPolicy(),
                                                     false);

        createRepository(STORAGE0,
                         getRepositoryName(REPOSITORY_HOSTED_YAHR, testInfo),
                         RepositoryPolicyEnum.SNAPSHOT.getPolicy(),
                         false);

        mockHostedRepositoryMetadataUpdate(localYahrSourceRepository,
                                           getRepositoryName(REPOSITORY_HOSTED_YAHR, testInfo),
                                           getRepositoryName(REPOSITORY_LOCAL_YAHR_SOURCE, testInfo),
                                           versionLevelMetadataYahr,
                                           artifactLevelMetadataYahr);

        createProxyRepository(STORAGE0,
                              getRepositoryName(REPOSITORY_PROXY, testInfo),
                              "http://localhost:48080/storages/" + STORAGE0 + "/" +
                              getRepositoryName(REPOSITORY_HOSTED, testInfo) + "/");

        createGroup(STORAGE0,
                    getRepositoryName(REPOSITORY_GROUP, testInfo),
                    getRepositoryName(REPOSITORY_PROXY, testInfo),
                    getRepositoryName(REPOSITORY_HOSTED_YAHR, testInfo));

        mockResolvingProxiedRemoteArtifactsToHostedRepository(testInfo);
    }

    @Test
    public void expiredGroupMetadataShouldFetchVersionLevelMetadataFromFirstMatch(TestInfo testInfo)
            throws Exception
    {
        final RepositoryPath hostedPath = resolvePath(getRepositoryName(REPOSITORY_HOSTED, testInfo),
                                                      true,
                                                      "maven-metadata.xml");

        String sha1HostedPathChecksum = readChecksum(resolveSiblingChecksum(hostedPath, EncryptionAlgorithmsEnum.SHA1));
        assertNotNull(sha1HostedPathChecksum);


        final RepositoryPath proxyPath = resolvePath(getRepositoryName(REPOSITORY_PROXY, testInfo), true,
                                                     "maven-metadata.xml");
        final RepositoryPath groupPath = resolvePath(getRepositoryName(REPOSITORY_GROUP, testInfo),
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

        mockHostedRepositoryMetadataUpdate(localSourceRepository,
                                           getRepositoryName(REPOSITORY_HOSTED, testInfo),
                                           getRepositoryName(REPOSITORY_LOCAL_SOURCE, testInfo),
                                           versionLevelMetadata,
                                           artifactLevelMetadata);

        sha1HostedPathChecksum = readChecksum(resolveSiblingChecksum(hostedPath, EncryptionAlgorithmsEnum.SHA1));
        final String calculatedHostedPathChecksum = calculateChecksum(hostedPath,
                                                                      EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertEquals(sha1HostedPathChecksum, calculatedHostedPathChecksum);
        assertNotEquals(calculatedHostedPathChecksum, sha1ProxyPathChecksum);

        mockHostedRepositoryMetadataUpdate(localYahrSourceRepository,
                                           getRepositoryName(REPOSITORY_HOSTED_YAHR, testInfo),
                                           getRepositoryName(REPOSITORY_LOCAL_YAHR_SOURCE, testInfo),
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

    private Set<MutableRepository> getRepositories(TestInfo testInfo)
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName(REPOSITORY_HOSTED, testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName(REPOSITORY_HOSTED_YAHR, testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName(REPOSITORY_PROXY, testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName(REPOSITORY_LOCAL_SOURCE, testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName(REPOSITORY_LOCAL_YAHR_SOURCE, testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName(REPOSITORY_GROUP, testInfo),
                                              Maven2LayoutProvider.ALIAS));
        return repositories;
    }

    @AfterEach
    public void removeRepositories(TestInfo testInfo)
            throws Exception
    {
        removeRepositories(getRepositories(testInfo));
    }

}
