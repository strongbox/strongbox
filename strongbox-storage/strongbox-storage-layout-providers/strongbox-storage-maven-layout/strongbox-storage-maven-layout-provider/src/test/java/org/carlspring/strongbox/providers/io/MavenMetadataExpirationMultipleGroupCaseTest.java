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
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_HOSTED_YAHR, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_PROXY, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LOCAL_SOURCE, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LOCAL_YAHR_SOURCE, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP, Maven2LayoutProvider.ALIAS));

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

        localYahrSourceRepository = createRepository(STORAGE0,
                                                     REPOSITORY_LOCAL_YAHR_SOURCE,
                                                     RepositoryPolicyEnum.SNAPSHOT.getPolicy(),
                                                     false);

        createRepository(STORAGE0,
                         REPOSITORY_HOSTED_YAHR,
                         RepositoryPolicyEnum.SNAPSHOT.getPolicy(),
                         false);

        mockHostedRepositoryMetadataUpdate(localYahrSourceRepository,
                                           REPOSITORY_HOSTED_YAHR,
                                           REPOSITORY_LOCAL_YAHR_SOURCE,
                                           versionLevelMetadataYahr,
                                           artifactLevelMetadataYahr);

        createProxyRepository(STORAGE0,
                              REPOSITORY_PROXY,
                              "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_HOSTED + "/");

        createGroup(STORAGE0, REPOSITORY_GROUP, REPOSITORY_PROXY, REPOSITORY_HOSTED_YAHR);

        mockResolvingProxiedRemoteArtifactsToHostedRepository();
    }

    @Test
    public void expiredGroupMetadataShouldForceMergeMetadataFromMultipleSubRepositories()
            throws Exception
    {
        final RepositoryPath hostedPath = resolvePath(REPOSITORY_HOSTED, true, "maven-metadata.xml");

        String sha1HostedPathChecksum = readChecksum(resolveSiblingChecksum(hostedPath, EncryptionAlgorithmsEnum.SHA1));
        assertNotNull(sha1HostedPathChecksum);

        final RepositoryPath proxyPath = resolvePath(REPOSITORY_PROXY, true, "maven-metadata.xml");
        final RepositoryPath groupPath = resolvePath(REPOSITORY_GROUP, true, "maven-metadata.xml");
        String sha1ProxyPathChecksum = readChecksum(resolveSiblingChecksum(proxyPath, EncryptionAlgorithmsEnum.SHA1));
        assertNull(sha1ProxyPathChecksum);

        assertFalse(RepositoryFiles.artifactExists(groupPath));

        RepositoryPath resolvedGroupPath = groupRepositoryProvider.fetchPath(groupPath);
        assertTrue(RepositoryFiles.artifactExists(resolvedGroupPath));

        sha1ProxyPathChecksum = readChecksum(resolveSiblingChecksum(proxyPath, EncryptionAlgorithmsEnum.SHA1));
        assertNotNull(sha1ProxyPathChecksum);
        assertThat(sha1ProxyPathChecksum, equalTo(sha1HostedPathChecksum));

        String calculatedGroupPathChecksum = calculateChecksum(resolvedGroupPath,
                                                               EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertThat(sha1ProxyPathChecksum, equalTo(calculatedGroupPathChecksum));

        Files.setLastModifiedTime(proxyPath, oneHourAgo());

        groupRepositoryProvider.fetchPath(groupPath);
        sha1ProxyPathChecksum = readChecksum(resolveSiblingChecksum(proxyPath, EncryptionAlgorithmsEnum.SHA1));
        assertThat(sha1ProxyPathChecksum, equalTo(calculatedGroupPathChecksum));

        mockHostedRepositoryMetadataUpdate(localSourceRepository,
                                           REPOSITORY_HOSTED,
                                           REPOSITORY_LOCAL_SOURCE,
                                           versionLevelMetadata,
                                           artifactLevelMetadata);

        sha1HostedPathChecksum = readChecksum(resolveSiblingChecksum(hostedPath, EncryptionAlgorithmsEnum.SHA1));
        final String calculatedHostedPathChecksum = calculateChecksum(hostedPath,
                                                                      EncryptionAlgorithmsEnum.SHA1.getAlgorithm());
        assertThat(sha1HostedPathChecksum, equalTo(calculatedHostedPathChecksum));
        assertThat(calculatedHostedPathChecksum, not(equalTo(sha1ProxyPathChecksum)));

        mockHostedRepositoryMetadataUpdate(localYahrSourceRepository,
                                           REPOSITORY_HOSTED_YAHR,
                                           REPOSITORY_LOCAL_YAHR_SOURCE,
                                           versionLevelMetadataYahr,
                                           artifactLevelMetadataYahr);

        Files.setLastModifiedTime(proxyPath, oneHourAgo());

        resolvedGroupPath = groupRepositoryProvider.fetchPath(groupPath);

        sha1ProxyPathChecksum = readChecksum(resolveSiblingChecksum(proxyPath, EncryptionAlgorithmsEnum.SHA1));
        assertThat(sha1ProxyPathChecksum, equalTo(calculatedHostedPathChecksum));
        calculatedGroupPathChecksum = calculateChecksum(resolvedGroupPath,
                                                        EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        final RepositoryPath hostedYahrPath = resolvePath(REPOSITORY_HOSTED_YAHR, true, "maven-metadata.xml");
        String calculatedHostedYahrPathChecksum = calculateChecksum(hostedYahrPath,
                                                                    EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        assertThat(calculatedHostedYahrPathChecksum, equalTo(calculatedGroupPathChecksum));
    }

    @After
    public void removeRepositories()
            throws Exception
    {
        closeIndexersForRepository(STORAGE0, REPOSITORY_HOSTED);
        closeIndexersForRepository(STORAGE0, REPOSITORY_HOSTED_YAHR);
        closeIndexersForRepository(STORAGE0, REPOSITORY_LOCAL_SOURCE);
        closeIndexersForRepository(STORAGE0, REPOSITORY_LOCAL_YAHR_SOURCE);
        closeIndexersForRepository(STORAGE0, REPOSITORY_PROXY);
        closeIndexersForRepository(STORAGE0, REPOSITORY_GROUP);
        removeRepositories(getRepositoriesToClean());
        cleanUp();
    }

}
