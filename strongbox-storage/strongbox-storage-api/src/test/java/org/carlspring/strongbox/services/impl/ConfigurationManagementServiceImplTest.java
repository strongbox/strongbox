package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.artifact.coordinates.RawArtifactCoordinates;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.repository.HttpConnectionPool;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.MutableRoutingRule;
import org.carlspring.strongbox.storage.routing.MutableRoutingRuleRepository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum;
import org.carlspring.strongbox.testing.NullLayoutProvider;
import org.carlspring.strongbox.testing.repository.NullRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = StorageApiTestConfig.class)
public class ConfigurationManagementServiceImplTest
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagementServiceImplTest.class);

    private static final String RULE_PATTERN = "\\*.org.test";

    private static final String STORAGE0 = "storage0";
    
    private static final String REPOSITORY_RELEASES_1 = "cmsi-releases-1";

    private static final String REPOSITORY_RELEASES_2 = "cmsi-releases-2";

    private static final String REPOSITORY_GROUP_1 = "csmi-group-1";

    private static final String REPOSITORY_GROUP_2 = "csmi-group-2";

    private static final String STORAGE_COMMON_PROXIES = "storage-common-proxies";

    private static final String REPOSITORY_GROUP_COMMON_PROXIES = "group-common-proxies";

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Test
    public void groupRepositoriesShouldBeSortedAsExpected()
    {
        Repository repository = configurationManagementService.getConfiguration().getRepository(
                STORAGE_COMMON_PROXIES,
                REPOSITORY_GROUP_COMMON_PROXIES);

        Iterator<String> iterator = repository.getGroupRepositories().iterator();
        assertThat(iterator.next()).isEqualTo("carlspring");
        assertThat(iterator.next()).isEqualTo("maven-central");
        assertThat(iterator.next()).isEqualTo("apache-snapshots");
        assertThat(iterator.next()).isEqualTo("jboss-public-releases");
    }

    @Test
    public void additionOfTheSameGroupRepositoryShouldNotAffectGroupRepositoriesList()
            throws IOException
    {
        configurationManagementService.addRepositoryToGroup(STORAGE_COMMON_PROXIES,
                                                            REPOSITORY_GROUP_COMMON_PROXIES,
                                                            "maven-central");

        Repository repository = configurationManagementService.getConfiguration()
                                                              .getRepository(STORAGE_COMMON_PROXIES,
                                                                             REPOSITORY_GROUP_COMMON_PROXIES);

        assertThat(repository.getGroupRepositories()).hasSize(4);
        Iterator<String> iterator = repository.getGroupRepositories().iterator();
        assertThat(iterator.next()).isEqualTo("carlspring");
        assertThat(iterator.next()).isEqualTo("maven-central");
        assertThat(iterator.next()).isEqualTo("apache-snapshots");
        assertThat(iterator.next()).isEqualTo("jboss-public-releases");
    }

    @Test
    public void multipleAdditionOfTheSameRepositoryShouldNotAffectGroup()
            throws IOException
    {
        for (int i = 1; i <= 3; i++)
        {
            configurationManagementService.addRepositoryToGroup(STORAGE_COMMON_PROXIES,
                                                                REPOSITORY_GROUP_COMMON_PROXIES,
                                                                "maven-central");
        }

        Repository repository = configurationManagementService.getConfiguration()
                                                              .getRepository(STORAGE_COMMON_PROXIES,
                                                                             REPOSITORY_GROUP_COMMON_PROXIES);

        assertThat(repository.getGroupRepositories()).hasSize(4);
        Iterator<String> iterator = repository.getGroupRepositories().iterator();
        assertThat(iterator.next()).isEqualTo("carlspring");
        assertThat(iterator.next()).isEqualTo("maven-central");
        assertThat(iterator.next()).isEqualTo("apache-snapshots");
        assertThat(iterator.next()).isEqualTo("jboss-public-releases");
    }

    @Test
    public void testGetRepositories()
    {
        List<Repository> repositories = configurationManagementService.getConfiguration().getRepositories();

        assertThat(repositories).isNotEmpty();

        logger.debug("Repositories:");

        for (Repository repository : repositories)
        {
            logger.debug(" - {}", repository.getId());
        }
    }

    @Test
    public void testGetGroupRepositories()
    {
        List<Repository> groupRepositories = configurationManagementService.getConfiguration().getGroupRepositories();

        assertThat(groupRepositories).isNotEmpty();

        logger.debug("Group repositories:");

        for (Repository repository : groupRepositories)
        {
            logger.debug(" - {}", repository.getId());
        }
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testGetGroupRepositoriesContainingRepository(@NullRepository(repositoryId = REPOSITORY_RELEASES_1)
                                                             Repository releases1,
                                                             @Group(repositories = REPOSITORY_RELEASES_1)
                                                             @NullRepository(repositoryId = REPOSITORY_GROUP_1)
                                                             Repository releasesGroup)
    {
        final String storageId = releases1.getStorage().getId();
        final String releases1Id = releases1.getId();

        List<Repository> groups = configurationManagementService.getConfiguration()
                                                                .getGroupRepositoriesContaining(storageId,
                                                                                                releases1Id);

        assertThat(groups).isNotEmpty();

        logger.debug("Group repositories containing \"{}\" repository:", releases1Id);

        for (Repository repository : groups)
        {
            logger.debug(" - {}", repository.getId());
        }
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testRemoveRepositoryFromAssociatedGroups(@NullRepository(repositoryId = REPOSITORY_RELEASES_1) 
                                                         Repository releases1,
                                                         @Group(repositories = REPOSITORY_RELEASES_1)
                                                         @NullRepository(repositoryId = REPOSITORY_GROUP_1) 
                                                         Repository releasesGroup1,
                                                         @Group(repositories = REPOSITORY_RELEASES_1)
                                                         @NullRepository(repositoryId = REPOSITORY_GROUP_2) 
                                                         Repository releasesGroup2) throws IOException
    {
        final String storageId = releases1.getStorage().getId();
        final String releases1Id = releases1.getId();

        assertThat(configurationManagementService.getConfiguration()
                                                 .getGroupRepositoriesContaining(storageId, releases1Id))
                .as("Failed to add repository to group!")
                .hasSize(2);

        configurationManagementService.removeRepositoryFromAssociatedGroups(storageId,
                                                                            releases1Id);

        assertThat(configurationManagementService.getConfiguration()
                                                 .getGroupRepositoriesContaining(storageId, releases1Id))
                .as("Failed to remove repository from all associated groups!")
                .isEmpty();
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testSetProxyRepositoryMaxConnections(@NullRepository(repositoryId = REPOSITORY_RELEASES_2)
                                                     Repository releases2) throws IOException
    {
        final String storageId = releases2.getStorage().getId();
        final String releases2Id = releases2.getId();

        configurationManagementService.setProxyRepositoryMaxConnections(storageId,
                                                                        releases2Id,
                                                                        10);

        HttpConnectionPool pool = configurationManagementService.getConfiguration()
                                                                .getHttpConnectionPoolConfiguration(storageId,
                                                                                                    releases2Id);

        assertThat(pool).isNotNull();
        assertThat(pool.getAllocatedConnections()).isEqualTo(10);
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void shouldAddEditAndRemoveRoutingRule(@NullRepository(repositoryId = REPOSITORY_RELEASES_1)
                                                  Repository releases1,
                                                  @Group(repositories = REPOSITORY_RELEASES_1)
                                                  @NullRepository(repositoryId = REPOSITORY_GROUP_1)
                                                  Repository releasesGroup)
            throws IOException
    {
        final MutableRoutingRule routingRule = createRoutingRule(RoutingRuleTypeEnum.ACCEPT);
        String groupRepositoryId = routingRule.getGroupRepositoryId();
        String storageId = routingRule.getStorageId();

        final boolean added = configurationManagementService.addRoutingRule(routingRule);
        assertThat(added).isTrue();

        Configuration configuration = configurationManagementService.getConfiguration();
        List<RoutingRule> routingRulesMatching = configuration.getRoutingRules()
                                                              .getRules()
                                                              .stream()
                                                              .filter(a -> groupRepositoryId.equals(a.getGroupRepositoryId()) &&
                                                                           storageId.equals(a.getStorageId()))
                                                              .collect(Collectors.toList());

        assertThat(routingRulesMatching).hasSize(1);
        RoutingRule dbRoutingRule = routingRulesMatching.get(0);
        assertThat(dbRoutingRule.getType()).isEqualTo(RoutingRuleTypeEnum.ACCEPT);

        routingRule.setType(RoutingRuleTypeEnum.DENY.getType());
        boolean updated = configurationManagementService.updateRoutingRule(dbRoutingRule.getUuid(), routingRule);
        assertThat(updated).isTrue();

        configuration = configurationManagementService.getConfiguration();
        routingRulesMatching = configuration.getRoutingRules()
                                            .getRules()
                                            .stream()
                                            .filter(a -> groupRepositoryId.equals(a.getGroupRepositoryId()) &&
                                                         storageId.equals(a.getStorageId()))
                                            .collect(Collectors.toList());

        assertThat(routingRulesMatching).hasSize(1);
        RoutingRule routingRule1 = routingRulesMatching.get(0);
        assertThat(routingRule1.getType()).isEqualTo(RoutingRuleTypeEnum.DENY);

        boolean removed = configurationManagementService.removeRoutingRule(dbRoutingRule.getUuid());
        assertThat(removed).isTrue();

        configuration = configurationManagementService.getConfiguration();
        routingRulesMatching = configuration.getRoutingRules()
                                            .getRules()
                                            .stream()
                                            .filter(a -> groupRepositoryId.equals(a.getGroupRepositoryId()) &&
                                                         storageId.equals(a.getStorageId()))
                                            .collect(Collectors.toList());

        assertThat(routingRulesMatching).isEmpty();
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testCanGetRepositoriesWithStorageAndLayout(@NullRepository(repositoryId = REPOSITORY_RELEASES_1)
                                                           Repository releases1,
                                                           @NullRepository(repositoryId = REPOSITORY_RELEASES_2) 
                                                           Repository releases2,
                                                           @Group(repositories = REPOSITORY_RELEASES_1)
                                                           @NullRepository(repositoryId = REPOSITORY_GROUP_1) 
                                                           Repository releasesGroup1,
                                                           @Group(repositories = REPOSITORY_RELEASES_1)
                                                           @NullRepository(repositoryId = REPOSITORY_GROUP_2) 
                                                           Repository releasesGroup2)
    {
        List<Repository> repositories = configurationManagementService.getConfiguration()
                                                                      .getRepositoriesWithLayout(STORAGE0,
                                                                                                 NullLayoutProvider.ALIAS);

        assertThat(repositories).isNotEmpty();

        repositories.forEach(repository -> {
            assertThat(repository.getLayout()).isEqualTo(NullLayoutProvider.ALIAS);
            assertThat(repository.getStorage().getId()).isEqualTo(STORAGE0);
        });
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testCanGetRepositoriesWithStorageAndLayoutNotExistedStorage(@NullRepository(repositoryId = REPOSITORY_RELEASES_1)
                                                                            Repository releases1,
                                                                            @NullRepository(repositoryId = REPOSITORY_RELEASES_2) 
                                                                            Repository releases2,
                                                                            @Group(repositories = REPOSITORY_RELEASES_1)
                                                                            @NullRepository(repositoryId = REPOSITORY_GROUP_1) 
                                                                            Repository releasesGroup1,
                                                                            @Group(repositories = REPOSITORY_RELEASES_1)
                                                                            @NullRepository(repositoryId = REPOSITORY_GROUP_2) 
                                                                            Repository releasesGroup2)
    {
        List<Repository> repositories = configurationManagementService.getConfiguration()
                                                                      .getRepositoriesWithLayout("notExistingStorage",
                                                                                                 RawArtifactCoordinates.LAYOUT_NAME);

        assertThat(repositories).isEmpty();
    }

    private MutableRoutingRule createRoutingRule(RoutingRuleTypeEnum type)
    {
        MutableRoutingRule routingRule = new MutableRoutingRule();
        routingRule.setPattern(RULE_PATTERN);
        routingRule.setRepositories(Stream.of(REPOSITORY_RELEASES_1).map(
                r -> new MutableRoutingRuleRepository(STORAGE0, r)).collect(
                Collectors.toList()));
        routingRule.setType(type.getType());
        routingRule.setGroupRepositoryId(REPOSITORY_GROUP_1);
        routingRule.setStorageId(STORAGE0);
        return routingRule;
    }

}
