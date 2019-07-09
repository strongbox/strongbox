package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.routing.MutableRoutingRuleRepository;
import org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryAttributes;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group.Rule;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private static final String MGRPT_RELEASES_1 = "mgrpt-releases-1";
    private static final String MGRPT_RELEASES_2 = "mgrpt-releases-2";
    private static final String MGRPT_RELEASES_GROUP = "mgrpt-releases-group";
    private static final String MGRPT_RELEASES_GROUP_WITH_NESTED_GROUP_LEVEL_1 = "mgrpt-releases-group-with-nested-group-level-1";

    @Inject
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testGroupIncludes(@MavenRepository(repositoryId = MGRPT_RELEASES_1) Repository releases1,
                                  @MavenRepository(repositoryId = MGRPT_RELEASES_2) Repository releases2,
                                  @Group({ MGRPT_RELEASES_1,
                                           MGRPT_RELEASES_2 })
                                  @MavenRepository(repositoryId = MGRPT_RELEASES_GROUP) Repository releasesGroup,
                                  @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.in.releases.one:foo", versions = "1.2.3") Path a1,
                                  @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.in.releases.under:group", versions = "1.2.3") Path a2,
                                  @MavenTestArtifact(repositoryId = MGRPT_RELEASES_2, id = "com.artifacts.in.releases.four:foo", versions = "1.2.4") Path a3,
                                  @MavenTestArtifact(repositoryId = MGRPT_RELEASES_2, id = "com.artifacts.in.releases.under:group", versions = "1.2.4") Path a4)
            throws Exception
    {
        System.out.println("# Testing group includes...");

        // Test data initialized.
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(releasesGroup.getType());

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

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void mavenMetadataFileShouldBeFetchedFromGroupPathRepository(@MavenRepository(repositoryId = MGRPT_RELEASES_1) Repository releases1,
                                                                        @MavenRepository(repositoryId = MGRPT_RELEASES_2) Repository releases2,
                                                                        @Group({ MGRPT_RELEASES_1,
                                                                                 MGRPT_RELEASES_2 })
                                                                        @MavenRepository(repositoryId = MGRPT_RELEASES_GROUP) Repository releasesGroup,
                                                                        @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.in.releases.one:foo", versions = "1.2.3") Path a1,
                                                                        @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.in.releases.under123:group", versions = "1.2.3") Path a2,
                                                                        @MavenTestArtifact(repositoryId = MGRPT_RELEASES_2, id = "com.artifacts.in.releases.under123:group", versions = "1.2.4") Path a3)
            throws Exception
    {
        generateMavenMetadata(STORAGE0, releases1.getId());
        generateMavenMetadata(STORAGE0, releases2.getId());
        
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

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testGroupIncludesWithOutOfServiceRepository(@MavenRepository(repositoryId = MGRPT_RELEASES_1) Repository releases1,
                                                            @MavenRepository(repositoryId = MGRPT_RELEASES_2) Repository releases2,
                                                            @Group(repositories = { MGRPT_RELEASES_1,
                                                                                    MGRPT_RELEASES_2 },
                                                                   rules = { @Rule(repositories = { MGRPT_RELEASES_1,
                                                                                                    MGRPT_RELEASES_2 },
                                                                                   pattern = ".*(com|org)/artifacts.in.releases.*"),
                                                                             @Rule(repositories = { MGRPT_RELEASES_1 },
                                                                                   pattern = ".*(com|org)/artifacts.in.*",
                                                                                   type = RoutingRuleTypeEnum.DENY)})
                                                            @MavenRepository(repositoryId = MGRPT_RELEASES_GROUP) Repository releasesGroup,
                                                            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.in.releases.one:foo", versions = "1.2.3") Path a1,
                                                            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_2, id = "com.artifacts.in.releases.two:foo", versions = "1.2.4") Path a2)
            throws Exception
    {


        // Test data initialized.

        System.out.println("# Testing group includes with out of service repository...");

        configurationManagementService.putOutOfService(STORAGE0, releases2.getId());

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(releasesGroup.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(releasesGroup, (RepositoryPath) a2.normalize());

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNull(is);
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testGroupIncludesWildcardRule(@MavenRepository(repositoryId = MGRPT_RELEASES_1) Repository releases1,
                                              @MavenRepository(repositoryId = MGRPT_RELEASES_2) Repository releases2,
                                              @Group(repositories = { MGRPT_RELEASES_1,
                                                                      MGRPT_RELEASES_2 })
                                              @RepositoryAttributes(allowsRedeployment = false, allowsDelete = false)
                                              @MavenRepository(repositoryId = MGRPT_RELEASES_GROUP) Repository releasesGroup,
                                              @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.in.releases.three:foo", versions = "1.2.3") Path a1,
                                              @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.in.releases.under2:group", versions = "1.2.3") Path a2,
                                              @MavenTestArtifact(repositoryId = MGRPT_RELEASES_2, id = "com.artifacts.in.releases.two:foo", versions = "1.2.4") Path a3,
                                              @MavenTestArtifact(repositoryId = MGRPT_RELEASES_2, id = "com.artifacts.in.releases.under2:group", versions = "1.2.4") Path a4)
            throws Exception
    {
        System.out.println("# Testing group includes with wildcard...");

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(releasesGroup.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(releasesGroup, (RepositoryPath) a3.normalize());

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNotNull(is);
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testGroupIncludesWildcardRuleAgainstNestedRepository(
            @MavenRepository(repositoryId = MGRPT_RELEASES_1) Repository releases1,
            @MavenRepository(repositoryId = MGRPT_RELEASES_2) Repository releases2,
            @Group(repositories = { MGRPT_RELEASES_1,
                                    MGRPT_RELEASES_2 })
            @RepositoryAttributes(allowsRedeployment = false, allowsDelete = false)
            @MavenRepository(repositoryId = MGRPT_RELEASES_GROUP) Repository releasesGroup,
            @Group(repositories = { MGRPT_RELEASES_GROUP,})
            @RepositoryAttributes(allowsRedeployment = false, allowsDelete = false)
            @MavenRepository(repositoryId = MGRPT_RELEASES_GROUP_WITH_NESTED_GROUP_LEVEL_1) Repository releasesGroupWithNestedGroup,
            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.in.releases.one:foo", versions = "1.2.3") Path a1,
            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.in.releases.under3:group", versions = "1.2.3") Path a2,
            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_2, id = "com.artifacts.in.releases.two:foo", versions = "1.2.4") Path a3,
            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_2, id = "com.artifacts.in.releases.under3:group", versions = "1.2.4") Path a4)
            throws Exception
    {
        System.out.println("# Testing group includes with wildcard against nested repositories...");

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(releasesGroupWithNestedGroup.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(releasesGroupWithNestedGroup, (RepositoryPath) a3.normalize());

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNotNull(is);
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testGroupAgainstNestedRepository(
            @MavenRepository(repositoryId = MGRPT_RELEASES_1) Repository releases1,
            @Group(repositories = { MGRPT_RELEASES_1 })
            @RepositoryAttributes(allowsRedeployment = false, allowsDelete = false)
            @MavenRepository(repositoryId = MGRPT_RELEASES_GROUP) Repository releasesGroup,
            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "org.carlspring.metadata.by.juan:juancho", versions = "1.2.64") Path a1)
            throws Exception
    {
        System.out.println("# Testing group includes with wildcard against nested repositories...");

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(releasesGroup.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(releasesGroup, (RepositoryPath) a1.normalize());

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNotNull(is);
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testGroupExcludes(@MavenRepository(repositoryId = MGRPT_RELEASES_1) Repository releases1,
                                  @MavenRepository(repositoryId = MGRPT_RELEASES_2) Repository releases2,
                                  @Group(repositories = { MGRPT_RELEASES_1,
                                                          MGRPT_RELEASES_2 },
                                         rules = { @Rule(repositories = { MGRPT_RELEASES_1 },
                                                         pattern = ".*(com|org)/artifacts.denied.*",
                                                         type = RoutingRuleTypeEnum.DENY)})
                                  @MavenRepository(repositoryId = MGRPT_RELEASES_GROUP) Repository releasesGroup,
                                  @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.accepted:foo", versions = "1.2.6") Path a1,
                                  @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.denied.by.wildcard:foo", versions = "1.2.6") Path a2,
                                  @MavenTestArtifact(repositoryId = MGRPT_RELEASES_2, id = "com.artifacts.denied.by.wildcard:foo", versions = "1.2.7") Path a3)
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

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void deniedRuleShouldBeValid(@MavenRepository(repositoryId = MGRPT_RELEASES_1) Repository releases1,
                                        @MavenRepository(repositoryId = MGRPT_RELEASES_2) Repository releases2,
                                        @Group(repositories = { MGRPT_RELEASES_1,
                                                                MGRPT_RELEASES_2 },
                                               rules = { @Rule(repositories = { MGRPT_RELEASES_2 },
                                                               pattern = ".*(com|org)/carlspring.metadata.*",
                                                               type = RoutingRuleTypeEnum.DENY)})
                                        @MavenRepository(repositoryId = MGRPT_RELEASES_GROUP) Repository releasesGroup,
                                        @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.accepted:foo", versions = "1.2.6") Path a1,
                                        @MavenTestArtifact(repositoryId = MGRPT_RELEASES_2, id = "org.carlspring.metadata.will.not.be:retrieved", versions = "1.2.64") Path a2)
            throws Exception
    {
        testDeny(releases1.getId(), releases2.getId(), releasesGroup, (RepositoryPath) a2.normalize());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void deniedRoutingRuleShouldWorkIfRepositoryAndStorageIsNotProvided(
            @MavenRepository(repositoryId = MGRPT_RELEASES_1) Repository releases1,
            @MavenRepository(repositoryId = MGRPT_RELEASES_2) Repository releases2,
            @Group(repositories = { MGRPT_RELEASES_1,
                                    MGRPT_RELEASES_2 })
            @MavenRepository(repositoryId = MGRPT_RELEASES_GROUP) Repository releasesGroup,
            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.accepted:foo", versions = "1.2.6") Path a1,
            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_2, id = "org.carlspring.metadata.will.not.be:retrieved", versions = "1.2.64") Path a2)
            throws Exception
    {
        // Rule cannot be created with annotation, because its 'repositories' attribute only allows not null repositories ids.
        createAndAddRoutingRule(STORAGE0,
                                releasesGroup.getId(),
                                Arrays.asList(new MutableRoutingRuleRepository(null, null)),
                                ".*(com|org)/carlspring.metadata.*",
                                RoutingRuleTypeEnum.DENY);

        testDeny(releases1.getId(), releases2.getId(), releasesGroup, (RepositoryPath) a2.normalize());
    }


    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void deniedRoutingRuleShouldWorkIfRepositoryIsEmpty(
            @MavenRepository(repositoryId = MGRPT_RELEASES_1) Repository releases1,
            @MavenRepository(repositoryId = MGRPT_RELEASES_2) Repository releases2,
            @Group(repositories = { MGRPT_RELEASES_1,
                                    MGRPT_RELEASES_2 },
                   rules = { @Rule(repositories = { StringUtils.EMPTY },
                                   pattern = ".*(com|org)/carlspring.metadata.*",
                                   type = RoutingRuleTypeEnum.DENY)})
            @MavenRepository(repositoryId = MGRPT_RELEASES_GROUP) Repository releasesGroup,
            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.accepted:foo", versions = "1.2.6") Path a1,
            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_2, id = "org.carlspring.metadata.will.not.be:retrieved", versions = "1.2.64") Path a2)
            throws Exception
    {
        testDeny(releases1.getId(), releases2.getId(), releasesGroup, (RepositoryPath) a2.normalize());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void deniedRoutingRuleShouldWorkForAllGroupsUnderTheSameStorage(
            @MavenRepository(repositoryId = MGRPT_RELEASES_1) Repository releases1,
            @MavenRepository(repositoryId = MGRPT_RELEASES_2) Repository releases2,
            @Group(repositories = { MGRPT_RELEASES_1,
                                    MGRPT_RELEASES_2 })
            @MavenRepository(repositoryId = MGRPT_RELEASES_GROUP) Repository releasesGroup,
            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.accepted:foo", versions = "1.2.6") Path a1,
            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_2, id = "org.carlspring.metadata.will.not.be:retrieved", versions = "1.2.64") Path a2)
            throws Exception
    {

        // Rule cannot be created with annotation, because is associated to a group, which cannot be null.
        createAndAddRoutingRule(STORAGE0,
                                null,
                                Arrays.asList(new MutableRoutingRuleRepository(STORAGE0, releases2.getId())),
                                ".*(com|org)/carlspring.metadata.*",
                                RoutingRuleTypeEnum.DENY);

        testDeny(releases1.getId(), releases2.getId(), releasesGroup, (RepositoryPath) a2.normalize());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void deniedRoutingRuleShouldWorkForAllGroups(
            @MavenRepository(repositoryId = MGRPT_RELEASES_1) Repository releases1,
            @MavenRepository(repositoryId = MGRPT_RELEASES_2) Repository releases2,
            @Group(repositories = { MGRPT_RELEASES_1,
                                    MGRPT_RELEASES_2 })
            @MavenRepository(repositoryId = MGRPT_RELEASES_GROUP) Repository releasesGroup,
            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.accepted:foo", versions = "1.2.6") Path a1,
            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_2, id = "org.carlspring.metadata.will.not.be:retrieved", versions = "1.2.64") Path a2)
            throws Exception
    {
        // Rule cannot be created with annotation, because groupStorageId and groupRepositoryId cannot be passed as null.
        createAndAddRoutingRule(null,
                                null,
                                Arrays.asList(new MutableRoutingRuleRepository(STORAGE0, releases2.getId())),
                                ".*(com|org)/carlspring.metadata.*",
                                RoutingRuleTypeEnum.DENY);

        testDeny(releases1.getId(), releases2.getId(), releasesGroup, (RepositoryPath) a2.normalize());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void deniedRoutingRuleShouldWorkForAllGroupsUnderTheSameRepositoryName(
            @MavenRepository(repositoryId = MGRPT_RELEASES_1) Repository releases1,
            @MavenRepository(repositoryId = MGRPT_RELEASES_2) Repository releases2,
            @Group(repositories = { MGRPT_RELEASES_1,
                                    MGRPT_RELEASES_2 })
            @MavenRepository(repositoryId = MGRPT_RELEASES_GROUP) Repository releasesGroup,
            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_1, id = "com.artifacts.accepted:foo", versions = "1.2.6") Path a1,
            @MavenTestArtifact(repositoryId = MGRPT_RELEASES_2, id = "org.carlspring.metadata.will.not.be:retrieved", versions = "1.2.64") Path a2)
            throws Exception
    {
        // Rule cannot be created with annotation, because groupStorageId cannot be passed as null.
        createAndAddRoutingRule(null,
                                releasesGroup.getId(),
                                Arrays.asList(new MutableRoutingRuleRepository(STORAGE0, releases2.getId())),
                                ".*(com|org)/carlspring.metadata.*",
                                RoutingRuleTypeEnum.DENY);

        testDeny(releases1.getId(), releases2.getId(), releasesGroup, (RepositoryPath) a2.normalize());
    }

    private void testDeny(String repositoryReleases1Name,
                          String repositoryReleases2Name,
                          Repository repositoryReleasesGroup,
                          RepositoryPath artifactPath)
            throws IOException
    {
        generateMavenMetadata(STORAGE0, repositoryReleases1Name);
        generateMavenMetadata(STORAGE0, repositoryReleases2Name);
        // Test data initialized.

        System.out.println("# Testing group excludes...");

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repositoryReleasesGroup.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(repositoryReleasesGroup, artifactPath);

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertNull(is);
        }
    }

}
