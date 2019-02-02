package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.routing.MutableRoutingRule;
import org.carlspring.strongbox.storage.routing.MutableRoutingRules;
import org.carlspring.strongbox.storage.routing.MutableRuleSet;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.carlspring.strongbox.testing.TestCaseWithRepository.STORAGE0;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author mtodorov
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = StorageApiTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class ConfigurationManagerTest
{

    public static final String TEST_CLASSES = "target/test-classes";

    public static final String CONFIGURATION_BASEDIR = TEST_CLASSES + "/xml";

    public static final String CONFIGURATION_OUTPUT_FILE = CONFIGURATION_BASEDIR + "/strongbox-saved-cm.xml";

    private GenericParser<MutableConfiguration> parser = new GenericParser<>(MutableConfiguration.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private ConfigurationResourceResolver configurationResourceResolver;


    @BeforeEach
    public void setUp()
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
    {
        final Configuration configuration = configurationManager.getConfiguration();

        assertNotNull(configuration);
        assertNotNull(configuration.getStorages());
        assertNotNull(configuration.getRoutingRules());
        // assertFalse(configuration.getRoutingRules().getWildcardAcceptedRules().getRoutingRules().isEmpty());
        // assertFalse(configuration.getRoutingRules().getWildcardDeniedRules().getRoutingRules().isEmpty());

        for (String storageId : configuration.getStorages().keySet())
        {
            assertNotNull(storageId, "Storage ID was null!");
            // assertTrue(!configuration.getStorages().get(storageId).getRepositories().isEmpty(), "No repositories were parsed!");
        }

        assertTrue(configuration.getStorages().size() > 0, "Unexpected number of storages!");
        assertNotNull(configuration.getVersion(), "Incorrect version!");
        assertEquals(48080, configuration.getPort(), "Incorrect port number!");
        assertTrue(configuration.getStorages()
                                .get("storage0")
                                .getRepositories()
                                .get("snapshots")
                                .isSecured(),
                   "Repository should have required authentication!");

        assertTrue(configuration.getStorages()
                                .get("storage0")
                                .getRepositories()
                                .get("releases")
                                .allowsDirectoryBrowsing());
    }

    @Test
    public void testStoreConfiguration()
            throws IOException, JAXBException
    {
        MutableProxyConfiguration proxyConfigurationGlobal = new MutableProxyConfiguration();
        proxyConfigurationGlobal.setUsername("maven");
        proxyConfigurationGlobal.setPassword("password");
        proxyConfigurationGlobal.setHost("192.168.100.1");
        proxyConfigurationGlobal.setPort(8080);
        proxyConfigurationGlobal.addNonProxyHost("192.168.100.1");
        proxyConfigurationGlobal.addNonProxyHost("192.168.100.2");

        MutableProxyConfiguration proxyConfigurationRepository1 = new MutableProxyConfiguration();
        proxyConfigurationRepository1.setUsername("maven");
        proxyConfigurationRepository1.setPassword("password");
        proxyConfigurationRepository1.setHost("192.168.100.5");
        proxyConfigurationRepository1.setPort(8080);
        proxyConfigurationRepository1.addNonProxyHost("192.168.100.10");
        proxyConfigurationRepository1.addNonProxyHost("192.168.100.11");

        MutableRepository repository1 = new MutableRepository("snapshots");
        repository1.setProxyConfiguration(proxyConfigurationRepository1);

        MutableRepository repository2 = new MutableRepository("releases");

        MutableStorage storage = new MutableStorage();
        storage.setId("myStorageId");
        storage.setBasedir(new File(configurationResourceResolver.getVaultDirectory() + "/storages" + STORAGE0)
                                   .getAbsolutePath());
        storage.addRepository(repository1);
        storage.addRepository(repository2);

        MutableConfiguration configuration = new MutableConfiguration();
        configuration.addStorage(storage);
        configuration.setProxyConfiguration(proxyConfigurationGlobal);

        File outputFile = new File(CONFIGURATION_OUTPUT_FILE);

        parser.store(configuration, outputFile.getCanonicalPath());

        assertTrue(outputFile.length() > 0, "Failed to store the produced XML!");
    }

    @Test
    public void testGroupRepositories()
            throws IOException, JAXBException
    {
        MutableRepository repository1 = new MutableRepository("snapshots");
        MutableRepository repository2 = new MutableRepository("ext-snapshots");
        MutableRepository repository3 = new MutableRepository("grp-snapshots");
        repository3.addRepositoryToGroup(repository1.getId());
        repository3.addRepositoryToGroup(repository2.getId());

        MutableStorage storage = new MutableStorage("storage0");
        storage.setBasedir(new File(configurationResourceResolver.getVaultDirectory() + "/storages" + STORAGE0)
                                   .getAbsolutePath());
        storage.addRepository(repository1);
        storage.addRepository(repository2);
        storage.addRepository(repository3);

        MutableConfiguration configuration = new MutableConfiguration();
        configuration.addStorage(storage);

        File outputFile = new File(CONFIGURATION_OUTPUT_FILE);

        parser.store(configuration, outputFile.getCanonicalPath());

        assertTrue(outputFile.length() > 0, "Failed to store the produced XML!");

        MutableConfiguration c = parser.parse(outputFile.toURI().toURL());

        assertEquals(2,
                     c.getStorages().get("storage0")
                      .getRepositories()
                      .get("grp-snapshots")
                      .getGroupRepositories()
                      .size(),
                     "Failed to read repository groups!");
    }

    @Test
    public void testRoutingRules()
            throws JAXBException
    {
        Set<String> repositories = new LinkedHashSet<>();
        repositories.addAll(Arrays.asList("int-releases", "int-snapshots"));

        MutableRoutingRule routingRule = new MutableRoutingRule(".*(com|org)/artifacts.denied.in.memory.*",
                                                                repositories);

        List<MutableRoutingRule> routingRulesList = new ArrayList<>();
        routingRulesList.add(routingRule);

        MutableRuleSet ruleSet = new MutableRuleSet();
        ruleSet.setGroupRepository("group-internal");
        ruleSet.setRoutingRules(routingRulesList);

        MutableRoutingRules routingRules = new MutableRoutingRules();
        routingRules.addAcceptRule("group-internal", ruleSet);

        GenericParser<MutableRoutingRules> parser = new GenericParser<>(MutableRoutingRule.class,
                                                                        MutableRoutingRules.class,
                                                                        MutableRuleSet.class);

        parser.store(routingRules, new ByteArrayOutputStream());
        // parser.store(routingRules, System.out);

        // Assuming that if there is no error, there is no problem.
        // Not optimal, but that's as good as it gets right now.
    }

    @Test
    public void testCorsConfiguration()
            throws IOException, JAXBException
    {

        MutableCorsConfiguration corsConfiguration = new MutableCorsConfiguration(
                Arrays.asList("http://example.com", "https://github.com/strongbox", "http://carlspring.org"));

        MutableConfiguration configuration = new MutableConfiguration();
        configuration.setCorsConfiguration(corsConfiguration);

        File outputFile = new File(CONFIGURATION_OUTPUT_FILE);

        parser.store(configuration, outputFile.getCanonicalPath());

        assertTrue(outputFile.length() > 0, "Failed to store the produced XML!");

        MutableConfiguration c = parser.parse(outputFile.toURI().toURL());

        assertEquals(3,
                     c.getCorsConfiguration().getAllowedOrigins().size(),
                     "Failed to read saved cors allowedOrigins!");
    }

    @Test
    public void testSmtpConfiguration()
            throws IOException, JAXBException
    {

        final String smtpHost = "localhost";
        final Integer smtpPort = 25;
        final String smtpConnection = "tls";
        final String smtpUsername = "user-name";
        final String smtpPassword = "user-password";

        MutableSmtpConfiguration smtpConfiguration = new MutableSmtpConfiguration(smtpHost,
                                                                                  smtpPort,
                                                                                  smtpConnection,
                                                                                  smtpUsername,
                                                                                  smtpPassword);

        MutableConfiguration configuration = new MutableConfiguration();
        configuration.setSmtpConfiguration(smtpConfiguration);

        File outputFile = new File(CONFIGURATION_OUTPUT_FILE);

        parser.store(configuration, outputFile.getCanonicalPath());

        assertTrue(outputFile.length() > 0, "Failed to store the produced XML!");

        MutableConfiguration c = parser.parse(outputFile.toURI().toURL());

        MutableSmtpConfiguration savedSmtpConfiguration = c.getSmtpConfiguration();

        assertEquals(smtpHost, savedSmtpConfiguration.getHost(), "Failed to read saved smtp host!");
        assertEquals(smtpPort, savedSmtpConfiguration.getPort(), "Failed to read saved smtp port!");
        assertEquals(smtpConnection, savedSmtpConfiguration.getConnection(), "Failed to read saved smtp connection!");
        assertEquals(smtpUsername, savedSmtpConfiguration.getUsername(), "Failed to read saved smtp username!");
        assertEquals(smtpPassword, savedSmtpConfiguration.getPassword(), "Failed to read saved smtp password!");
    }
}
