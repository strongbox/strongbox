package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryStatusEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.routing.MutableRoutingRule;
import org.carlspring.strongbox.storage.routing.MutableRoutingRuleRepository;
import org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum;
import org.carlspring.strongbox.testing.MavenMetadataServiceHelper;
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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
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
{
    private static final Logger logger = LoggerFactory.getLogger(MavenGroupRepositoryProviderTest.class);

    private static final String REPOSITORY_RELEASES_TGI_1 = "mgrpt-releases-tgi-1";

    private static final String REPOSITORY_RELEASES_TGI_2 = "mgrpt-releases-tgi-2";

    private static final String REPOSITORY_RELEASES_TGI_GROUP = "mgrpt-releases-tgi-group";

    private static final String REPOSITORY_RELEASES_MMFSBFFGPR_1 = "mgrpt-releases-mmfsbffgpr-1";

    private static final String REPOSITORY_RELEASES_MMFSBFFGPR_2 = "mgrpt-releases-mmfsbffgpr-2";

    private static final String REPOSITORY_RELEASES_MMFSBFFGPR_GROUP = "mgrpt-releases-mmfsbffgpr-group";

    private static final String REPOSITORY_RELEASES_TGIWOOSR_1 = "mgrpt-releases-tgiwoosr-1";

    private static final String REPOSITORY_RELEASES_TGIWOOSR_2 = "mgrpt-releases-tgiwoosr-2";

    private static final String REPOSITORY_RELEASES_TGIWOOSR_GROUP = "mgrpt-releases-tgiwoosr-group";

    private static final String REPOSITORY_RELEASES_TGIWR_1 = "mgrpt-releases-tgiwr-1";

    private static final String REPOSITORY_RELEASES_TGIWR_2 = "mgrpt-releases-tgiwr-2";

    private static final String REPOSITORY_RELEASES_TGIWR_GROUP = "mgrpt-releases-tgiwr-group";

    private static final String REPOSITORY_RELEASES_TGIWRANR_1 = "mgrpt-releases-tgiwranr-1";

    private static final String REPOSITORY_RELEASES_TGIWRANR_2 = "mgrpt-releases-tgiwranr-2";

    private static final String REPOSITORY_RELEASES_TGIWRANR_GROUP = "mgrpt-releases-tgiwranr-group";

    private static final String REPOSITORY_RELEASES_TGIWRANR_GROUP_WITH_NESTED_GROUP_1 = "mgrpt-releases-tgiwranr-group-with-nested-group-level-1";

    private static final String REPOSITORY_RELEASES_TGANR_1 = "mgrpt-releases-tganr-1";

    private static final String REPOSITORY_RELEASES_TGANR_GROUP = "mgrpt-releases-tganr-group";

    private static final String REPOSITORY_RELEASES_TGE_1 = "mgrpt-releases-tge-1";

    private static final String REPOSITORY_RELEASES_TGE_2 = "mgrpt-releases-tge-2";

    private static final String REPOSITORY_RELEASES_TGE_GROUP = "mgrpt-releases-tge-group";

    private static final String REPOSITORY_RELEASES_DRRSBV_1 = "mgrpt-releases-drrsbv-1";

    private static final String REPOSITORY_RELEASES_DRRSBV_2 = "mgrpt-releases-drrsbv-2";

    private static final String REPOSITORY_RELEASES_DRRSBV_GROUP = "mgrpt-releases-drrsbv-group";

    private static final String REPOSITORY_RELEASES_DRRSWIRASINP_1 = "mgrpt-releases-drrswirasinp-1";

    private static final String REPOSITORY_RELEASES_DRRSWIRASINP_2 = "mgrpt-releases-drrswirasinp-2";

    private static final String REPOSITORY_RELEASES_DRRSWIRASINP_GROUP = "mgrpt-releases-drrswirasinp-group";

    private static final String REPOSITORY_RELEASES_DRRSWIRINP_1 = "mgrpt-releases-drrswirinp-1";

    private static final String REPOSITORY_RELEASES_DRRSWIRINP_2 = "mgrpt-releases-drrswirinp-2";

    private static final String REPOSITORY_RELEASES_DRRSWIRINP_GROUP = "mgrpt-releases-drrswirinp-group";

    private static final String REPOSITORY_RELEASES_DRRSWIRIE_1 = "mgrpt-releases-drrswirie-1";

    private static final String REPOSITORY_RELEASES_DRRSWIRIE_2 = "mgrpt-releases-drrswirie-2";

    private static final String REPOSITORY_RELEASES_DRRSWIRIE_GROUP = "mgrpt-releases-drrswirie-group";

    private static final String REPOSITORY_RELEASES_DRRSWFAGUTSS_1 = "mgrpt-releases-drrswfagutss-1";

    private static final String REPOSITORY_RELEASES_DRRSWFAGUTSS_2 = "mgrpt-releases-drrswfagutss-2";

    private static final String REPOSITORY_RELEASES_DRRSWFAGUTSS_GROUP = "mgrpt-releases-drrswfagutss-group";

    private static final String REPOSITORY_RELEASES_DRRSWFAG_1 = "mgrpt-releases-drrswfag-1";

    private static final String REPOSITORY_RELEASES_DRRSWFAG_2 = "mgrpt-releases-drrswfag-2";

    private static final String REPOSITORY_RELEASES_DRRSWFAG_GROUP = "mgrpt-releases-drrswfag-group";

    private static final String REPOSITORY_RELEASES_DRRSWFAGUTSRN_1 = "mgrpt-releases-drrswfagutsrn-1";

    private static final String REPOSITORY_RELEASES_DRRSWFAGUTSRN_2 = "mgrpt-releases-drrswfagutsrn-2";

    private static final String REPOSITORY_RELEASES_DRRSWFAGUTSRN_GROUP = "mgrpt-releases-drrswfagutsrn-group";

    @Inject
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private ConfigurationManagementService configurationManagementService;
    
    @Inject
    private RepositoryPathResolver repositoryPathResolver;
    
    @Inject
    private MavenMetadataServiceHelper mavenMetadataServiceHelper;

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testGroupIncludes(@MavenRepository(repositoryId = REPOSITORY_RELEASES_TGI_1) Repository releases1,
                                  @MavenRepository(repositoryId = REPOSITORY_RELEASES_TGI_2) Repository releases2,
                                  @Group({ REPOSITORY_RELEASES_TGI_1,
                                           REPOSITORY_RELEASES_TGI_2 })
                                  @MavenRepository(repositoryId = REPOSITORY_RELEASES_TGI_GROUP) Repository releasesGroup,
                                  @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGI_1, id = "com.artifacts.in.releases.one:foo", versions = "1.2.3") Path a1,
                                  @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGI_1, id = "com.artifacts.in.releases.under:group", versions = "1.2.3") Path a2,
                                  @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGI_2, id = "com.artifacts.in.releases.four:foo", versions = "1.2.4") Path a3,
                                  @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGI_2, id = "com.artifacts.in.releases.under:group", versions = "1.2.4") Path a4)
            throws Exception
    {
        logger.debug("# Testing group includes...");

        // Test data initialized.
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(releasesGroup.getType());

        RepositoryPath resolvedPath1 = (RepositoryPath) a1.normalize();

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath1);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertThat(is).isNotNull();
        }

        RepositoryPath resolvedPath2 = (RepositoryPath) a3.normalize();
        repositoryPath = repositoryProvider.fetchPath(resolvedPath2);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {

            assertThat(is).isNotNull();
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void mavenMetadataFileShouldBeFetchedFromGroupPathRepository(@MavenRepository(repositoryId = REPOSITORY_RELEASES_MMFSBFFGPR_1) Repository releases1,
                                                                        @MavenRepository(repositoryId = REPOSITORY_RELEASES_MMFSBFFGPR_2) Repository releases2,
                                                                        @Group({ REPOSITORY_RELEASES_MMFSBFFGPR_1,
                                                                                 REPOSITORY_RELEASES_MMFSBFFGPR_2 })
                                                                        @MavenRepository(repositoryId = REPOSITORY_RELEASES_MMFSBFFGPR_GROUP) Repository releasesGroup,
                                                                        @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_MMFSBFFGPR_1, id = "com.artifacts.in.releases.one:foo", versions = "1.2.3") Path a1,
                                                                        @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_MMFSBFFGPR_1, id = "com.artifacts.in.releases.under123:group", versions = "1.2.3") Path a2,
                                                                        @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_MMFSBFFGPR_2, id = "com.artifacts.in.releases.under123:group", versions = "1.2.4") Path a3)
            throws Exception
    {
        mavenMetadataServiceHelper.generateMavenMetadata(releases1);
        mavenMetadataServiceHelper.generateMavenMetadata(releases2);

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(RepositoryTypeEnum.GROUP.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(releasesGroup,
                                                                     "com/artifacts/in/releases/under123/group/maven-metadata.xml");

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        InputStream is = repositoryProvider.getInputStream(repositoryPath);

        assertThat(is).isNotNull();

        Metadata metadata = artifactMetadataService.getMetadata(is);

        assertThat(metadata.getVersioning().getVersions()).hasSize(2);
        assertThat(metadata.getVersioning().getVersions()).containsExactly("1.2.3", "1.2.4");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testGroupIncludesWithOutOfServiceRepository(@MavenRepository(repositoryId = REPOSITORY_RELEASES_TGIWOOSR_1)
                                                            Repository releases1,
                                                            @MavenRepository(repositoryId = REPOSITORY_RELEASES_TGIWOOSR_2)
                                                            @RepositoryAttributes(status = RepositoryStatusEnum.OUT_OF_SERVICE)
                                                            Repository releases2,
                                                            @Group(repositories = { REPOSITORY_RELEASES_TGIWOOSR_1,
                                                                                    REPOSITORY_RELEASES_TGIWOOSR_2 },
                                                                   rules = { @Rule(repositories = { REPOSITORY_RELEASES_TGIWOOSR_1,
                                                                                                    REPOSITORY_RELEASES_TGIWOOSR_2 },
                                                                                   pattern = ".*(com|org)/artifacts.in.releases.*"),
                                                                             @Rule(repositories = { REPOSITORY_RELEASES_TGIWOOSR_1 },
                                                                                   pattern = ".*(com|org)/artifacts.in.*",
                                                                                   type = RoutingRuleTypeEnum.DENY)})
                                                            @MavenRepository(repositoryId = REPOSITORY_RELEASES_TGIWOOSR_GROUP)
                                                            Repository releasesGroup,
                                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGIWOOSR_1,
                                                                               id = "com.artifacts.in.releases.one:foo",
                                                                               versions = "1.2.3")
                                                            Path a1,
                                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGIWOOSR_2,
                                                                               id = "com.artifacts.in.releases.two:foo",
                                                                               versions = "1.2.4")
                                                            Path a2)
            throws Exception
    {


        // Test data initialized.

        logger.debug("# Testing group includes with out of service repository...");

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(releasesGroup.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(releasesGroup, (RepositoryPath) a2.normalize());

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertThat(is).isNull();
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testGroupIncludesWildcardRule(@MavenRepository(repositoryId = REPOSITORY_RELEASES_TGIWR_1) Repository releases1,
                                              @MavenRepository(repositoryId = REPOSITORY_RELEASES_TGIWR_2) Repository releases2,
                                              @Group(repositories = { REPOSITORY_RELEASES_TGIWR_1,
                                                                      REPOSITORY_RELEASES_TGIWR_2 })
                                              @MavenRepository(repositoryId = REPOSITORY_RELEASES_TGIWR_GROUP) Repository releasesGroup,
                                              @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGIWR_1, id = "com.artifacts.in.releases.three:foo", versions = "1.2.3") Path a1,
                                              @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGIWR_1, id = "com.artifacts.in.releases.under2:group", versions = "1.2.3") Path a2,
                                              @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGIWR_2, id = "com.artifacts.in.releases.two:foo", versions = "1.2.4") Path a3,
                                              @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGIWR_2, id = "com.artifacts.in.releases.under2:group", versions = "1.2.4") Path a4)
            throws Exception
    {
        logger.debug("# Testing group includes with wildcard...");

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(releasesGroup.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(releasesGroup, (RepositoryPath) a3.normalize());

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertThat(is).isNotNull();
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testGroupIncludesWildcardRuleAgainstNestedRepository(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_TGIWRANR_1) Repository releases1,
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_TGIWRANR_2) Repository releases2,
            @Group(repositories = { REPOSITORY_RELEASES_TGIWRANR_1,
                                    REPOSITORY_RELEASES_TGIWRANR_2 })
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_TGIWRANR_GROUP) Repository releasesGroup,
            @Group(repositories = { REPOSITORY_RELEASES_TGIWRANR_GROUP,})
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_TGIWRANR_GROUP_WITH_NESTED_GROUP_1) Repository releasesGroupWithNestedGroup,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGIWRANR_1, id = "com.artifacts.in.releases.one:foo", versions = "1.2.3") Path a1,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGIWRANR_1, id = "com.artifacts.in.releases.under3:group", versions = "1.2.3") Path a2,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGIWRANR_2, id = "com.artifacts.in.releases.two:foo", versions = "1.2.4") Path a3,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGIWRANR_2, id = "com.artifacts.in.releases.under3:group", versions = "1.2.4") Path a4)
            throws Exception
    {
        logger.debug("# Testing group includes with wildcard against nested repositories...");

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(releasesGroupWithNestedGroup.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(releasesGroupWithNestedGroup, (RepositoryPath) a3.normalize());

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertThat(is).isNotNull();
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testGroupAgainstNestedRepository(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_TGANR_1) Repository releases1,
            @Group(repositories = { REPOSITORY_RELEASES_TGANR_1 })
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_TGANR_GROUP) Repository releasesGroup,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGANR_1, id = "com.artifacts.in.releases.one:foo", versions = "1.2.3") Path a1)
            throws Exception
    {
        logger.debug("# Testing group includes with wildcard against nested repositories...");

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(releasesGroup.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(releasesGroup, (RepositoryPath) a1.normalize());

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertThat(is).isNotNull();
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testGroupExcludes(@MavenRepository(repositoryId = REPOSITORY_RELEASES_TGE_1) Repository releases1,
                                  @MavenRepository(repositoryId = REPOSITORY_RELEASES_TGE_2) Repository releases2,
                                  @Group(repositories = { REPOSITORY_RELEASES_TGE_1,
                                                          REPOSITORY_RELEASES_TGE_2 },
                                         rules = { @Rule(repositories = { REPOSITORY_RELEASES_TGE_1 },
                                                         pattern = ".*(com|org)/artifacts.denied.*",
                                                         type = RoutingRuleTypeEnum.DENY)})
                                  @MavenRepository(repositoryId = REPOSITORY_RELEASES_TGE_GROUP) Repository releasesGroup,
                                  @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGE_1, id = "com.artifacts.accepted:foo", versions = "1.2.6") Path a1,
                                  @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGE_1, id = "com.artifacts.denied.by.wildcard:foo", versions = "1.2.6") Path a2,
                                  @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_TGE_2, id = "com.artifacts.denied.by.wildcard:foo", versions = "1.2.7") Path a3)
            throws Exception
    {
        logger.debug("# Testing group excludes...");
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(releasesGroup.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(releasesGroup, (RepositoryPath) a1.normalize());
        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertThat(is).isNotNull();
        }

        resolvedPath = repositoryPathResolver.resolve(releasesGroup, (RepositoryPath) a2.normalize());
        repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertThat(is).isNull();
        }

        resolvedPath = repositoryPathResolver.resolve(releasesGroup, (RepositoryPath) a3.normalize());
        repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertThat(is).isNotNull();
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void deniedRoutingRuleShouldBeValid(@MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSBV_1) Repository releases1,
                                               @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSBV_2) Repository releases2,
                                               @Group(repositories = { REPOSITORY_RELEASES_DRRSBV_1,
                                                                       REPOSITORY_RELEASES_DRRSBV_2 },
                                                       rules = { @Rule(repositories = { REPOSITORY_RELEASES_DRRSBV_2 },
                                                                       pattern = ".*(com|org)/carlspring.metadata.*",
                                                                       type = RoutingRuleTypeEnum.DENY)})
                                               @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSBV_GROUP) Repository releasesGroup,
                                               @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_DRRSBV_1, id = "com.artifacts.accepted:foo", versions = "1.2.6") Path a1,
                                               @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_DRRSBV_2, id = "org.carlspring.metadata.will.not.be:retrieved", versions = "1.2.64") Path a2)
            throws Exception
    {
        testDeny(releases1,
                 releases2,
                 releasesGroup,
                 (RepositoryPath) a2.normalize());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void deniedRoutingRuleShouldWorkIfRepositoryAndStorageIsNotProvided(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWIRASINP_1) Repository releases1,
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWIRASINP_2) Repository releases2,
            @Group(repositories = { REPOSITORY_RELEASES_DRRSWIRASINP_1,
                                    REPOSITORY_RELEASES_DRRSWIRASINP_2 })
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWIRASINP_GROUP) Repository releasesGroup,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_DRRSWIRASINP_1, id = "com.artifacts.accepted:foo", versions = "1.2.6") Path a1,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_DRRSWIRASINP_2, id = "org.carlspring.metadata.will.not.be:retrieved", versions = "1.2.64") Path a2)
            throws Exception
    {
        // Rule cannot be created with annotation, because its 'repositories' attribute only allows not null repositories ids.
        createAndAddRoutingRule(releasesGroup.getStorage().getId(),
                                releasesGroup.getId(),
                                Arrays.asList(new MutableRoutingRuleRepository(null, null)),
                                ".*(com|org)/carlspring.metadata.*",
                                RoutingRuleTypeEnum.DENY);

        testDeny(releases1,
                 releases2,
                 releasesGroup,
                 (RepositoryPath) a2.normalize());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void deniedRoutingRuleShouldWorkIfRepositoryIsNotProvided(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWIRINP_1) Repository releases1,
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWIRINP_2) Repository releases2,
            @Group(repositories = { REPOSITORY_RELEASES_DRRSWIRINP_1,
                                    REPOSITORY_RELEASES_DRRSWIRINP_2 })
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWIRINP_GROUP) Repository releasesGroup,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_DRRSWIRINP_1, id = "com.artifacts.accepted:foo", versions = "1.2.6") Path a1,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_DRRSWIRINP_2, id = "org.carlspring.metadata.will.not.be:retrieved", versions = "1.2.64") Path a2)
            throws Exception
    {
        // Rule cannot be created with annotation, because its 'repositories' attribute only allows not null repositories ids.
        createAndAddRoutingRule(releasesGroup.getStorage().getId(),
                                releasesGroup.getId(),
                                Arrays.asList(new MutableRoutingRuleRepository(releasesGroup.getStorage().getId(),
                                                                               null)),
                                ".*(com|org)/carlspring.metadata.*",
                                RoutingRuleTypeEnum.DENY);

        testDeny(releases1,
                 releases2,
                 releasesGroup,
                 (RepositoryPath) a2.normalize());
    }


    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void deniedRoutingRuleShouldWorkIfRepositoryIsEmpty(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWIRIE_1) Repository releases1,
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWIRIE_2) Repository releases2,
            @Group(repositories = { REPOSITORY_RELEASES_DRRSWIRIE_1,
                                    REPOSITORY_RELEASES_DRRSWIRIE_2 },
                   rules = { @Rule(repositories = { StringUtils.EMPTY },
                                   pattern = ".*(com|org)/carlspring.metadata.*",
                                   type = RoutingRuleTypeEnum.DENY)})
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWIRIE_GROUP) Repository releasesGroup,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_DRRSWIRIE_1, id = "com.artifacts.accepted:foo", versions = "1.2.6") Path a1,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_DRRSWIRIE_2, id = "org.carlspring.metadata.will.not.be:retrieved", versions = "1.2.64") Path a2)
            throws Exception
    {
        testDeny(releases1,
                 releases2,
                 releasesGroup,
                 (RepositoryPath) a2.normalize());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void deniedRoutingRuleShouldWorkForAllGroupsUnderTheSameStorage(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWFAGUTSS_1) Repository releases1,
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWFAGUTSS_2) Repository releases2,
            @Group(repositories = { REPOSITORY_RELEASES_DRRSWFAGUTSS_1,
                                    REPOSITORY_RELEASES_DRRSWFAGUTSS_2 })
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWFAGUTSS_GROUP) Repository releasesGroup,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_DRRSWFAGUTSS_1, id = "com.artifacts.accepted:foo", versions = "1.2.6") Path a1,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_DRRSWFAGUTSS_2, id = "org.carlspring.metadata.will.not.be:retrieved", versions = "1.2.64") Path a2)
            throws Exception
    {
        // Rule cannot be created with annotation, because is associated to a group, which cannot be null.
        createAndAddRoutingRule(releasesGroup.getStorage().getId(),
                                null,
                                Arrays.asList(new MutableRoutingRuleRepository(releases2.getStorage().getId(),
                                                                               releases2.getId())),
                                ".*(com|org)/carlspring.metadata.*",
                                RoutingRuleTypeEnum.DENY);

        testDeny(releases1,
                 releases2,
                 releasesGroup,
                 (RepositoryPath) a2.normalize());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void deniedRoutingRuleShouldWorkForAllGroups(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWFAG_1) Repository releases1,
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWFAG_2) Repository releases2,
            @Group(repositories = { REPOSITORY_RELEASES_DRRSWFAG_1,
                                    REPOSITORY_RELEASES_DRRSWFAG_2 })
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWFAG_GROUP) Repository releasesGroup,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_DRRSWFAG_1, id = "com.artifacts.accepted:foo", versions = "1.2.6") Path a1,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_DRRSWFAG_2, id = "org.carlspring.metadata.will.not.be:retrieved", versions = "1.2.64") Path a2)
            throws Exception
    {
        // Rule cannot be created with annotation, because groupStorageId and groupRepositoryId cannot be passed as null.
        createAndAddRoutingRule(null,
                                null,
                                Arrays.asList(new MutableRoutingRuleRepository(releases2.getStorage().getId(),
                                                                               releases2.getId())),
                                ".*(com|org)/carlspring.metadata.*",
                                RoutingRuleTypeEnum.DENY);

        testDeny(releases1,
                 releases2,
                 releasesGroup,
                 (RepositoryPath) a2.normalize());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void deniedRoutingRuleShouldWorkForAllGroupsUnderTheSameRepositoryName(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWFAGUTSRN_1) Repository releases1,
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWFAGUTSRN_2) Repository releases2,
            @Group(repositories = { REPOSITORY_RELEASES_DRRSWFAGUTSRN_1,
                                    REPOSITORY_RELEASES_DRRSWFAGUTSRN_2 })
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_DRRSWFAGUTSRN_GROUP) Repository releasesGroup,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_DRRSWFAGUTSRN_1, id = "com.artifacts.accepted:foo", versions = "1.2.6") Path a1,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_DRRSWFAGUTSRN_2, id = "org.carlspring.metadata.will.not.be:retrieved", versions = "1.2.64") Path a2)
            throws Exception
    {
        // Rule cannot be created with annotation, because groupStorageId cannot be passed as null.
        createAndAddRoutingRule(null,
                                releasesGroup.getId(),
                                Arrays.asList(new MutableRoutingRuleRepository(releases2.getStorage().getId(),
                                                                               releases2.getId())),
                                ".*(com|org)/carlspring.metadata.*",
                                RoutingRuleTypeEnum.DENY);

        testDeny(releases1,
                 releases2,
                 releasesGroup,
                 (RepositoryPath) a2.normalize());
    }

    private void testDeny(Repository repository1,
                          Repository repository2,
                          Repository repositoryReleasesGroup,
                          RepositoryPath artifactPath)
            throws IOException
    {
        mavenMetadataServiceHelper.generateMavenMetadata(repository1);
        mavenMetadataServiceHelper.generateMavenMetadata(repository2);
        // Test data initialized.

        logger.debug("# Testing group excludes...");

        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(
                repositoryReleasesGroup.getType());

        RepositoryPath resolvedPath = repositoryPathResolver.resolve(repositoryReleasesGroup, artifactPath);

        Path repositoryPath = repositoryProvider.fetchPath(resolvedPath);
        try (InputStream is = repositoryProvider.getInputStream(repositoryPath))
        {
            assertThat(is).isNull();
        }
    }

    private void createAndAddRoutingRule(String groupStorageId,
                                         String groupRepositoryId,
                                         List<MutableRoutingRuleRepository> repositories,
                                         String rulePattern,
                                         RoutingRuleTypeEnum type)
            throws IOException
    {
        MutableRoutingRule routingRule = MutableRoutingRule.create(groupStorageId, groupRepositoryId,
                                                                   repositories, rulePattern, type);
        configurationManagementService.addRoutingRule(routingRule);
    }

}
