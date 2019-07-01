package org.carlspring.strongbox.services.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.artifact.coordinates.NullArtifactCoordinates;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.StorageData;
import org.carlspring.strongbox.storage.repository.HttpConnectionPool;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.storage.routing.MutableRoutingRule;
import org.carlspring.strongbox.storage.routing.MutableRoutingRuleRepository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum;
import org.carlspring.strongbox.testing.repository.NullRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author mtodorov
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = StorageApiTestConfig.class)
@Execution(CONCURRENT)
public class ConfigurationManagementServiceImplTest
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagementServiceImplTest.class);

    private static final String RULE_PATTERN = "\\*.org.test";

    private static final String STORAGE0 = "storage0";
    
    private static final String REPOSITORY_RELEASES_1 = "cmsi-releases-1";

    private static final String REPOSITORY_RELEASES_2 = "cmsi-releases-2";

    private static final String REPOSITORY_GROUP_1 = "csmi-group-1";

    private static final String REPOSITORY_GROUP_2 = "csmi-group-2";

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Test
    public void groupRepositoriesShouldBeSortedAsExpected()
    {
        RepositoryData repository = configurationManagementService.getConfiguration().getRepository(
                "storage-common-proxies",
                "group-common-proxies");

        Iterator<String> iterator = repository.getGroupRepositories().iterator();
        assertThat(iterator.next(), CoreMatchers.equalTo("carlspring"));
        assertThat(iterator.next(), CoreMatchers.equalTo("maven-central"));
        assertThat(iterator.next(), CoreMatchers.equalTo("apache-snapshots"));
        assertThat(iterator.next(), CoreMatchers.equalTo("jboss-public-releases"));
    }

    @Test
    public void additionOfTheSameGroupRepositoryShouldNotAffectGroupRepositoriesList() throws IOException
    {
        configurationManagementService.addRepositoryToGroup("storage-common-proxies",
                                                            "group-common-proxies",
                                                            "maven-central");

        RepositoryData repository = configurationManagementService.getConfiguration()
                                                              .getRepository("storage-common-proxies",
                                                                             "group-common-proxies");

        assertThat(repository.getGroupRepositories().size(), CoreMatchers.equalTo(4));
        Iterator<String> iterator = repository.getGroupRepositories().iterator();
        assertThat(iterator.next(), CoreMatchers.equalTo("carlspring"));
        assertThat(iterator.next(), CoreMatchers.equalTo("maven-central"));
        assertThat(iterator.next(), CoreMatchers.equalTo("apache-snapshots"));
        assertThat(iterator.next(), CoreMatchers.equalTo("jboss-public-releases"));
    }

    @Test
    public void multipleAdditionOfTheSameRepositoryShouldNotAffectGroup() throws IOException
    {
        configurationManagementService.addRepositoryToGroup("storage-common-proxies",
                                                            "group-common-proxies",
                                                            "maven-central");
        configurationManagementService.addRepositoryToGroup("storage-common-proxies",
                                                            "group-common-proxies",
                                                            "maven-central");
        configurationManagementService.addRepositoryToGroup("storage-common-proxies",
                                                            "group-common-proxies",
                                                            "maven-central");

        RepositoryData repository = configurationManagementService.getConfiguration()
                                                              .getRepository("storage-common-proxies",
                                                                             "group-common-proxies");

        assertThat(repository.getGroupRepositories().size(), CoreMatchers.equalTo(4));
        Iterator<String> iterator = repository.getGroupRepositories().iterator();
        assertThat(iterator.next(), CoreMatchers.equalTo("carlspring"));
        assertThat(iterator.next(), CoreMatchers.equalTo("maven-central"));
        assertThat(iterator.next(), CoreMatchers.equalTo("apache-snapshots"));
        assertThat(iterator.next(), CoreMatchers.equalTo("jboss-public-releases"));
    }

    @Test
    public void testGetGroupRepositories()
    {
        List<RepositoryData> groupRepositories = configurationManagementService.getConfiguration().getGroupRepositories();

        assertFalse(groupRepositories.isEmpty());

        logger.debug("Group repositories:");

        for (RepositoryData repository : groupRepositories)
        {
            logger.debug(" - " + repository.getId());
        }
    }

    @Test
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    public void testGetGroupRepositoriesContainingRepository(@NullRepository(repositoryId = REPOSITORY_RELEASES_1) RepositoryData releases1,
                                                             @TestRepository.Group(repositories = REPOSITORY_RELEASES_1)
                                                             @NullRepository(repositoryId = REPOSITORY_GROUP_1) RepositoryData releasesGroup)
    {
        List<RepositoryData> groups = configurationManagementService.getConfiguration()
                                                                .getGroupRepositoriesContaining(STORAGE0,
                                                                                                REPOSITORY_RELEASES_1);

        assertFalse(groups.isEmpty());

        logger.debug("Group repositories containing \"" + REPOSITORY_RELEASES_1 + "\" repository:");

        for (RepositoryData repository : groups)
        {
            logger.debug(" - " + repository.getId());
        }
    }

    @Test
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    public void testRemoveRepositoryFromAssociatedGroups(@NullRepository(repositoryId = REPOSITORY_RELEASES_1) 
                                                         RepositoryData releases1,
                                                         @TestRepository.Group(repositories = REPOSITORY_RELEASES_1)
                                                         @NullRepository(repositoryId = REPOSITORY_GROUP_1) 
                                                         RepositoryData releasesGroup1,
                                                         @TestRepository.Group(repositories = REPOSITORY_RELEASES_1)
                                                         @NullRepository(repositoryId = REPOSITORY_GROUP_2) 
                                                         RepositoryData releasesGroup2) throws IOException
    {
        assertEquals(2,
                     configurationManagementService.getConfiguration()
                                                   .getGroupRepositoriesContaining(STORAGE0,
                                                                                   REPOSITORY_RELEASES_1).size(),
                     "Failed to add repository to group!");

        configurationManagementService.removeRepositoryFromAssociatedGroups(STORAGE0, REPOSITORY_RELEASES_1);

        assertEquals(0,
                     configurationManagementService.getConfiguration()
                                                   .getGroupRepositoriesContaining(STORAGE0,
                                                                                   REPOSITORY_RELEASES_1).size(),
                     "Failed to remove repository from all associated groups!");
    }

    @Test
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    public void testSetProxyRepositoryMaxConnections(@NullRepository(repositoryId = REPOSITORY_RELEASES_2) 
                                                     RepositoryData releases1) throws IOException
    {
        StorageData storage = configurationManagementService.getConfiguration().getStorage(STORAGE0);

        RepositoryData repository = storage.getRepository(REPOSITORY_RELEASES_2);

        configurationManagementService.setProxyRepositoryMaxConnections(storage.getId(), repository.getId(), 10);

        HttpConnectionPool pool = configurationManagementService.getConfiguration()
                                                                .getHttpConnectionPoolConfiguration(storage.getId(),
                                                                                                    repository.getId());

        assertNotNull(pool);
        assertEquals(10, pool.getAllocatedConnections());
    }

    @Test
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    public void shouldAddEditAndRemoveRoutingRule(@NullRepository(repositoryId = REPOSITORY_RELEASES_1) RepositoryData releases1,
                                                  @TestRepository.Group(repositories = REPOSITORY_RELEASES_1)
                                                  @NullRepository(repositoryId = REPOSITORY_GROUP_1) RepositoryData releasesGroup) throws IOException
    {
        final MutableRoutingRule routingRule = createRoutingRule(RoutingRuleTypeEnum.ACCEPT);
        String repositoryId = routingRule.getRepositoryId();
        String storageId = routingRule.getStorageId();

        final boolean added = configurationManagementService.addRoutingRule(routingRule);
        assertTrue(added);

        Configuration configuration = configurationManagementService.getConfiguration();
        List<RoutingRule> routingRulesMatching = configuration.getRoutingRules()
                                                              .getRules()
                                                              .stream()
                                                              .filter(a -> repositoryId.equals(a.getRepositoryId()) &&
                                                                           storageId.equals(a.getStorageId()))
                                                              .collect(Collectors.toList());

        assertThat(routingRulesMatching.size(), CoreMatchers.equalTo(1));
        RoutingRule dbRoutingRule = routingRulesMatching.get(0);
        assertThat(dbRoutingRule.getType(), CoreMatchers.equalTo(RoutingRuleTypeEnum.ACCEPT));

        routingRule.setType(RoutingRuleTypeEnum.DENY.getType());
        boolean updated = configurationManagementService.updateRoutingRule(dbRoutingRule.getUuid(), routingRule);
        assertTrue(updated);

        configuration = configurationManagementService.getConfiguration();
        routingRulesMatching = configuration.getRoutingRules()
                                            .getRules()
                                            .stream()
                                            .filter(a -> repositoryId.equals(a.getRepositoryId()) &&
                                                         storageId.equals(a.getStorageId()))
                                            .collect(Collectors.toList());

        assertThat(routingRulesMatching.size(), CoreMatchers.equalTo(1));
        RoutingRule routingRule1 = routingRulesMatching.get(0);
        assertThat(routingRule1.getType(), CoreMatchers.equalTo(RoutingRuleTypeEnum.DENY));

        boolean removed = configurationManagementService.removeRoutingRule(dbRoutingRule.getUuid());
        assertTrue(removed);

        configuration = configurationManagementService.getConfiguration();
        routingRulesMatching = configuration.getRoutingRules()
                                            .getRules()
                                            .stream()
                                            .filter(a -> repositoryId.equals(a.getRepositoryId()) &&
                                                         storageId.equals(a.getStorageId()))
                                            .collect(Collectors.toList());

        assertThat(routingRulesMatching.size(), CoreMatchers.equalTo(0));


    }


    @Test
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    public void testCanGetRepositoriesWithStorageAndLayout(@NullRepository(repositoryId = REPOSITORY_RELEASES_1) 
                                                           RepositoryData releases1,
                                                           @NullRepository(repositoryId = REPOSITORY_RELEASES_2) 
                                                           RepositoryData releases2,
                                                           @TestRepository.Group(repositories = REPOSITORY_RELEASES_1)
                                                           @NullRepository(repositoryId = REPOSITORY_GROUP_1) 
                                                           RepositoryData releasesGroup1,
                                                           @TestRepository.Group(repositories = REPOSITORY_RELEASES_1)
                                                           @NullRepository(repositoryId = REPOSITORY_GROUP_2) 
                                                           RepositoryData releasesGroup2)
    {
        List<RepositoryData> repositories = configurationManagementService.getConfiguration()
                                                                      .getRepositoriesWithLayout(STORAGE0,
                                                                                                 NullArtifactCoordinates.LAYOUT_NAME);

        assertFalse(repositories.isEmpty());

        repositories.forEach(repository -> assertTrue(repository.getLayout().equals(NullArtifactCoordinates.LAYOUT_NAME)));

        repositories.forEach(repository -> assertTrue(repository.getStorage().getId().equals(STORAGE0)));
    }

    @Test
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    public void testCanGetRepositoriesWithStorageAndLayoutNotExistedStorage(@NullRepository(repositoryId = REPOSITORY_RELEASES_1) 
                                                                            RepositoryData releases1,
                                                                            @NullRepository(repositoryId = REPOSITORY_RELEASES_2) 
                                                                            RepositoryData releases2,
                                                                            @TestRepository.Group(repositories = REPOSITORY_RELEASES_1)
                                                                            @NullRepository(repositoryId = REPOSITORY_GROUP_1) 
                                                                            RepositoryData releasesGroup1,
                                                                            @TestRepository.Group(repositories = REPOSITORY_RELEASES_1)
                                                                            @NullRepository(repositoryId = REPOSITORY_GROUP_2) 
                                                                            RepositoryData releasesGroup2)
    {
        List<RepositoryData> repositories = configurationManagementService.getConfiguration()
                                                                      .getRepositoriesWithLayout("notExistedStorage",
                                                                                                 NullArtifactCoordinates.LAYOUT_NAME);

        assertTrue(repositories.isEmpty());
    }

    private MutableRoutingRule createRoutingRule(RoutingRuleTypeEnum type)
    {
        MutableRoutingRule routingRule = new MutableRoutingRule();
        routingRule.setPattern(RULE_PATTERN);
        routingRule.setRepositories(Collections.singletonList(REPOSITORY_RELEASES_1).stream().map(
                r -> new MutableRoutingRuleRepository(STORAGE0, r)).collect(
                Collectors.toList()));
        routingRule.setType(type.getType());
        routingRule.setRepositoryId(REPOSITORY_GROUP_1);
        routingRule.setStorageId(STORAGE0);
        return routingRule;
    }

}
