package org.carlspring.strongbox.services;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.*;
import org.carlspring.strongbox.storage.routing.*;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author mtodorov
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class ConfigurationManagementServiceImplTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagementServiceImplTest.class);

    private static final String RULE_PATTERN = "\\*.org.test";

    private static final String REPOSITORY_RELEASES_1 = "cmsi-releases-1";

    private static final String REPOSITORY_RELEASES_2 = "cmsi-releases-2";

    private static final String REPOSITORY_GROUP_1 = "csmi-group-1";

    private static final String REPOSITORY_GROUP_2 = "csmi-group-2";

    private static final String REPOSITORY_4_DB_VERSION_1 = "db-versioned-conf-release-1";

    private static final String REPOSITORY_4_DB_VERSION_2 = "db-versioned-conf-release-2";

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;


    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_1, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_4_DB_VERSION_1, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_4_DB_VERSION_2, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @BeforeEach
    public void setUp()
            throws Exception
    {
        MutableRepository repository1 = mavenRepositoryFactory.createRepository(REPOSITORY_RELEASES_1);
        repository1.setType(RepositoryTypeEnum.HOSTED.getType());

        MutableRepository repository2 = mavenRepositoryFactory.createRepository(REPOSITORY_RELEASES_2);
        repository2.setType(RepositoryTypeEnum.HOSTED.getType());

        MutableRepository groupRepository1 = mavenRepositoryFactory.createRepository(REPOSITORY_GROUP_1);
        groupRepository1.setType(RepositoryTypeEnum.GROUP.getType());
        groupRepository1.getGroupRepositories().put(repository1.getId(), repository1.getId());

        MutableRepository groupRepository2 = mavenRepositoryFactory.createRepository(REPOSITORY_GROUP_2);
        groupRepository2.setType(RepositoryTypeEnum.GROUP.getType());
        groupRepository2.getGroupRepositories().put(repository1.getId(), repository1.getId());

        createRepository(STORAGE0, repository1);
        createRepository(STORAGE0, repository2);
        createRepository(STORAGE0, groupRepository1);
        createRepository(STORAGE0, groupRepository2);
    }

    @AfterEach
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    @Test
    public void groupRepositoriesShouldBeSortedAsExpected()
    {
        Repository repository = configurationManagementService.getConfiguration().getRepository(
                "storage-common-proxies",
                "group-common-proxies");

        Iterator<String> iterator = repository.getGroupRepositories().keySet().iterator();
        assertThat(iterator.next(), CoreMatchers.equalTo("carlspring"));
        assertThat(iterator.next(), CoreMatchers.equalTo("maven-central"));
        assertThat(iterator.next(), CoreMatchers.equalTo("apache-snapshots"));
        assertThat(iterator.next(), CoreMatchers.equalTo("jboss-public-releases"));
    }

    @Test
    public void additionOfTheSameGroupRepositoryShouldNotAffectGroupRepositoriesList()
    {
        configurationManagementService.addRepositoryToGroup("storage-common-proxies",
                                                            "group-common-proxies",
                                                            "maven-central");

        Repository repository = configurationManagementService.getConfiguration()
                                                              .getRepository("storage-common-proxies",
                                                                             "group-common-proxies");

        assertThat(repository.getGroupRepositories().size(), CoreMatchers.equalTo(4));
        Iterator<String> iterator = repository.getGroupRepositories().keySet().iterator();
        assertThat(iterator.next(), CoreMatchers.equalTo("carlspring"));
        assertThat(iterator.next(), CoreMatchers.equalTo("maven-central"));
        assertThat(iterator.next(), CoreMatchers.equalTo("apache-snapshots"));
        assertThat(iterator.next(), CoreMatchers.equalTo("jboss-public-releases"));
    }

    @Test
    public void multipleAdditionOfTheSameRepositoryShouldNotAffectGroup()
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

        Repository repository = configurationManagementService.getConfiguration()
                                                              .getRepository("storage-common-proxies",
                                                                             "group-common-proxies");

        assertThat(repository.getGroupRepositories().size(), CoreMatchers.equalTo(4));
        Iterator<String> iterator = repository.getGroupRepositories().keySet().iterator();
        assertThat(iterator.next(), CoreMatchers.equalTo("carlspring"));
        assertThat(iterator.next(), CoreMatchers.equalTo("maven-central"));
        assertThat(iterator.next(), CoreMatchers.equalTo("apache-snapshots"));
        assertThat(iterator.next(), CoreMatchers.equalTo("jboss-public-releases"));
    }

    @Test
    public void testGetGroupRepositories()
    {
        List<Repository> groupRepositories = configurationManagementService.getConfiguration().getGroupRepositories();

        assertFalse(groupRepositories.isEmpty());

        logger.debug("Group repositories:");

        for (Repository repository : groupRepositories)
        {
            logger.debug(" - " + repository.getId());
        }
    }

    @Test
    public void testGetGroupRepositoriesContainingRepository()
    {
        List<Repository> groups = configurationManagementService.getConfiguration()
                                                                .getGroupRepositoriesContaining(STORAGE0,
                                                                                                REPOSITORY_RELEASES_1);

        assertFalse(groups.isEmpty());

        logger.debug("Group repositories containing \"" + REPOSITORY_RELEASES_1 + "\" repository:");

        for (Repository repository : groups)
        {
            logger.debug(" - " + repository.getId());
        }
    }

    @Test
    public void testRemoveRepositoryFromAssociatedGroups()
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

        configurationManagementService.removeRepository(STORAGE0, REPOSITORY_GROUP_1);
        configurationManagementService.removeRepository(STORAGE0, REPOSITORY_GROUP_2);
    }

    @Test
    public void testSetProxyRepositoryMaxConnections()
    {
        Storage storage = configurationManagementService.getConfiguration().getStorage(STORAGE0);

        Repository repository = storage.getRepository(REPOSITORY_RELEASES_2);

        configurationManagementService.setProxyRepositoryMaxConnections(storage.getId(), repository.getId(), 10);

        HttpConnectionPool pool = configurationManagementService.getConfiguration()
                                                                .getHttpConnectionPoolConfiguration(storage.getId(),
                                                                                                    repository.getId());

        assertNotNull(pool);
        assertEquals(10, pool.getAllocatedConnections());
    }

    @Test
    public void shouldAddRoutingRule()
    {
        final MutableRoutingRule routingRule = createRoutingRule(RoutingRuleTypeEnum.ACCEPT);
        final boolean added = configurationManagementService.addRoutingRule(routingRule);
        assertTrue(added);

        final Configuration configuration = configurationManagementService.getConfiguration();

        final List<RoutingRule> routingRules = configuration.getRoutingRules()
                                                            .getAccepted()
                                                            .stream()
                                                            .filter(a -> REPOSITORY_GROUP_1.equals(
                                                                    a.getRepositoryId()) &&
                                                                         STORAGE0.equals(a.getStorageId()))
                                                            .collect(Collectors.toList());
        assertThat(routingRules.size(), CoreMatchers.equalTo(1));
        RoutingRule addedRoutingRule = routingRules.get(0);

        assertNotNull(addedRoutingRule);

        List<RoutingRuleRepository> repositories = addedRoutingRule.getRepositories();
        assertEquals(1, repositories.size());
        RoutingRuleRepository routingRuleRepository = repositories.get(0);
        assertThat(routingRuleRepository.getRepositoryId(), CoreMatchers.equalTo(REPOSITORY_RELEASES_1));
        assertThat(routingRuleRepository.getStorageId(), CoreMatchers.equalTo(STORAGE0));
        assertThat(addedRoutingRule.getPattern(), CoreMatchers.equalTo(RULE_PATTERN));
    }

    @Test
    public void shouldAddAndRemoveRoutingRule()
    {
        final MutableRoutingRule routingRule = createRoutingRule(RoutingRuleTypeEnum.ACCEPT);
        final boolean added = configurationManagementService.addRoutingRule(routingRule);
        assertTrue(added);

        RoutingRules routingRules = configurationManagementService.getConfiguration().getRoutingRules();
        assertThat(routingRules.getRules().size(), Matchers.greaterThan(0));

        boolean removed = configurationManagementService.removeRoutingRule(routingRules.getRules().size() - 1);
        assertTrue(removed);

        final Configuration configuration = configurationManagementService.getConfiguration();
        final List<RoutingRule> allRoutingRules = configuration.getRoutingRules()
                                                               .getAccepted()
                                                               .stream()
                                                               .filter(a -> REPOSITORY_GROUP_1.equals(
                                                                       a.getRepositoryId()) &&
                                                                            STORAGE0.equals(a.getStorageId()))
                                                               .collect(Collectors.toList());

        assertThat(allRoutingRules.size(), CoreMatchers.equalTo(0));
    }

    @Test
    public void shouldAddAndEditRoutingRule()
    {
        final MutableRoutingRule routingRule = createRoutingRule(RoutingRuleTypeEnum.ACCEPT);
        final boolean added = configurationManagementService.addRoutingRule(routingRule);
        assertTrue(added);

        RoutingRules routingRules = configurationManagementService.getConfiguration().getRoutingRules();
        assertThat(routingRules.getRules().size(), Matchers.greaterThan(0));

        routingRule.setType(RoutingRuleTypeEnum.DENY.getType());

        boolean updated = configurationManagementService.updateRoutingRule(routingRules.getRules().size() - 1,
                                                                           routingRule);
        assertTrue(updated);

        final Configuration configuration = configurationManagementService.getConfiguration();
        final List<RoutingRule> allRoutingRules = configuration.getRoutingRules()
                                                               .getAccepted()
                                                               .stream()
                                                               .filter(a -> REPOSITORY_GROUP_1.equals(
                                                                       a.getRepositoryId()) &&
                                                                            STORAGE0.equals(a.getStorageId()))
                                                               .collect(Collectors.toList());

        assertThat(allRoutingRules.size(), CoreMatchers.equalTo(1));
        RoutingRule routingRule1 = allRoutingRules.get(0);
        assertThat(routingRule1.getType(), CoreMatchers.equalTo(RoutingRuleTypeEnum.DENY));
    }


    @Test
    public void testCanGetRepositoriesWithStorageAndLayout()
    {
        String maven2Layout = Maven2LayoutProvider.ALIAS;
        List<Repository> repositories = configurationManagementService.getConfiguration()
                                                                      .getRepositoriesWithLayout(STORAGE0,
                                                                                                 maven2Layout);

        assertFalse(repositories.isEmpty());

        repositories.forEach(repository -> assertTrue(repository.getLayout().equals(maven2Layout)));

        repositories.forEach(repository -> assertTrue(repository.getStorage().getId().equals(STORAGE0)));
    }

    @Test
    public void testCanGetRepositoriesWithStorageAndLayoutNotExistedStorage()
    {
        String maven2Layout = Maven2LayoutProvider.ALIAS;
        List<Repository> repositories = configurationManagementService.getConfiguration()
                                                                      .getRepositoriesWithLayout("notExistedStorage",
                                                                                                 maven2Layout);

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
