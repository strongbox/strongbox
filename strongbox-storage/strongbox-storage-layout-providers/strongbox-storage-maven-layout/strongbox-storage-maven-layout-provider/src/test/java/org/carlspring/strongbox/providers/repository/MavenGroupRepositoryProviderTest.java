package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.hamcrest.MatcherAssert.assertThat;
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

    private static final String REPOSITORY_RELEASES_1 = "grpt-releases-1";

    private static final String REPOSITORY_RELEASES_2 = "grpt-releases-2";

    private static final String REPOSITORY_GROUP_WITH_NESTED_GROUP_1 = "grpt-releases-group-with-nested-group-level-1";

    private static final String REPOSITORY_GROUP_WITH_NESTED_GROUP_2 = "grpt-releases-group-with-nested-group-level-2";

    private static final String REPOSITORY_GROUP = "grpt-releases-group";

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

    @BeforeEach
    public void setUp()
            throws Exception
    {
        createRepositoryWithArtifacts(STORAGE0,
                                      REPOSITORY_RELEASES_1,
                                      false,
                                      "com.artifacts.in.releases.one:foo",
                                      "1.2.3");

        createRepositoryWithArtifacts(STORAGE0,
                                      REPOSITORY_RELEASES_1,
                                      false,
                                      "com.artifacts.in.releases.under:group",
                                      "1.2.3");

        createRepositoryWithArtifacts(STORAGE0,
                                      REPOSITORY_RELEASES_2,
                                      false,
                                      "com.artifacts.in.releases.two:foo",
                                      "1.2.4");

        createRepositoryWithArtifacts(STORAGE0,
                                      REPOSITORY_RELEASES_2,
                                      false,
                                      "com.artifacts.in.releases.under:group",
                                      "1.2.4");

        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(false);

        MutableRepository repositoryGroup = mavenRepositoryFactory.createRepository(REPOSITORY_GROUP);
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.setRepositoryConfiguration(mavenRepositoryConfiguration);
        repositoryGroup.addRepositoryToGroup(REPOSITORY_RELEASES_1);
        repositoryGroup.addRepositoryToGroup(REPOSITORY_RELEASES_2);

        createRepository(STORAGE0, repositoryGroup);

        MutableRepository repositoryWithNestedGroupLevel1 = mavenRepositoryFactory.createRepository(REPOSITORY_GROUP_WITH_NESTED_GROUP_1);
        repositoryWithNestedGroupLevel1.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryWithNestedGroupLevel1.setAllowsRedeployment(false);
        repositoryWithNestedGroupLevel1.setAllowsDelete(false);
        repositoryWithNestedGroupLevel1.setAllowsForceDeletion(false);
        repositoryWithNestedGroupLevel1.setRepositoryConfiguration(mavenRepositoryConfiguration);
        repositoryWithNestedGroupLevel1.addRepositoryToGroup(REPOSITORY_GROUP);

        createRepository(STORAGE0, repositoryWithNestedGroupLevel1);

        MutableRepository repositoryWithNestedGroupLevel2 = mavenRepositoryFactory.createRepository(REPOSITORY_GROUP_WITH_NESTED_GROUP_2);
        repositoryWithNestedGroupLevel2.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryWithNestedGroupLevel2.setAllowsRedeployment(false);
        repositoryWithNestedGroupLevel2.setAllowsDelete(false);
        repositoryWithNestedGroupLevel2.setAllowsForceDeletion(false);
        repositoryWithNestedGroupLevel2.setRepositoryConfiguration(mavenRepositoryConfiguration);
        repositoryWithNestedGroupLevel2.addRepositoryToGroup(REPOSITORY_GROUP_WITH_NESTED_GROUP_1);

        createRepository(STORAGE0, repositoryWithNestedGroupLevel2);

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES_2).getAbsolutePath(),
                         "org.carlspring.metadata.by.juan:juancho:1.2.64");

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES_2).getAbsolutePath(),
                         "org.carlspring.metadata.will.not.be:retrieved:1.2.64");

        // Used by the testGroupExcludesWildcardRule() test
        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "com.artifacts.denied.by.wildcard:foo:1.2.6");
        // Used by the testGroupExcludesWildcardRule() test
        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES_2).getAbsolutePath(),
                         "com.artifacts.denied.by.wildcard:foo:1.2.7");

        createRoutingRules();

        generateMavenMetadata(STORAGE0, REPOSITORY_RELEASES_1);
        generateMavenMetadata(STORAGE0, REPOSITORY_RELEASES_2);
    }

    @AfterEach
    public void removeRepositories()
            throws Exception
    {
        removeRepositories(getRepositoriesToClean());
        cleanUp();
    }

    private void createRoutingRules()
    {
        /**
         <accepted>
             <rule-set group-repository="*">
                 <rule pattern=".*(com|org)/artifacts.in.releases1.*">
                     <repositories>
                         <repository>releases</repository>
                     </repositories>
                 </rule>
             </rule-set>
         </accepted>
         */
        createRoutingRuleSet(STORAGE0,
                             "*",
                             new String[]{ REPOSITORY_RELEASES_1 },
                             ".*(com|org)/artifacts.in.releases.one.*",
                             ROUTING_RULE_TYPE_ACCEPTED);

        /**
        <accepted>
            <rule-set group-repository="group-releases">
                <rule pattern=".*(com|org)/artifacts.in.releases.*">
                    <repositories>
                        <repository>releases-with-trash</repository>
                        <repository>releases-with-redeployment</repository>
                    </repositories>
                </rule>
            </rule-set>
         **/
        createRoutingRuleSet(STORAGE0,
                             REPOSITORY_GROUP,
                             new String[]{ REPOSITORY_RELEASES_1, REPOSITORY_RELEASES_2 },
                             ".*(com|org)/artifacts.in.releases.*",
                             ROUTING_RULE_TYPE_ACCEPTED);

        /**
        <denied>
            <rule-set group-repository="*">
                 <rule pattern=".*(com|org)/artifacts.denied.by.wildcard.*">
                     <repositories>
                         <repository>releases</repository>
                     </repositories>
                 </rule>
            </rule-set>
        </denied>
         **/
        createRoutingRuleSet(STORAGE0,
                             "*",
                             new String[]{ REPOSITORY_RELEASES_1 },
                             ".*(com|org)/artifacts.denied.by.wildcard.*",
                             ROUTING_RULE_TYPE_DENIED);

        /**
         <denied>
            <rule-set group-repository="group-releases">
                <rule pattern=".*(com|org)/artifacts.in.releases.*">
                    <repositories>
                        <repository>grpt-releases-1</repository>
                    </repositories>
                </rule>
            </rule-set>
         </denied>
         **/
        createRoutingRuleSet(STORAGE0,
                             REPOSITORY_GROUP,
                             new String[]{ REPOSITORY_RELEASES_1 },
                             ".*(com|org)/artifacts.in.*",
                             ROUTING_RULE_TYPE_DENIED);

        /**
         <denied>
             <rule-set group-repository="grpt-releases-group">
                 <rule pattern=".*(com|org)/carlspring/metadata/will/not/be/retrieved.*">
                     <repositories>
                        <repository>grpt-releases-2</repository>
                     </repositories>
                 </rule>
             </rule-set>
         </denied>
         **/
        createRoutingRuleSet(STORAGE0,
                             REPOSITORY_GROUP,
                             new String[]{ REPOSITORY_RELEASES_2 },
                             ".*(com|org)/carlspring/metadata/will/not/be/retrieved.*",
                             ROUTING_RULE_TYPE_DENIED);
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_WITH_NESTED_GROUP_1, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_WITH_NESTED_GROUP_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @AfterEach
    public void tearDown()
    {
        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + REPOSITORY_RELEASES_1);
        if (!repository.isInService())
        {
            configurationManagementService.putInService(STORAGE0, REPOSITORY_RELEASES_1);
        }
    }

    @Test
    public void testGroupIncludes()
            throws IOException
    {
        System.out.println("# Testing group includes...");

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + REPOSITORY_GROUP);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        Path repositoryPath = repositoryProvider.fetchPath(repositoryPathResolver.resolve(STORAGE0, REPOSITORY_GROUP,
                                                                                          "com/artifacts/in/releases/one/foo/1.2.3/foo-1.2.3.jar"));
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {

            assertNotNull(is);
        }

        repositoryPath = repositoryProvider.fetchPath(repositoryPathResolver.resolve(STORAGE0,
                                                                                     REPOSITORY_GROUP,
                                                                                     "com/artifacts/in/releases/two/foo/1.2.4/foo-1.2.4.jar"));
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {

            assertNotNull(is);
        }
    }

    @Test
    public void mavenMetadataFileShouldBeFetchedFromGroupPathRepository()
            throws Exception
    {
        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + REPOSITORY_GROUP);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        Path repositoryPath = repositoryProvider.fetchPath(repositoryPathResolver.resolve(STORAGE0, REPOSITORY_GROUP,
                                                                                          "com/artifacts/in/releases/under/group/maven-metadata.xml"));
        InputStream is = repositoryProvider.getInputStream(repositoryPath);

        assertNotNull(is);

        Metadata metadata = artifactMetadataService.getMetadata(is);
        
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions(), CoreMatchers.hasItems("1.2.3", "1.2.4"));
    }

    @Test
    public void testGroupIncludesWithOutOfServiceRepository()
            throws IOException
    {
        System.out.println("# Testing group includes with out of service repository...");

        configurationManagementService.putOutOfService(STORAGE0, REPOSITORY_RELEASES_2);

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + REPOSITORY_GROUP);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        Path repositoryPath = repositoryProvider.fetchPath(repositoryPathResolver.resolve(STORAGE0,
                                                                                          REPOSITORY_GROUP,
                                                                                          "com/artifacts/in/releases/two/foo/1.2.4/foo-1.2.4.jar"));
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            configurationManagementService.putInService(STORAGE0, REPOSITORY_RELEASES_2);

            assertNull(is);
        }
    }

    @Test
    public void testGroupIncludesWildcardRule()
            throws IOException
    {
        System.out.println("# Testing group includes with wildcard...");

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + REPOSITORY_GROUP);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        Path repositoryPath = repositoryProvider.fetchPath(repositoryPathResolver.resolve(STORAGE0,
                                                                                          REPOSITORY_GROUP,
                                                                                          "com/artifacts/in/releases/two/foo/1.2.4/foo-1.2.4.jar"));
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNotNull(is);
        }
    }

    @Test
    public void testGroupIncludesWildcardRuleAgainstNestedRepository()
            throws IOException
    {
        System.out.println("# Testing group includes with wildcard against nested repositories...");

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + REPOSITORY_GROUP_WITH_NESTED_GROUP_1);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        Path repositoryPath = repositoryProvider.fetchPath(repositoryPathResolver.resolve(STORAGE0,
                                                                                          REPOSITORY_GROUP_WITH_NESTED_GROUP_1,
                                                                                          "com/artifacts/in/releases/two/foo/1.2.4/foo-1.2.4.jar"));
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNotNull(is);
        }
    }
    
    @Test
    public void testGroupAgainstNestedRepository()
            throws IOException
    {
        System.out.println("# Testing group includes with wildcard against nested repositories...");

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + REPOSITORY_GROUP_WITH_NESTED_GROUP_2);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        Path repositoryPath = repositoryProvider.fetchPath(repositoryPathResolver.resolve(STORAGE0,
                                                                                          REPOSITORY_GROUP_WITH_NESTED_GROUP_2,
                                                                                          "org/carlspring/metadata/by/juan/juancho/1.2.64/juancho-1.2.64.jar"));
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNotNull(is);
        }
    }

    @Test
    public void testGroupExcludes()
            throws IOException
    {
        System.out.println("# Testing group excludes...");

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + REPOSITORY_GROUP);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        Path repositoryPath = repositoryProvider.fetchPath(repositoryPathResolver.resolve(STORAGE0,
                                                                                          REPOSITORY_GROUP,
                                                                                          "com/artifacts/denied/in/memory/foo/1.2.5/foo-1.2.5.jar"));
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNull(is);
        }
    }

    @Test
    public void testGroupExcludesWildcardRule()
            throws IOException
    {
        System.out.println("# Testing group excludes with wildcard...");

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + REPOSITORY_GROUP);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        try (InputStream is = repositoryProvider.getInputStream(repositoryProvider.fetchPath(repositoryPathResolver.resolve(STORAGE0,
                                                                                             REPOSITORY_GROUP,
                                                                                             "com/artifacts/denied/by/wildcard/foo/1.2.6/foo-1.2.6.jar"))))
        {
            assertNull(is);
        }

        // This one should work, as it's in a different repository
        Path repositoryPath = repositoryProvider.fetchPath(repositoryPathResolver.resolve(STORAGE0,
                                                                                          REPOSITORY_GROUP,
                                                                                          "com/artifacts/denied/by/wildcard/foo/1.2.7/foo-1.2.7.jar"));
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNotNull(is);
        }
    }

    @Test
    public void deniedRuleShouldBeValid()
            throws Exception
    {
        System.out.println("# Testing group includes...");

        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + REPOSITORY_GROUP);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        Path repositoryPath = repositoryProvider.fetchPath(repositoryPathResolver.resolve(STORAGE0,
                                                                                          REPOSITORY_GROUP,
                                                                                          "org/carlspring/metadata/will/not/be/retrieved/1.2.64/retrieved-1.2.64.jar"));
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {

            assertNull(is);
        }
    }

}
