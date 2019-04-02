package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.configuration.MutableConfiguration;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.aws.MutableAwsConfiguration;
import org.carlspring.strongbox.storage.repository.gcs.MutableGoogleCloudConfiguration;
import org.carlspring.strongbox.yaml.YAMLMapperFactory;
import org.carlspring.strongbox.yaml.repository.MutableCustomRepositoryConfiguration;
import org.carlspring.strongbox.yaml.repository.remote.MutableRemoteRepositoryConfiguration;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = { StorageApiTestConfig.class })
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(ExecutionMode.CONCURRENT)
public class RepositoryTest
{

    @Inject
    private YAMLMapperFactory yamlMapperFactory;

    private YAMLMapper yamlMapper;

    @BeforeEach
    public void init()
    {
        yamlMapper = yamlMapperFactory.create(
                Sets.newHashSet(MutableCustomRepositoryConfiguration.class, MutableRemoteRepositoryConfiguration.class));
    }

    @Test
    public void testAddRepositoryWithCustomConfiguration()
            throws IOException
    {
        MutableRepository repository = createTestRepositoryWithCustomConfig();

        MutableStorage storage = new MutableStorage("storage0");
        storage.addRepository(repository);

        MutableConfiguration configuration = new MutableConfiguration();
        configuration.addStorage(storage);

        StringWriter writer = new StringWriter();
        yamlMapper.writeValue(writer, configuration);
        String serialized = writer.toString();

        System.out.println(serialized);

        assertTrue(serialized.contains("bucket: \"test-bucket\""));
        assertTrue(serialized.contains("key: \"test-key\""));
    }

    @Test
    public void testMarshallAndUnmarshallSimpleConfiguration()
            throws IOException
    {
        MutableStorage storage = new MutableStorage("storage0");

        MutableRepository repository = new MutableRepository("test-repository");
        repository.setStorage(storage);

        storage.addRepository(repository);

        MutableConfiguration configuration = new MutableConfiguration();
        configuration.addStorage(storage);

        StringWriter writer = new StringWriter();
        yamlMapper.writeValue(writer, configuration);
        String serialized = writer.toString();

        System.out.println(serialized);

        assertFalse(serialized.contains("<aws-configuration bucket=\"test-bucket\" key=\"test-key\"/>"));
        assertFalse(serialized.contains("<google-cloud-configuration bucket=\"test-bucket\" key=\"test-key\"/>"));

        MutableConfiguration unmarshalledConfiguration = yamlMapper.readValue(serialized.getBytes(),
                                                                              MutableConfiguration.class);

        assertNotNull(unmarshalledConfiguration.getStorage("storage0"));
    }

    @Test
    public void testMarshallAndUnmarshallSimpleConfigurationWithoutServiceLoader()
            throws IOException
    {
        MutableStorage storage = new MutableStorage("storage0");

        MutableRepository repository = new MutableRepository("test-repository");
        repository.setStorage(storage);

        storage.addRepository(repository);

        MutableConfiguration configuration = new MutableConfiguration();
        configuration.addStorage(storage);

        StringWriter writer = new StringWriter();
        yamlMapper.writeValue(writer, configuration);
        String serialized = writer.toString();

        System.out.println(serialized);

        assertFalse(serialized.contains("<aws-configuration bucket=\"test-bucket\" key=\"test-key\"/>"));
        assertFalse(serialized.contains("<google-cloud-configuration bucket=\"test-bucket\" key=\"test-key\"/>"));

        MutableConfiguration unmarshalledConfiguration = yamlMapper.readValue(serialized.getBytes(),
                                                                              MutableConfiguration.class);

        assertNotNull(unmarshalledConfiguration.getStorage("storage0"));
    }

    @Test
    public void testMarshallAndUnmarshallStrongboxConfiguration()
            throws IOException
    {
        MutableConfiguration configuration = yamlMapper.readValue(
                this.getClass().getResourceAsStream("/etc/conf/strongbox.yaml"), MutableConfiguration.class);
        MutableStorage storage = configuration.getStorage("storage0");

        assertNotNull(storage);
    }

    private MutableRepository createTestRepositoryWithCustomConfig()
    {
        MutableStorage storage = new MutableStorage("storage0");
        MutableRepository repository = new MutableRepository("test-repository");
        repository.setStorage(storage);

        MutableAwsConfiguration awsConfiguration = new MutableAwsConfiguration();
        awsConfiguration.setBucket("test-bucket");
        awsConfiguration.setKey("test-key");

        MutableGoogleCloudConfiguration googleCloudConfiguration = new MutableGoogleCloudConfiguration();
        googleCloudConfiguration.setBucket("test-bucket");
        googleCloudConfiguration.setKey("test-key");

        List<MutableCustomConfiguration> customConfigurations = new ArrayList<>();
        customConfigurations.add(awsConfiguration);
        customConfigurations.add(googleCloudConfiguration);

        repository.setCustomConfigurations(customConfigurations);

        return repository;
    }

}
