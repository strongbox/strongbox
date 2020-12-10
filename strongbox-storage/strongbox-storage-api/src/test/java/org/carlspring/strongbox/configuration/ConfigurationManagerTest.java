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
import java.util.Comparator;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
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

    private static final String STORAGE0 = "storage0";

    @Inject
    private YAMLMapperFactory yamlMapperFactory;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private PropertiesBooter propertiesBooter;

    private YAMLMapper yamlMapper;

    @BeforeEach
    void setUp(TestInfo testInfo)
            throws IOException
    {
        Path configurationBasePath = getConfigurationBasePath(testInfo);
        if (Files.notExists(configurationBasePath))
        {
            Files.createDirectories(configurationBasePath);
        }

        yamlMapper = yamlMapperFactory.create(
                Sets.newHashSet(CustomRepositoryConfigurationDto.class, RemoteRepositoryConfigurationDto.class));
    }

    @AfterEach
    void tearDown(TestInfo testInfo)
            throws IOException
    {
        Path configurationBasePath = getConfigurationBasePath(testInfo);

        Files.walk(configurationBasePath)
             .sorted(Comparator.reverseOrder())
             .map(Path::toFile)
             .forEach(File::delete);
    }


    @Test
    public void testParseConfiguration()
    {
        final Configuration configuration = configurationManager.getConfiguration();

        assertThat(configuration).isNotNull();
        assertThat(configuration.getStorages()).isNotNull();
        assertThat(configuration.getRoutingRules()).isNotNull();

        for (String storageId : configuration.getStorages().keySet())
        {
            assertThat(storageId).as("Storage ID was null!").isNotNull();
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
    public void testStoreConfiguration(TestInfo testInfo)
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

        final String storageId = "myStorageId";
        final Path storageBasePath = getStorageBasePath(storageId, testInfo);

        StorageDto storage = new StorageDto(storageId);
        storage.setBasedir(storageBasePath.toAbsolutePath().toString());
        storage.addRepository(repository1);
        storage.addRepository(repository2);

        MutableConfiguration configuration = new MutableConfiguration();
        configuration.addStorage(storage);
        configuration.setProxyConfiguration(proxyConfigurationGlobal);

        final Path outputFilePath = getConfigurationOutputFilePath(testInfo);
        File outputFile = outputFilePath.toFile();

        yamlMapper.writeValue(outputFile, configuration);

        assertThat(Files.size(outputFilePath) > 0).as("Failed to store the produced YAML!").isTrue();
    }

    @Test
    public void testGroupRepositories(TestInfo testInfo)
            throws IOException
    {
        RepositoryDto repository1 = new RepositoryDto("snapshots");
        RepositoryDto repository2 = new RepositoryDto("ext-snapshots");
        RepositoryDto repository3 = new RepositoryDto("grp-snapshots");
        repository3.addRepositoryToGroup(repository1.getId());
        repository3.addRepositoryToGroup(repository2.getId());

        final String storageId = STORAGE0;
        final Path storageBasePath = getStorageBasePath(storageId, testInfo);

        StorageDto storage = new StorageDto(storageId);
        storage.setBasedir(storageBasePath.toAbsolutePath().toString());
        storage.addRepository(repository1);
        storage.addRepository(repository2);
        storage.addRepository(repository3);

        MutableConfiguration configuration = new MutableConfiguration();
        configuration.addStorage(storage);

        final Path outputFilePath = getConfigurationOutputFilePath(testInfo);
        File outputFile = outputFilePath.toFile();

        yamlMapper.writeValue(outputFile, configuration);

        assertThat(Files.size(outputFilePath) > 0).as("Failed to store the produced YAML!").isTrue();

        MutableConfiguration c = yamlMapper.readValue(outputFile.toURI().toURL(), MutableConfiguration.class);

        assertThat(c.getStorages().get(storageId)
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
    }

    @Test
    public void testCorsConfiguration(TestInfo testInfo)
            throws IOException
    {

        MutableCorsConfiguration corsConfiguration = new MutableCorsConfiguration(
                Arrays.asList("http://example.com", "https://github.com/strongbox", "http://carlspring.org"));

        MutableConfiguration configuration = new MutableConfiguration();
        configuration.setCorsConfiguration(corsConfiguration);

        final Path outputFilePath = getConfigurationOutputFilePath(testInfo);
        File outputFile = outputFilePath.toFile();

        yamlMapper.writeValue(outputFile, configuration);

        assertThat(Files.size(outputFilePath) > 0).as("Failed to store the produced YAML!").isTrue();

        MutableConfiguration c = yamlMapper.readValue(outputFile, MutableConfiguration.class);

        assertThat(c.getCorsConfiguration().getAllowedOrigins())
                .as("Failed to read saved cors allowedOrigins!")
                .hasSize(3);
    }

    @Test
    public void testSmtpConfiguration(TestInfo testInfo)
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

        final Path outputFilePath = getConfigurationOutputFilePath(testInfo);
        File outputFile = outputFilePath.toFile();

        yamlMapper.writeValue(outputFile, configuration);

        assertThat(Files.size(outputFilePath) > 0).as("Failed to store the produced YAML!").isTrue();

        MutableConfiguration c = yamlMapper.readValue(outputFile, MutableConfiguration.class);

        MutableSmtpConfiguration savedSmtpConfiguration = c.getSmtpConfiguration();

        assertThat(savedSmtpConfiguration.getHost()).as("Failed to read saved smtp host!").isEqualTo(smtpHost);
        assertThat(savedSmtpConfiguration.getPort()).as("Failed to read saved smtp port!").isEqualTo(smtpPort);
        assertThat(savedSmtpConfiguration.getConnection()).as("Failed to read saved smtp connection!").isEqualTo(smtpConnection);
        assertThat(savedSmtpConfiguration.getUsername()).as("Failed to read saved smtp username!").isEqualTo(smtpUsername);
        assertThat(savedSmtpConfiguration.getPassword()).as("Failed to read saved smtp password!").isEqualTo(smtpPassword);
    }

    private Path getStorageBasePath(final String storageId,
                                    TestInfo testInfo)
    {
        final String methodName = getMethodName(testInfo);
        return Paths.get(propertiesBooter.getVaultDirectory(), "storages", storageId + "-" + methodName);
    }

    private Path getConfigurationBasePath(TestInfo testInfo)
    {
        final String methodName = getMethodName(testInfo);
        return Paths.get(propertiesBooter.getVaultDirectory(), "etc", "conf", methodName);
    }

    private Path getConfigurationOutputFilePath(TestInfo testInfo)
    {
        final String methodName = getMethodName(testInfo);
        final String fileName = String.format("strongbox-saved-cm-%s.yaml", methodName);

        return getConfigurationBasePath(testInfo).resolve(fileName);
    }

    private String getMethodName(TestInfo testInfo)
    {
        Assumptions.assumeTrue(testInfo.getTestMethod().isPresent());
        return testInfo.getTestMethod().get().getName();
    }
}
