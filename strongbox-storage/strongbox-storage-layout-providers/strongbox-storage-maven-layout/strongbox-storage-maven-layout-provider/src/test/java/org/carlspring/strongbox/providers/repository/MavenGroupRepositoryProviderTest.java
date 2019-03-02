package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.routing.MutableRoutingRuleRepository;
import org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.xml.configuration.repository.MutableMavenRepositoryConfiguration;

import javax.inject.Inject;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class MavenGroupRepositoryProviderTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    @Inject
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

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
                                              getRepositoryName("grpt-releases-tgi-1", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tgi-2", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tgi-group", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-mmfsbffgpr-1", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-mmfsbffgpr-2", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-mmfsbffgpr-group", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tgiwoosr-1", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tgiwoosr-2", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tgiwoosr-group", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tgiwr-1", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tgiwr-2", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tgiwr-group", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tgiwr-1", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tgiwr-2", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tgiwr-group", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tgiwranr-1", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tgiwranr-2", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tgiwranr-group", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName(
                                                      "grpt-releases-tgiwranr-group-with-nested-group-level-1",
                                                      testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, getRepositoryName("grpt-releases-tganr-1", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName(
                                                      "grpt-releases-tganr-test-group-against-nested-repository",
                                                      testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tge-1", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tge-2", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-tge-group", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-drsbv-1", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-drsbv-2", testInfo),
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName("grpt-releases-drsbv-group", testInfo),
                                              Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Test
    public void testGroupIncludes(TestInfo testInfo)
            throws Exception
    {
        System.out.println("# Testing group includes...");

        String repositoryReleases1Name = getRepositoryName("grpt-releases-tgi-1", testInfo);

        String repositoryReleases2Name = getRepositoryName("grpt-releases-tgi-2", testInfo);

        String repositoryGroupName = getRepositoryName("grpt-releases-tgi-group", testInfo);

        // Initialize test data
        createRepository(STORAGE0, repositoryReleases1Name, false);

        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases1Name).getAbsolutePath(),
                         "com.artifacts.in.releases.one:foo",
                         new String[]{ "1.2.3" });

        createRepositoryWithArtifacts(STORAGE0,
                                      repositoryReleases1Name,
                                      false,
                                      "com.artifacts.in.releases.under:group",
                                      "1.2.3");

        createRepository(STORAGE0, repositoryReleases2Name, false);
        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases2Name).getAbsolutePath(),
                         "com.artifacts.in.releases.four:foo",
                         new String[]{ "1.2.4" });

        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases2Name).getAbsolutePath(),
                         "com.artifacts.in.releases.under:group",
                         new String[]{ "1.2.4" });

        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(false);

        MutableRepository repositoryGroup = mavenRepositoryFactory.createRepository(repositoryGroupName);
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.setRepositoryConfiguration(mavenRepositoryConfiguration);
        repositoryGroup.addRepositoryToGroup(repositoryReleases1Name);
        repositoryGroup.addRepositoryToGroup(repositoryReleases2Name);

        createRepository(STORAGE0, repositoryGroup);
        // Test data initialized.

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + repositoryGroupName);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        RepositoryPath resolvedPath1 = repositoryPathResolver.resolve(STORAGE0,
                                                                      repositoryGroupName,
                                                                      "com/artifacts/in/releases/one/foo/1.2.3/foo-1.2.3.jar");

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath1);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNotNull(is);
        }

        RepositoryPath resolvedPath2 = repositoryPathResolver.resolve(STORAGE0,
                                                                      repositoryGroupName,
                                                                      "com/artifacts/in/releases/four/foo/1.2.4/foo-1.2.4.jar");
        repositoryPath = repositoryProvider.fetchPath(resolvedPath2);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {

            assertNotNull(is);
        }
    }

    @Test
    public void mavenMetadataFileShouldBeFetchedFromGroupPathRepository(TestInfo testInfo)
            throws Exception
    {
        String repositoryReleases1Name = getRepositoryName("grpt-releases-mmfsbffgpr-1", testInfo);

        String repositoryReleases2Name = getRepositoryName("grpt-releases-mmfsbffgpr-2", testInfo);

        String repositoryGroupName = getRepositoryName("grpt-releases-mmfsbffgpr-group", testInfo);

        // Initialize test data
        createRepository(STORAGE0, repositoryReleases1Name, false);

        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases1Name).getAbsolutePath(),
                         "com.artifacts.in.releases.one:foo",
                         new String[]{ "1.2.3" });

        createRepositoryWithArtifacts(STORAGE0,
                                      repositoryReleases1Name,
                                      false,
                                      "com.artifacts.in.releases.under123:group",
                                      "1.2.3");

        createRepository(STORAGE0, repositoryReleases2Name, false);

        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases2Name).getAbsolutePath(),
                         "com.artifacts.in.releases.under123:group",
                         new String[]{ "1.2.4" });

        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(false);

        MutableRepository repositoryGroup = mavenRepositoryFactory.createRepository(repositoryGroupName);
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.setRepositoryConfiguration(mavenRepositoryConfiguration);
        repositoryGroup.addRepositoryToGroup(repositoryReleases1Name);
        repositoryGroup.addRepositoryToGroup(repositoryReleases2Name);

        createRepository(STORAGE0, repositoryGroup);

        generateMavenMetadata(STORAGE0, repositoryReleases1Name);
        generateMavenMetadata(STORAGE0, repositoryReleases2Name);
        // Test data initialized.

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + repositoryGroupName);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(STORAGE0,
                                                                     repositoryGroupName,
                                                                     "com/artifacts/in/releases/under123/group/maven-metadata.xml");

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        InputStream is = repositoryProvider.getInputStream(repositoryPath);

        assertNotNull(is);

        Metadata metadata = artifactMetadataService.getMetadata(is);

        assertEquals(metadata.getVersioning().getVersions().size(), 2);
        assertThat(metadata.getVersioning().getVersions(), contains("1.2.3", "1.2.4"));
    }

    @Test
    public void testGroupIncludesWithOutOfServiceRepository(TestInfo testInfo)
            throws Exception
    {
        String repositoryReleases1Name = getRepositoryName("grpt-releases-tgiwoosr-1", testInfo);

        String repositoryReleases2Name = getRepositoryName("grpt-releases-tgiwoosr-2", testInfo);

        String repositoryGroupName = getRepositoryName("grpt-releases-tgiwoosr-group", testInfo);

        // Initialize test data
        createRepository(STORAGE0, repositoryReleases1Name, false);

        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases1Name).getAbsolutePath(),
                         "com.artifacts.in.releases.one:foo",
                         new String[]{ "1.2.3" });

        createRepository(STORAGE0, repositoryReleases2Name, false);

        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases2Name).getAbsolutePath(),
                         "com.artifacts.in.releases.two:foo",
                         new String[]{ "1.2.4" });

        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(false);

        MutableRepository repositoryGroup = mavenRepositoryFactory.createRepository(repositoryGroupName);
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.setRepositoryConfiguration(mavenRepositoryConfiguration);
        repositoryGroup.addRepositoryToGroup(repositoryReleases1Name);
        repositoryGroup.addRepositoryToGroup(repositoryReleases2Name);

        createRepository(STORAGE0, repositoryGroup);

        createAndAddRoutingRule(STORAGE0,
                                repositoryGroupName,
                                Arrays.asList(new MutableRoutingRuleRepository(STORAGE0, repositoryReleases1Name),
                                        new MutableRoutingRuleRepository(STORAGE0, repositoryReleases2Name)),
                                ".*(com|org)/artifacts.in.releases.*",
                                RoutingRuleTypeEnum.ACCEPT);

        createAndAddRoutingRule(STORAGE0,
                                repositoryGroupName,
                                Arrays.asList(new MutableRoutingRuleRepository(STORAGE0, repositoryReleases1Name)),
                                ".*(com|org)/artifacts.in.*",
                                RoutingRuleTypeEnum.DENY);
        // Test data initialized.

        System.out.println("# Testing group includes with out of service repository...");

        configurationManagementService.putOutOfService(STORAGE0, repositoryReleases2Name);

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + repositoryGroupName);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(STORAGE0,
                                                                     repositoryGroupName,
                                                                     "com/artifacts/in/releases/two/foo/1.2.4/foo-1.2.4.jar");

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            configurationManagementService.putInService(STORAGE0, repositoryReleases2Name);

            assertNull(is);
        }
    }

    @Test
    public void testGroupIncludesWildcardRule(TestInfo testInfo)
            throws Exception
    {
        System.out.println("# Testing group includes with wildcard...");

        String repositoryReleases1Name = getRepositoryName("grpt-releases-tgiwr-1", testInfo);

        String repositoryReleases2Name = getRepositoryName("grpt-releases-tgiwr-2", testInfo);

        String repositoryGroupName = getRepositoryName("grpt-releases-tgiwr-group", testInfo);

        // Initialize test data
        createRepository(STORAGE0, repositoryReleases1Name, false);

        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases1Name).getAbsolutePath(),
                         "com.artifacts.in.releases.three:foo",
                         new String[]{ "1.2.3" });

        createRepositoryWithArtifacts(STORAGE0,
                                      repositoryReleases1Name,
                                      false,
                                      "com.artifacts.in.releases.under2:group",
                                      "1.2.3");

        createRepository(STORAGE0, repositoryReleases2Name, false);
        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases2Name).getAbsolutePath(),
                         "com.artifacts.in.releases.two:foo",
                         new String[]{ "1.2.4" });

        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases2Name).getAbsolutePath(),
                         "com.artifacts.in.releases.under2:group",
                         new String[]{ "1.2.4" });

        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(false);

        MutableRepository repositoryGroup = mavenRepositoryFactory.createRepository(repositoryGroupName);
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.setRepositoryConfiguration(mavenRepositoryConfiguration);
        repositoryGroup.addRepositoryToGroup(repositoryReleases1Name);
        repositoryGroup.addRepositoryToGroup(repositoryReleases2Name);

        createRepository(STORAGE0, repositoryGroup);
        // Test data initialized.

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + repositoryGroupName);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(STORAGE0,
                                                                     repositoryGroupName,
                                                                     "com/artifacts/in/releases/two/foo/1.2.4/foo-1.2.4.jar");

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNotNull(is);
        }
    }

    @Test
    public void testGroupIncludesWildcardRuleAgainstNestedRepository(TestInfo testInfo)
            throws Exception
    {
        System.out.println("# Testing group includes with wildcard against nested repositories...");

        String repositoryReleases1Name = getRepositoryName("grpt-releases-tgiwranr-1", testInfo);

        String repositoryReleases2Name = getRepositoryName("grpt-releases-tgiwranr-2", testInfo);

        String repositoryGroupName = getRepositoryName("grpt-releases-tgiwranr-group", testInfo);

        String repositoryGroupWithNestedGroup1 = getRepositoryName(
                "grpt-releases-tgiwranr-group-with-nested-group-level-1", testInfo);

        // Initialize test data
        createRepository(STORAGE0, repositoryReleases1Name, false);

        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases1Name).getAbsolutePath(),
                         "com.artifacts.in.releases.one:foo",
                         new String[]{ "1.2.3" });

        createRepositoryWithArtifacts(STORAGE0,
                                      repositoryReleases1Name,
                                      false,
                                      "com.artifacts.in.releases.under3:group",
                                      "1.2.3");

        createRepository(STORAGE0, repositoryReleases2Name, false);
        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases2Name).getAbsolutePath(),
                         "com.artifacts.in.releases.two:foo",
                         new String[]{ "1.2.4" });

        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases2Name).getAbsolutePath(),
                         "com.artifacts.in.releases.under3:group",
                         new String[]{ "1.2.4" });

        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(false);

        MutableRepository repositoryGroup = mavenRepositoryFactory.createRepository(repositoryGroupName);
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.setRepositoryConfiguration(mavenRepositoryConfiguration);
        repositoryGroup.addRepositoryToGroup(repositoryReleases1Name);
        repositoryGroup.addRepositoryToGroup(repositoryReleases2Name);

        createRepository(STORAGE0, repositoryGroup);

        MutableRepository repositoryWithNestedGroupLevel1 = mavenRepositoryFactory.createRepository(
                repositoryGroupWithNestedGroup1);
        repositoryWithNestedGroupLevel1.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryWithNestedGroupLevel1.setAllowsRedeployment(false);
        repositoryWithNestedGroupLevel1.setAllowsDelete(false);
        repositoryWithNestedGroupLevel1.setAllowsForceDeletion(false);
        repositoryWithNestedGroupLevel1.setRepositoryConfiguration(mavenRepositoryConfiguration);
        repositoryWithNestedGroupLevel1.addRepositoryToGroup(repositoryGroupName);

        createRepository(STORAGE0, repositoryWithNestedGroupLevel1);
        // Test data initialized.

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + repositoryGroupWithNestedGroup1);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(STORAGE0,
                                                                     repositoryGroupWithNestedGroup1,
                                                                     "com/artifacts/in/releases/two/foo/1.2.4/foo-1.2.4.jar");

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNotNull(is);
        }
    }

    @Test
    public void testGroupAgainstNestedRepository(TestInfo testInfo)
            throws Exception
    {
        String repositoryReleases1Name = getRepositoryName("grpt-releases-tganr-1", testInfo);

        String repositoryGroupName = getRepositoryName("grpt-releases-tganr-test-group-against-nested-repository",
                                                       testInfo);

        createRepository(STORAGE0, repositoryReleases1Name, false);

        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases1Name).getAbsolutePath(),
                         "org.carlspring.metadata.by.juan:juancho:1.2.64");

        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(false);

        MutableRepository repositoryGroup = mavenRepositoryFactory.createRepository(repositoryGroupName);
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.setRepositoryConfiguration(mavenRepositoryConfiguration);
        repositoryGroup.addRepositoryToGroup(repositoryReleases1Name);

        createRepository(STORAGE0, repositoryGroup);

        System.out.println("# Testing group includes with wildcard against nested repositories...");

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + repositoryGroupName);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(STORAGE0,
                                                                     repositoryGroupName,
                                                                     "org/carlspring/metadata/by/juan/juancho/1.2.64/juancho-1.2.64.jar");

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNotNull(is);
        }
    }

    @Test
    public void testGroupExcludes(TestInfo testInfo)
            throws Exception
    {
        System.out.println("# Testing group excludes...");

        String repositoryReleases1Name = getRepositoryName("grpt-releases-tge-1", testInfo);

        String repositoryReleases2Name = getRepositoryName("grpt-releases-tge-2", testInfo);

        String repositoryGroupName = getRepositoryName("grpt-releases-tge-group", testInfo);

        // Initialize test data
        createRepository(STORAGE0, repositoryReleases1Name, false);
        createRepository(STORAGE0, repositoryReleases2Name, false);

        // Used by the testGroupExcludesWildcardRule() test
        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases1Name).getAbsolutePath(),
                         "com.artifacts.accepted:foo:1.2.6");

        // Used by the testGroupExcludesWildcardRule() test
        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases2Name).getAbsolutePath(),
                         "com.artifacts.denied.by.wildcard:foo:1.2.7");

        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(false);

        MutableRepository repositoryGroup = mavenRepositoryFactory.createRepository(repositoryGroupName);
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.setRepositoryConfiguration(mavenRepositoryConfiguration);
        repositoryGroup.addRepositoryToGroup(repositoryReleases1Name);
        repositoryGroup.addRepositoryToGroup(repositoryReleases2Name);

        createRepository(STORAGE0, repositoryGroup);

        createAndAddRoutingRule(STORAGE0,
                                repositoryGroupName,
                                Arrays.asList(new MutableRoutingRuleRepository(STORAGE0, repositoryReleases1Name)),
                                ".*(com|org)/artifacts.denied.*",
                                RoutingRuleTypeEnum.DENY);
        // Test data initialized.

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + repositoryGroupName);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(STORAGE0,
                                                                     repositoryGroupName,
                                                                     "com/artifacts/accepted/foo/1.2.6/foo-1.2.6.jar");

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNotNull(is);
        }

        resolvedPath = repositoryPathResolver.resolve(STORAGE0,
                                                      repositoryGroupName,
                                                      "com/artifacts/denied/foo/1.2.7/foo-1.2.7.jar");

        repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNull(is);
        }
    }

    @Test
    public void deniedRuleShouldBeValid(TestInfo testInfo)
            throws Exception
    {
        String repositoryReleases1Name = getRepositoryName("grpt-releases-drsbv-1", testInfo);

        String repositoryReleases2Name = getRepositoryName("grpt-releases-drsbv-2", testInfo);

        String repositoryGroupName = getRepositoryName("grpt-releases-drsbv-group", testInfo);

        // Initialize test data
        createRepository(STORAGE0, repositoryReleases1Name, false);
        createRepository(STORAGE0, repositoryReleases2Name, false);

        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases1Name).getAbsolutePath(),
                         "com.artifacts.accepted:foo:1.2.6");

        generateArtifact(getRepositoryBasedir(STORAGE0, repositoryReleases2Name).getAbsolutePath(),
                         "org.carlspring.metadata.will.not.be:retrieved:1.2.64");

        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(false);

        MutableRepository repositoryGroup = mavenRepositoryFactory.createRepository(repositoryGroupName);
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.setRepositoryConfiguration(mavenRepositoryConfiguration);
        repositoryGroup.addRepositoryToGroup(repositoryReleases1Name);
        repositoryGroup.addRepositoryToGroup(repositoryReleases2Name);

        createRepository(STORAGE0, repositoryGroup);

        createAndAddRoutingRule(STORAGE0,
                                repositoryGroupName,
                                Arrays.asList(new MutableRoutingRuleRepository(STORAGE0, repositoryReleases2Name)),
                                ".*(com|org)/artifacts.denied.*",
                                RoutingRuleTypeEnum.DENY);

        generateMavenMetadata(STORAGE0, repositoryReleases1Name);
        generateMavenMetadata(STORAGE0, repositoryReleases2Name);
        // Test data initialized.

        System.out.println("# Testing group excludes...");

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + repositoryGroupName);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(STORAGE0,
                                                                     repositoryGroupName,
                                                                     "org/carlspring/metadata/will/not/be/retrieved/1.2.64/retrieved-1.2.64.jar");

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNull(is);
        }
    }

}
