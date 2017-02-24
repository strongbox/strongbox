package org.carlspring.strongbox.services;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationRepository;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.HttpConnectionPool;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RuleSet;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationWithIndexing;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.*;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@Commit
public class ConfigurationManagementServiceImplTest
        extends TestCaseWithArtifactGenerationWithIndexing
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagementServiceImplTest.class);

    private static final String RULE_PATTERN = "*.org.test";

    private static final String REPOSITORY_ID = "repo-id";

    public static final String GROUP_REPOSITORY = "group-repository-id";

    public static final String REPOSITORY_ID_2 = "repo";

    @Autowired
    private ConfigurationRepository configurationRepository;


    @Autowired
    private ConfigurationManagementService configurationManagementService;


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Before
    public void setUp()
            throws Exception
    {
        Storage storage = configurationManagementService.getStorage(STORAGE0);

        Repository repository = new Repository("cmsi-releases");
        repository.setType(RepositoryTypeEnum.HOSTED.getType());
        repository.setStorage(storage);

        Repository groupRepository1 = new Repository("cmsi-group-1");
        groupRepository1.setType(RepositoryTypeEnum.GROUP.getType());
        groupRepository1.getGroupRepositories().add(repository.getId());
        groupRepository1.setStorage(storage);

        Repository groupRepository2 = new Repository("cmsi-group-2");
        groupRepository2.setType(RepositoryTypeEnum.GROUP.getType());
        groupRepository2.getGroupRepositories().add(repository.getId());
        groupRepository2.setStorage(storage);

        createRepository(repository);
        createRepository(groupRepository1);
        createRepository(groupRepository2);
    }

    @PreDestroy
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(mockRepositoryMock(STORAGE0, "cmsi-releases"));
        repositories.add(mockRepositoryMock(STORAGE0, "cmsi-group-1"));
        repositories.add(mockRepositoryMock(STORAGE0, "cmsi-group-2"));

        return repositories;
    }

    @Test
    public void testGetGroupRepositories() throws Exception
    {
        List<Repository> groupRepositories = configurationManagementService.getGroupRepositories();

        assertFalse(groupRepositories.isEmpty());

        logger.debug("Group repositories:");

        for (Repository repository : groupRepositories)
        {
            logger.debug(" - " + repository.getId());
        }
    }

    @Test
    public void testGetGroupRepositoriesContainingRepository() throws Exception
    {
        List<Repository> groupRepositoriesContainingReleases = configurationManagementService.getGroupRepositoriesContaining("cmsi-releases");

        assertFalse(groupRepositoriesContainingReleases.isEmpty());

        logger.debug("Group repositories containing \"cmsi-releases\" repository:");

        for (Repository repository : groupRepositoriesContainingReleases)
        {
            logger.debug(" - " + repository.getId());
        }
    }

    @Test
    public void testRemoveRepositoryFromAssociatedGroups() throws Exception
    {
        assertEquals("Failed to add repository to group!",
                     2,
                     configurationManagementService.getGroupRepositoriesContaining("cmsi-releases").size());

        configurationManagementService.removeRepositoryFromAssociatedGroups("cmsi-releases");

        assertEquals("Failed to remove repository from all associated groups!",
                     0,
                     configurationManagementService.getGroupRepositoriesContaining("csmi-releases").size());

        configurationManagementService.removeRepository(STORAGE0, "cmsi-group-1");
        configurationManagementService.removeRepository(STORAGE0, "cmsi-group-2");
    }

    @Test
    public void testSetProxyRepositoryMaxConnections() throws IOException, JAXBException
    {
        Storage storage = configurationManagementService.getStorage(STORAGE0);

        Repository repository = new Repository("test-repository-releases");
        repository.setType(RepositoryTypeEnum.HOSTED.getType());
        repository.setStorage(storage);

        configurationManagementService.addOrUpdateRepository(STORAGE0, repository);

        configurationManagementService.setProxyRepositoryMaxConnections(storage.getId(), repository.getId(), 10);

        HttpConnectionPool pool = configurationManagementService.getHttpConnectionPoolConfiguration(storage.getId(),
                                                                                                    repository.getId());

        assertNotNull(pool);
        assertEquals(10, pool.getAllocatedConnections());
    }

    @Test
    public void addAcceptedRuleSet()
            throws Exception
    {
        final RuleSet ruleSet = getRuleSet();
        final boolean added = configurationManagementService.addOrUpdateAcceptedRuleSet(ruleSet);
        final Configuration configuration = configurationRepository.getConfiguration();

        final RuleSet addedRuleSet = configuration.getRoutingRules().getAccepted().get(GROUP_REPOSITORY);

        assertTrue(added);
        assertNotNull(addedRuleSet);
        assertEquals(1, addedRuleSet.getRoutingRules().size());
        assertTrue(addedRuleSet.getRoutingRules().get(0).getRepositories().contains(REPOSITORY_ID));
        assertEquals(1, addedRuleSet.getRoutingRules().get(0).getRepositories().size());
        assertEquals(RULE_PATTERN, addedRuleSet.getRoutingRules().get(0).getPattern());
    }

    @Test
    public void testRemoveAcceptedRuleSet()
            throws Exception
    {
        configurationManagementService.addOrUpdateAcceptedRuleSet(getRuleSet());

        final boolean removed = configurationManagementService.removeAcceptedRuleSet(GROUP_REPOSITORY);

        final Configuration configuration = configurationRepository.getConfiguration();
        final RuleSet addedRuleSet = configuration.getRoutingRules().getAccepted().get(GROUP_REPOSITORY);

        assertTrue(removed);
        assertNull(addedRuleSet);
    }

    @Test
    public void testAddAcceptedRepo()
            throws Exception
    {
        configurationManagementService.addOrUpdateAcceptedRuleSet(getRuleSet());

        final boolean added = configurationManagementService.addOrUpdateAcceptedRepository(GROUP_REPOSITORY, getRoutingRule());
        final Configuration configuration = configurationRepository.getConfiguration();

        assertTrue(added);

        configuration.getRoutingRules()
                     .getAccepted()
                     .get(GROUP_REPOSITORY)
                     .getRoutingRules()
                     .stream()
                     .filter(routingRule -> routingRule.getPattern().equals(RULE_PATTERN))
                     .forEach(routingRule -> assertTrue(routingRule.getRepositories().contains(REPOSITORY_ID_2)));
    }

    @Test
    public void testRemoveAcceptedRepository()
            throws Exception
    {
        configurationManagementService.addOrUpdateAcceptedRuleSet(getRuleSet());

        final boolean removed = configurationManagementService.removeAcceptedRepository(GROUP_REPOSITORY,
                                                                                        RULE_PATTERN,
                                                                                        REPOSITORY_ID);

        final Configuration configuration = configurationRepository.getConfiguration();
        configuration.getRoutingRules().getAccepted().get(GROUP_REPOSITORY).getRoutingRules().forEach(
                routingRule -> {
                    if (routingRule.getPattern().equals(RULE_PATTERN))
                    {
                        assertFalse(routingRule.getRepositories().contains(REPOSITORY_ID));
                    }
                }
        );

        assertTrue(removed);
    }

    @Test
    public void testOverrideAcceptedRepositories()
            throws Exception
    {
        configurationManagementService.addOrUpdateAcceptedRuleSet(getRuleSet());

        final RoutingRule rl = getRoutingRule();
        final boolean overridden = configurationManagementService.overrideAcceptedRepositories(GROUP_REPOSITORY, rl);
        final Configuration configuration = configurationRepository.getConfiguration();
        configuration.getRoutingRules().getAccepted().get(GROUP_REPOSITORY).getRoutingRules().forEach(
                routingRule -> {
                    if (routingRule.getPattern().equals(rl.getPattern()))
                    {
                        assertEquals(1, routingRule.getRepositories().size());
                        assertEquals(rl.getRepositories(), routingRule.getRepositories());
                    }
                }
        );

        assertTrue(overridden);
    }

    private RoutingRule getRoutingRule()
    {
        RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(RULE_PATTERN);
        routingRule.setRepositories(new HashSet<>(Collections.singletonList(REPOSITORY_ID_2)));

        return routingRule;
    }

    private RuleSet getRuleSet()
    {
        RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(RULE_PATTERN);
        routingRule.setRepositories(new HashSet<>(Collections.singletonList(REPOSITORY_ID)));

        RuleSet ruleSet = new RuleSet();
        ruleSet.setGroupRepository(GROUP_REPOSITORY);
        ruleSet.setRoutingRules(Collections.singletonList(routingRule));

        return ruleSet;
    }

}
