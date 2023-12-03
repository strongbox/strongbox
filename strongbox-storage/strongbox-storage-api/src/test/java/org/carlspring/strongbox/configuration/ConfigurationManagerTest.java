package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.storage.StorageDto;
import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.storage.routing.MutableRoutingRule;
import org.carlspring.strongbox.storage.routing.MutableRoutingRuleRepository;
import org.carlspring.strongbox.storage.routing.MutableRoutingRules;
import org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum;
import org.carlspring.strongbox.yaml.YAMLMapperFactory;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfigurationDto;
import org.carlspring.strongbox.yaml.repository.remote.RemoteRepositoryConfigurationDto;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author mtodorov
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = StorageApiTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class ConfigurationManagerTest
{

    private static final String TEST_CLASSES = "target/test-classes";

    private static final String CONFIGURATION_BASEDIR = TEST_CLASSES + "/yaml";

    private static final String CONFIGURATION_OUTPUT_FILE = CONFIGURATION_BASEDIR + "/strongbox-saved-cm.yaml";

    private static final String STORAGE0 = "storage0";

    @Inject
    private YAMLMapperFactory yamlMapperFactory;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private PropertiesBooter propertiesBooter;

    private YAMLMapper yamlMapper;


    @BeforeEach
    public void setUp()
            throws IOException
    {
        Path yamlPath = Paths.get(CONFIGURATION_BASEDIR);
        if (Files.notExists(yamlPath))
        {
            Files.createDirectories(yamlPath);
        }

        yamlMapper = yamlMapperFactory.create(
                Sets.newHashSet(CustomRepositoryConfigurationDto.class, RemoteRepositoryConfigurationDto.class));
    }

    @Test
    public void testParseConfiguration()
    {
        final Configuration configuration = configurationManager.getConfiguration();

        assertThat(configuration).isNotNull();
        assertThat(configuration.getStorages()).isNotNull();
        assertThat(configuration.getRoutingRules()).isNotNull();
        // assertThat(configuration.getRoutingRules().getWildcardAcceptedRules().getRoutingRules().isEmpty()).isFalse();
        // assertThat(configuration.getRoutingRules().getWildcardDeniedRules().getRoutingRules().isEmpty()).isFalse();

        for (String storageId : configuration.getStorages().keySet())
        {
            assertThat(storageId).as("Storage ID was null!").isNotNull();
            // assertThat(!configuration.getStorages().get(storageId).getRepositories().isEmpty()).as("No repositories were parsed!").isTrue();
        }

        assertThat(configuration.getStorages()).as("Unexpected number of storages!").isNotEmpty();
        assertThat(configuration.getVersion()).as("Incorrect version!").isNotNull();
        assertThat(configuration.getPort()).as("Incorrect port number!").isEqualTo(48080);
        assertThat(configuration.getStorages()
                                .get(STORAGE0)
                                .getRepositories()
                                .get("snapshots")
                                .isSecured())
                .as("Repository should have required authentication!")
                .isTrue();

        assertThat(configuration.getStorages()
                                .get(STORAGE0)
                                .getRepositories()
                                .get("releases")
                                .allowsDirectoryBrowsing())
                .isTrue();
    }

    @Test
    public void testStoreConfiguration()
            throws IOException
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

        RepositoryDto repository1 = new RepositoryDto("snapshots");
        repository1.setProxyConfiguration(proxyConfigurationRepository1);

        RepositoryDto repository2 = new RepositoryDto("releases");

        StorageDto storage = new StorageDto();
        storage.setId("myStorageId");
        storage.setBasedir(new File(propertiesBooter.getVaultDirectory() + "/storages" + STORAGE0)
                                   .getAbsolutePath());
        storage.addRepository(repository1);
        storage.addRepository(repository2);

        MutableConfiguration configuration = new MutableConfiguration();
        configuration.addStorage(storage);
        configuration.setProxyConfiguration(proxyConfigurationGlobal);

        File outputFile = new File(CONFIGURATION_OUTPUT_FILE);
        yamlMapper.writeValue(outputFile, configuration);

        assertThat(outputFile.length() > 0).as("Failed to store the produced YAML!").isTrue();
    }

    @Test
    public void testGroupRepositories()
            throws IOException
    {
        RepositoryDto repository1 = new RepositoryDto("snapshots");
        RepositoryDto repository2 = new RepositoryDto("ext-snapshots");
        RepositoryDto repository3 = new RepositoryDto("grp-snapshots");
        repository3.addRepositoryToGroup(repository1.getId());
        repository3.addRepositoryToGroup(repository2.getId());

        StorageDto storage = new StorageDto(STORAGE0);
        storage.setBasedir(new File(propertiesBooter.getVaultDirectory() + "/storages" + STORAGE0).getAbsolutePath());
        storage.addRepository(repository1);
        storage.addRepository(repository2);
        storage.addRepository(repository3);

        MutableConfiguration configuration = new MutableConfiguration();
        configuration.addStorage(storage);

        File outputFile = new File(CONFIGURATION_OUTPUT_FILE);

        yamlMapper.writeValue(outputFile, configuration);

        assertThat(outputFile.length() > 0).as("Failed to store the produced YAML!").isTrue();

        MutableConfiguration c = yamlMapper.readValue(outputFile.toURI().toURL(), MutableConfiguration.class);

        assertThat(c.getStorages().get(STORAGE0)
                    .getRepositories()
                    .get("grp-snapshots")
                    .getGroupRepositories())
                .as("Failed to read repository groups!")
                .hasSize(2);
    }

    @Test
    public void testRoutingRules()
            throws IOException
    {

        MutableRoutingRule routingRule = MutableRoutingRule.create(STORAGE0,
                                                                   "group-internal",
                                                                   Arrays.asList(
                                                                           new MutableRoutingRuleRepository(STORAGE0,
                                                                                                            "int-releases"),
                                                                           new MutableRoutingRuleRepository(STORAGE0,
                                                                                                            "int-snapshots")),
                                                                   ".*(com|org)/artifacts.denied.in.memory.*",
                                                                   RoutingRuleTypeEnum.ACCEPT);
        MutableRoutingRules routingRules = new MutableRoutingRules();
        routingRules.setRules(Collections.singletonList(routingRule));

        try (OutputStream os = new ByteArrayOutputStream())
        {
            yamlMapper.writeValue(os, routingRules);
        }

        // parser.store(routingRules, System.out);

        // Assuming that if there is no error, there is no problem.
        // Not optimal, but that's as good as it gets right now.
    }

    @Test
    public void testCorsConfiguration()
            throws IOException
    {

        MutableCorsConfiguration corsConfiguration = new MutableCorsConfiguration(
                Arrays.asList("http://example.com", "https://github.com/strongbox", "http://carlspring.org"));

        MutableConfiguration configuration = new MutableConfiguration();
        configuration.setCorsConfiguration(corsConfiguration);

        File outputFile = new File(CONFIGURATION_OUTPUT_FILE);

        yamlMapper.writeValue(outputFile, configuration);

        assertThat(outputFile.length() > 0).as("Failed to store the produced YAML!").isTrue();

        MutableConfiguration c = yamlMapper.readValue(outputFile, MutableConfiguration.class);

        assertThat(c.getCorsConfiguration().getAllowedOrigins())
                .as("Failed to read saved cors allowedOrigins!")
                .hasSize(3);
    }

    @Test
    public void testSmtpConfiguration()
            throws IOException
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

        yamlMapper.writeValue(outputFile, configuration);

        assertThat(outputFile.length() > 0).as("Failed to store the produced YAML!").isTrue();

        MutableConfiguration c = yamlMapper.readValue(outputFile, MutableConfiguration.class);

        MutableSmtpConfiguration savedSmtpConfiguration = c.getSmtpConfiguration();

        assertThat(savedSmtpConfiguration.getHost()).as("Failed to read saved smtp host!").isEqualTo(smtpHost);
        assertThat(savedSmtpConfiguration.getPort()).as("Failed to read saved smtp port!").isEqualTo(smtpPort);
        assertThat(savedSmtpConfiguration.getConnection()).as("Failed to read saved smtp connection!").isEqualTo(smtpConnection);
        assertThat(savedSmtpConfiguration.getUsername()).as("Failed to read saved smtp username!").isEqualTo(smtpUsername);
        assertThat(savedSmtpConfiguration.getPassword()).as("Failed to read saved smtp password!").isEqualTo(smtpPassword);
    }
}
