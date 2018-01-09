package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.VersionValidatorType;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.carlspring.strongbox.testing.TestCaseWithRepository.STORAGE0;
import static org.junit.Assert.*;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = StorageApiTestConfig.class)
public class ConfigurationManagerTest
{

    public static final String TEST_CLASSES = "target/test-classes";

    public static final String CONFIGURATION_BASEDIR = TEST_CLASSES + "/xml";

    public static final String CONFIGURATION_OUTPUT_FILE = CONFIGURATION_BASEDIR + "/strongbox-saved-cm.xml";

    private GenericParser<Configuration> parser = new GenericParser<>(Configuration.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private ArtifactResolutionService artifactResolutionService;


    @Before
    public void setUp()
            throws Exception
    {
        File xmlDir = new File(CONFIGURATION_BASEDIR);
        if (!xmlDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            xmlDir.mkdirs();
        }
    }

    @Test
    public void testParseConfiguration()
            throws IOException
    {
        final Configuration configuration = configurationManager.getConfiguration();

        assertNotNull(configuration);
        assertNotNull(configuration.getStorages());
        assertNotNull(configuration.getRoutingRules());
        // assertFalse(configuration.getRoutingRules().getWildcardAcceptedRules().getRoutingRules().isEmpty());
        // assertFalse(configuration.getRoutingRules().getWildcardDeniedRules().getRoutingRules().isEmpty());

        for (String storageId : configuration.getStorages().keySet())
        {
            assertNotNull("Storage ID was null!", storageId);
            // assertTrue("No repositories were parsed!", !configuration.getStorages().get(storageId).getRepositories().isEmpty());
        }

        assertTrue("Unexpected number of storages!", configuration.getStorages().size() > 0);
        assertNotNull("Incorrect version!", configuration.getVersion());
        assertEquals("Incorrect port number!", 48080, configuration.getPort());
        assertTrue("Repository should have required authentication!",
                   configuration.getStorages()
                                .get("storage0")
                                .getRepositories()
                                .get("snapshots").isSecured());

        assertTrue(configuration.getStorages()
                                .get("storage0")
                                .getRepositories()
                                .get("releases").allowsDirectoryBrowsing());

        Set<VersionValidatorType> versionValidators = configuration.getStorages()
                                                                   .get("storage0")
                                                                   .getRepositories()
                                                                   .get("releases")
                                                                   .getVersionValidators();

        assertTrue(versionValidators.size() == 3);
        assertTrue(versionValidators.remove(VersionValidatorType.REDEPLOYMENT));
        assertTrue(versionValidators.remove(VersionValidatorType.RELEASE));
        assertTrue(versionValidators.remove(VersionValidatorType.SNAPSHOT));
        // assertTrue(versionValidators.remove(VersionValidatorType.SEM_VER));
        assertTrue(versionValidators.size() == 0);
    }

    @Test
    public void testStoreConfiguration()
            throws IOException, JAXBException
    {
        ProxyConfiguration proxyConfigurationGlobal = new ProxyConfiguration();
        proxyConfigurationGlobal.setUsername("maven");
        proxyConfigurationGlobal.setPassword("password");
        proxyConfigurationGlobal.setHost("192.168.100.1");
        proxyConfigurationGlobal.setPort(8080);
        proxyConfigurationGlobal.addNonProxyHost("192.168.100.1");
        proxyConfigurationGlobal.addNonProxyHost("192.168.100.2");

        ProxyConfiguration proxyConfigurationRepository1 = new ProxyConfiguration();
        proxyConfigurationRepository1.setUsername("maven");
        proxyConfigurationRepository1.setPassword("password");
        proxyConfigurationRepository1.setHost("192.168.100.5");
        proxyConfigurationRepository1.setPort(8080);
        proxyConfigurationRepository1.addNonProxyHost("192.168.100.10");
        proxyConfigurationRepository1.addNonProxyHost("192.168.100.11");

        Repository repository1 = new Repository("snapshots");
        repository1.setProxyConfiguration(proxyConfigurationRepository1);

        Repository repository2 = new Repository("releases");

        Storage storage = new Storage();
        storage.setId("myStorageId");
        storage.setBasedir(new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages" + STORAGE0)
                                   .getAbsolutePath());
        storage.addRepository(repository1);
        storage.addRepository(repository2);

        Configuration configuration = new Configuration();
        configuration.addStorage(storage);
        configuration.setProxyConfiguration(proxyConfigurationGlobal);

        File outputFile = new File(CONFIGURATION_OUTPUT_FILE);

        parser.store(configuration, outputFile.getCanonicalPath());

        assertTrue("Failed to store the produced XML!", outputFile.length() > 0);
    }

    @Test
    public void testGroupRepositories()
            throws IOException, JAXBException
    {
        Repository repository1 = new Repository("snapshots");
        Repository repository2 = new Repository("ext-snapshots");
        Repository repository3 = new Repository("grp-snapshots");
        repository3.addRepositoryToGroup(repository1.getId());
        repository3.addRepositoryToGroup(repository2.getId());

        Storage storage = new Storage("storage0");
        storage.setBasedir(new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages" + STORAGE0)
                                   .getAbsolutePath());
        storage.addRepository(repository1);
        storage.addRepository(repository2);
        storage.addRepository(repository3);

        Configuration configuration = new Configuration();
        configuration.addStorage(storage);

        File outputFile = new File(CONFIGURATION_OUTPUT_FILE);

        parser.store(configuration, outputFile.getCanonicalPath());

        assertTrue("Failed to store the produced XML!", outputFile.length() > 0);

        Configuration c = parser.parse(outputFile);

        assertEquals("Failed to read repository groups!",
                     2,
                     c.getStorages().get("storage0")
                      .getRepositories()
                      .get("grp-snapshots")
                      .getGroupRepositories()
                      .size());
    }

    @Test
    public void testRoutingRules()
            throws JAXBException
    {
        Set<String> repositories = new LinkedHashSet<>();
        repositories.addAll(Arrays.asList("int-releases", "int-snapshots"));

        RoutingRule routingRule = new RoutingRule(".*(com|org)/artifacts.denied.in.memory.*", repositories);

        List<RoutingRule> routingRulesList = new ArrayList<>();
        routingRulesList.add(routingRule);

        RuleSet ruleSet = new RuleSet();
        ruleSet.setGroupRepository("group-internal");
        ruleSet.setRoutingRules(routingRulesList);

        RoutingRules routingRules = new RoutingRules();
        routingRules.addAcceptRule("group-internal", ruleSet);

        GenericParser<RoutingRules> parser = new GenericParser<>(RoutingRule.class,
                                                                 RoutingRules.class,
                                                                 RuleSet.class);

        parser.store(routingRules, new ByteArrayOutputStream());
        // parser.store(routingRules, System.out);

        // Assuming that if there is no error, there is no problem.
        // Not optimal, but that's as good as it gets right now.
    }

}
