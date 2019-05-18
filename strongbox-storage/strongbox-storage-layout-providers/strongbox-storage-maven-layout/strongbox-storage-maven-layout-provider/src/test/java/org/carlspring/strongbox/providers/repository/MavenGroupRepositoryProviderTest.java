package org.carlspring.strongbox.providers.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.maven.artifact.repository.metadata.Metadata;
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
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group.Rule;
import org.carlspring.strongbox.yaml.configuration.repository.MutableMavenRepositoryConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

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
    @ExtendWith({RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class})
    public void testGroupIncludes(@MavenRepository(repositoryId = "grpt-releases-tgi-1") Repository releases1,
                                  @MavenRepository(repositoryId = "grpt-releases-tgi-2") Repository releases2,
                                  @TestRepository.Group({ "grpt-releases-tgi-1",
                                                          "grpt-releases-tgi-2" }) @MavenRepository(repositoryId = "grpt-releases-tgi-group") Repository releasesGroup,
                                  @MavenTestArtifact(repositoryId = "grpt-releases-tgi-1", id = "com.artifacts.in.releases.one:foo", versions = "1.2.3") Path a1,
                                  @MavenTestArtifact(repositoryId = "grpt-releases-tgi-1", id = "com.artifacts.in.releases.under:group", versions = "1.2.3") Path a2,
                                  @MavenTestArtifact(repositoryId = "grpt-releases-tgi-2", id = "com.artifacts.in.releases.four:foo", versions = "1.2.4") Path a3,
                                  @MavenTestArtifact(repositoryId = "grpt-releases-tgi-2", id = "com.artifacts.in.releases.under:group", versions = "1.2.4") Path a4)
            throws Exception
    {
        System.out.println("# Testing group includes...");

        // Test data initialized.
        Repository repository = releasesGroup;
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        RepositoryPath resolvedPath1 = (RepositoryPath) a1.normalize();

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath1);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNotNull(is);
        }

        RepositoryPath resolvedPath2 = (RepositoryPath) a3.normalize();
        repositoryPath = repositoryProvider.fetchPath(resolvedPath2);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {

            assertNotNull(is);
        }
    }

    @Test
    @ExtendWith({RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class})
    public void mavenMetadataFileShouldBeFetchedFromGroupPathRepository(@MavenRepository(repositoryId = "grpt-releases-mmfsbffgpr-1") Repository releases1,
                                                                        @MavenRepository(repositoryId = "grpt-releases-mmfsbffgpr-2") Repository releases2,
                                                                        @TestRepository.Group({ "grpt-releases-mmfsbffgpr-1",
                                                                                                "grpt-releases-mmfsbffgpr-2" }) @MavenRepository(
                                                                                repositoryId = "grpt-releases-mmfsbffgpr-group") Repository releasesGroup,
                                                                        @MavenTestArtifact(repositoryId = "grpt-releases-mmfsbffgpr-1", id = "com.artifacts.in.releases.one:foo", versions = "1.2.3") Path a1,
                                                                        @MavenTestArtifact(repositoryId = "grpt-releases-mmfsbffgpr-1", id = "com.artifacts.in.releases.under123:group", versions = "1.2.3") Path a2,
                                                                        @MavenTestArtifact(repositoryId = "grpt-releases-mmfsbffgpr-2", id = "com.artifacts.in.releases.under123:group", versions = "1.2.4") Path a3)
            throws Exception
    {
        generateMavenMetadata(STORAGE0, "grpt-releases-mmfsbffgpr-1");
        generateMavenMetadata(STORAGE0, "grpt-releases-mmfsbffgpr-2");
        
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(RepositoryTypeEnum.GROUP.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(releasesGroup,
                                                                     "com/artifacts/in/releases/under123/group/maven-metadata.xml");
        
        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        InputStream is = repositoryProvider.getInputStream(repositoryPath);

        assertNotNull(is);

        Metadata metadata = artifactMetadataService.getMetadata(is);

        assertEquals(2, metadata.getVersioning().getVersions().size());
        assertThat(metadata.getVersioning().getVersions(), contains("1.2.3", "1.2.4"));
    }

    @Test
    @ExtendWith({RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class})
    public void testGroupIncludesWithOutOfServiceRepository(@MavenRepository(repositoryId = "grpt-releases-tgiwoosr-1") Repository releases1,
                                                            @MavenRepository(repositoryId = "grpt-releases-tgiwoosr-2") Repository releases2,
                                                            @TestRepository.Group(repositories = { "grpt-releases-tgiwoosr-1",
                                                                                                   "grpt-releases-tgiwoosr-2" }, 
                                                                                  rules = { @Rule(repositories = { "grpt-releases-tgiwoosr-1",
                                                                                                                   "grpt-releases-tgiwoosr-2" },
                                                                                                  pattern = ".*(com|org)/artifacts.in.releases.*"),
                                                                                            @Rule(repositories = { "grpt-releases-tgiwoosr-1" },
                                                                                                  pattern = ".*(com|org)/artifacts.in.*",
                                                                                                  type = RoutingRuleTypeEnum.DENY)})
                                                            @MavenRepository(repositoryId = "grpt-releases-tgiwoosr-group") Repository releasesGroup,
                                                            @MavenTestArtifact(repositoryId = "grpt-releases-tgiwoosr-1", id = "com.artifacts.in.releases.one:foo", versions = "1.2.3") Path a1,
                                                            @MavenTestArtifact(repositoryId = "grpt-releases-tgiwoosr-2", id = "com.artifacts.in.releases.two:foo", versions = "1.2.4") Path a2)
            throws Exception
    {


        // Test data initialized.

        System.out.println("# Testing group includes with out of service repository...");

        configurationManagementService.putOutOfService(STORAGE0, "grpt-releases-tgiwoosr-2");

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(releasesGroup.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(releasesGroup, (RepositoryPath) a2.normalize());

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
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
    @ExtendWith({RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class})
    public void testGroupExcludes(@MavenRepository(repositoryId = "grpt-releases-tge-1") Repository releases1,
                                  @MavenRepository(repositoryId = "grpt-releases-tge-2") Repository releases2,
                                  @TestRepository.Group(repositories = { "grpt-releases-tge-1",
                                                                         "grpt-releases-tge-2" }, 
                                                        rules = { @Rule(repositories = { "grpt-releases-tge-1" },
                                                                        pattern = ".*(com|org)/artifacts.denied.*",
                                                                        type = RoutingRuleTypeEnum.DENY)})
                                  @MavenRepository(repositoryId = "grpt-releases-tge-group") Repository releasesGroup,
                                  @MavenTestArtifact(repositoryId = "grpt-releases-tge-1", id = "com.artifacts.accepted:foo", versions = "1.2.6") Path a1,
                                  @MavenTestArtifact(repositoryId = "grpt-releases-tge-1", id = "com.artifacts.denied.by.wildcard:foo", versions = "1.2.6") Path a2,
                                  @MavenTestArtifact(repositoryId = "grpt-releases-tge-2", id = "com.artifacts.denied.by.wildcard:foo", versions = "1.2.7") Path a3)
            throws Exception
    {
        System.out.println("# Testing group excludes...");
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(releasesGroup.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(releasesGroup, (RepositoryPath) a1.normalize());
        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNotNull(is);
        }

        resolvedPath = repositoryPathResolver.resolve(releasesGroup, (RepositoryPath) a2.normalize());
        repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNull(is);
        }
        
        resolvedPath = repositoryPathResolver.resolve(releasesGroup, (RepositoryPath) a3.normalize());
        repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNotNull(is);
        }
    }

    @Test
    public void deniedRuleShouldBeValid(TestInfo testInfo)
            throws Exception
    {
        String repositoryReleases1Name = getRepositoryName("grpt-releases-drsbv-1", testInfo);

        String repositoryReleases2Name = getRepositoryName("grpt-releases-drsbv-2", testInfo);

        String repositoryGroupName = getRepositoryName("grpt-releases-drsbv-group", testInfo);

        prepareForDeniedTest(repositoryReleases1Name, repositoryReleases2Name, repositoryGroupName);

        createAndAddRoutingRule(STORAGE0,
                                repositoryGroupName,
                                Arrays.asList(new MutableRoutingRuleRepository(STORAGE0, repositoryReleases2Name)),
                                ".*(com|org)/carlspring.metadata.*",
                                RoutingRuleTypeEnum.DENY);

        testDeny(repositoryReleases1Name, repositoryReleases2Name, repositoryGroupName);
    }

    @Test
    public void deniedRoutingRuleShouldWorkIfRepositoryAndStorageIsNotProvided(TestInfo testInfo)
            throws Exception
    {
        String repositoryReleases1Name = getRepositoryName("grpt-releases-drsbv-1", testInfo);

        String repositoryReleases2Name = getRepositoryName("grpt-releases-drsbv-2", testInfo);

        String repositoryGroupName = getRepositoryName("grpt-releases-drsbv-group", testInfo);

        prepareForDeniedTest(repositoryReleases1Name, repositoryReleases2Name, repositoryGroupName);

        createAndAddRoutingRule(STORAGE0,
                                repositoryGroupName,
                                Arrays.asList(new MutableRoutingRuleRepository(null, null)),
                                ".*(com|org)/carlspring.metadata.*",
                                RoutingRuleTypeEnum.DENY);

        testDeny(repositoryReleases1Name, repositoryReleases2Name, repositoryGroupName);
    }

    @Test
    public void deniedRoutingRuleShouldWorkIfRepositoryIsNotProvided(TestInfo testInfo)
            throws Exception
    {

        String repositoryReleases1Name = getRepositoryName("grpt-releases-drsbv-1", testInfo);

        String repositoryReleases2Name = getRepositoryName("grpt-releases-drsbv-2", testInfo);

        String repositoryGroupName = getRepositoryName("grpt-releases-drsbv-group", testInfo);

        prepareForDeniedTest(repositoryReleases1Name, repositoryReleases2Name, repositoryGroupName);

        createAndAddRoutingRule(STORAGE0,
                                repositoryGroupName,
                                Arrays.asList(new MutableRoutingRuleRepository(STORAGE0, null)),
                                ".*(com|org)/carlspring.metadata.*",
                                RoutingRuleTypeEnum.DENY);

        testDeny(repositoryReleases1Name, repositoryReleases2Name, repositoryGroupName);
    }

    @Test
    public void deniedRoutingRuleShouldWorkIfStorageIsNotProvided(TestInfo testInfo)
            throws Exception
    {

        String repositoryReleases1Name = getRepositoryName("grpt-releases-drsbv-1", testInfo);

        String repositoryReleases2Name = getRepositoryName("grpt-releases-drsbv-2", testInfo);

        String repositoryGroupName = getRepositoryName("grpt-releases-drsbv-group", testInfo);

        prepareForDeniedTest(repositoryReleases1Name, repositoryReleases2Name, repositoryGroupName);

        createAndAddRoutingRule(STORAGE0,
                                repositoryGroupName,
                                Arrays.asList(new MutableRoutingRuleRepository(null, repositoryReleases2Name)),
                                ".*(com|org)/carlspring.metadata.*",
                                RoutingRuleTypeEnum.DENY);

        testDeny(repositoryReleases1Name, repositoryReleases2Name, repositoryGroupName);
    }

    @Test
    public void deniedRoutingRuleShouldWorkForAllGroupsUnderTheSameStorage(TestInfo testInfo)
            throws Exception
    {

        String repositoryReleases1Name = getRepositoryName("grpt-releases-drsbv-1", testInfo);

        String repositoryReleases2Name = getRepositoryName("grpt-releases-drsbv-2", testInfo);

        String repositoryGroupName = getRepositoryName("grpt-releases-drsbv-group", testInfo);

        prepareForDeniedTest(repositoryReleases1Name, repositoryReleases2Name, repositoryGroupName);

        createAndAddRoutingRule(STORAGE0,
                                null,
                                Arrays.asList(new MutableRoutingRuleRepository(STORAGE0, repositoryReleases2Name)),
                                ".*(com|org)/carlspring.metadata.*",
                                RoutingRuleTypeEnum.DENY);

        testDeny(repositoryReleases1Name, repositoryReleases2Name, repositoryGroupName);
    }

    @Test
    public void deniedRoutingRuleShouldWorkForAllGroups(TestInfo testInfo)
            throws Exception
    {

        String repositoryReleases1Name = getRepositoryName("grpt-releases-drsbv-1", testInfo);

        String repositoryReleases2Name = getRepositoryName("grpt-releases-drsbv-2", testInfo);

        String repositoryGroupName = getRepositoryName("grpt-releases-drsbv-group", testInfo);

        prepareForDeniedTest(repositoryReleases1Name, repositoryReleases2Name, repositoryGroupName);

        createAndAddRoutingRule(null,
                                null,
                                Arrays.asList(new MutableRoutingRuleRepository(STORAGE0, repositoryReleases2Name)),
                                ".*(com|org)/carlspring.metadata.*",
                                RoutingRuleTypeEnum.DENY);

        testDeny(repositoryReleases1Name, repositoryReleases2Name, repositoryGroupName);
    }

    @Test
    public void deniedRoutingRuleShouldWorkForAllGroupsUnderTheSameRepositoryName(TestInfo testInfo)
            throws Exception
    {

        String repositoryReleases1Name = getRepositoryName("grpt-releases-drsbv-1", testInfo);

        String repositoryReleases2Name = getRepositoryName("grpt-releases-drsbv-2", testInfo);

        String repositoryGroupName = getRepositoryName("grpt-releases-drsbv-group", testInfo);

        prepareForDeniedTest(repositoryReleases1Name, repositoryReleases2Name, repositoryGroupName);

        createAndAddRoutingRule(null,
                                repositoryGroupName,
                                Arrays.asList(new MutableRoutingRuleRepository(STORAGE0, repositoryReleases2Name)),
                                ".*(com|org)/carlspring.metadata.*",
                                RoutingRuleTypeEnum.DENY);

        testDeny(repositoryReleases1Name, repositoryReleases2Name, repositoryGroupName);
    }

    private void prepareForDeniedTest(String repositoryReleases1Name,
                                      String repositoryReleases2Name,
                                      String repositoryGroupName)
            throws Exception
    {
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
    }

    private void testDeny(String repositoryReleases1Name,
                          String repositoryReleases2Name,
                          String repositoryGroupName)

            throws IOException
    {
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
