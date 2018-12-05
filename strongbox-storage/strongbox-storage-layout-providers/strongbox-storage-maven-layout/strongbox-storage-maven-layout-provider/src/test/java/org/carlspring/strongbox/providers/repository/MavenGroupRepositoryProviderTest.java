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
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.xml.configuration.repository.MutableMavenRepositoryConfiguration;

import javax.inject.Inject;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author mtodorov
 */
@ExtendWith(SpringExtension.class)
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

    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @AfterEach
    public void removeRepositories()
            throws Exception
    {
        removeRepositories(getRepositoriesToClean());
        cleanUp();
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tgi-1", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tgi-2", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tgi-group", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-mmfsbffgpr-1", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-mmfsbffgpr-2", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-mmfsbffgpr-group", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tgiwoosr-1", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tgiwoosr-2", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tgiwoosr-group", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tgiwr-1", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tgiwr-2", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tgiwr-group", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tgiwr-1", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tgiwr-2", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tgiwr-group", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tgiwranr-1", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tgiwranr-2", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tgiwranr-group", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tgiwranr-group-with-nested-group-level-1",
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tganr-1", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tganr-test-group-against-nested-repository",
                                              Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tge-1", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tge-2", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-tge-group", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-drsbv-1", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-drsbv-2", Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, "grpt-releases-drsbv-group", Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Test
    public void testGroupIncludes()
            throws Exception
    {
        System.out.println("# Testing group includes...");

        String repositoryReleases1Name = "grpt-releases-tgi-1";

        String repositoryReleases2Name = "grpt-releases-tgi-2";

        String repositoryGroupName = "grpt-releases-tgi-group";

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
                         "com.artifacts.in.releases.two:foo",
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
                                                                      "com/artifacts/in/releases/two/foo/1.2.4/foo-1.2.4.jar");
        repositoryPath = repositoryProvider.fetchPath(resolvedPath2);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {

            assertNotNull(is);
        }
    }

    @Test
    public void mavenMetadataFileShouldBeFetchedFromGroupPathRepository()
            throws Exception
    {
        String repositoryReleases1Name = "grpt-releases-mmfsbffgpr-1";

        String repositoryReleases2Name = "grpt-releases-mmfsbffgpr-2";

        String repositoryGroupName = "grpt-releases-mmfsbffgpr-group";

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

        generateMavenMetadata(STORAGE0, repositoryReleases1Name);
        generateMavenMetadata(STORAGE0, repositoryReleases2Name);
        // Test data initialized.

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + repositoryGroupName);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(STORAGE0,
                                                                     repositoryGroupName,
                                                                     "com/artifacts/in/releases/under/group/maven-metadata.xml");

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        InputStream is = repositoryProvider.getInputStream(repositoryPath);

        assertNotNull(is);

        Metadata metadata = artifactMetadataService.getMetadata(is);
        
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions(), CoreMatchers.hasItems("1.2.3", "1.2.4"));
    }

    @Test
    public void testGroupIncludesWithOutOfServiceRepository()
            throws Exception
    {
        String repositoryReleases1Name = "grpt-releases-tgiwoosr-1";

        String repositoryReleases2Name = "grpt-releases-tgiwoosr-2";

        String repositoryGroupName = "grpt-releases-tgiwoosr-group";

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

        createRoutingRuleSet(STORAGE0,
                             repositoryGroupName,
                             new String[]{ repositoryReleases1Name, repositoryReleases2Name },
                             ".*(com|org)/artifacts.in.releases.*",
                             ROUTING_RULE_TYPE_ACCEPTED);

        createRoutingRuleSet(STORAGE0,
                             repositoryGroupName,
                             new String[]{ repositoryReleases1Name },
                             ".*(com|org)/artifacts.in.*",
                             ROUTING_RULE_TYPE_DENIED);
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
    public void testGroupIncludesWildcardRule()
            throws Exception
    {
        System.out.println("# Testing group includes with wildcard...");

        String repositoryReleases1Name = "grpt-releases-tgiwr-1";

        String repositoryReleases2Name = "grpt-releases-tgiwr-2";

        String repositoryGroupName = "grpt-releases-tgiwr-group";

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
                         "com.artifacts.in.releases.two:foo",
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
    public void testGroupIncludesWildcardRuleAgainstNestedRepository()
            throws Exception
    {
        System.out.println("# Testing group includes with wildcard against nested repositories...");

        String repositoryReleases1Name = "grpt-releases-tgiwranr-1";

        String repositoryReleases2Name = "grpt-releases-tgiwranr-2";

        String repositoryGroupName = "grpt-releases-tgiwranr-group";

        String repositoryGroupWithNestedGroup1 = "grpt-releases-tgiwranr-group-with-nested-group-level-1";

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
                         "com.artifacts.in.releases.two:foo",
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

        MutableRepository repositoryWithNestedGroupLevel1 = mavenRepositoryFactory.createRepository(repositoryGroupWithNestedGroup1);
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
    public void testGroupAgainstNestedRepository()
            throws Exception
    {
        String repositoryReleases1Name = "grpt-releases-tganr-1";

        String repositoryGroupName = "grpt-releases-tganr-test-group-against-nested-repository";

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
    public void testGroupExcludes()
            throws Exception
    {
        System.out.println("# Testing group excludes...");

        String repositoryReleases1Name = "grpt-releases-tge-1";

        String repositoryReleases2Name = "grpt-releases-tge-2";

        String repositoryGroupName = "grpt-releases-tge-group";

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

        createRoutingRuleSet(STORAGE0,
                             repositoryGroupName,
                             new String[]{ repositoryReleases1Name },
                             ".*(com|org)/artifacts.denied.*",
                             ROUTING_RULE_TYPE_DENIED);

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
    public void deniedRuleShouldBeValid()
            throws Exception
    {
        String repositoryReleases1Name = "grpt-releases-drsbv-1";

        String repositoryReleases2Name = "grpt-releases-drsbv-2";

        String repositoryGroupName = "grpt-releases-drsbv-group";

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

        createRoutingRuleSet(STORAGE0,
                             repositoryGroupName,
                             new String[]{ repositoryReleases2Name },
                             ".*(com|org)/carlspring.metadata.*",
                             ROUTING_RULE_TYPE_DENIED);

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
